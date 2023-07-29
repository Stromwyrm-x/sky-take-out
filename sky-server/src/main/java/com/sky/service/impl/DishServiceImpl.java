package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.BaseException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl implements DishService
{
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    public void addWithFlavor(DishDTO dishDTO)
    {
        //1.新增dish表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setStatus(StatusConstant.DISABLE);
        dishMapper.insert(dish);
        //2.新增dish_flavor表
        List<DishFlavor> flavors = dishDTO.getFlavors();
        flavors.forEach(dishFlavor ->
        {
            dishFlavor.setDishId(dish.getId());
        });
        dishFlavorMapper.insertBatch(flavors);
    }

    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO)
    {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> dishVOPage = dishMapper.list(dishPageQueryDTO);
        return new PageResult(dishVOPage.getTotal(), dishVOPage.getResult());
    }

    @Override
    public void deleteByIdsWithFlavor(List<Long> ids)
    {
        //1.启售中的菜品不能删除
        Long count = dishMapper.countEnableDishByIds(ids);//查询当前ids集合中，有多少启售的菜品
        if (count > 0)
        {
            throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }
        //2.删除dish和dish_flavor
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }

    @Override
    public void changeStatus(Integer status, Long id)
    {
        //如果菜品关联了套餐，则不可以改变停售
        Long count = setmealDishMapper.countByDishId(id);
        if (count > 0)
        {
            throw new BaseException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //修改菜品状态
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.updateById(dish);
    }

    @Override
    public void updateById(DishDTO dishDTO)
    {
        //1.修改菜品信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.updateById(dish);
        //2.删除口味信息
        dishFlavorMapper.deleteByDishIds(Collections.singletonList(dishDTO.getId()));
        //3.新增口味信息
        List<DishFlavor> flavors = dishDTO.getFlavors();
        flavors.forEach(dishFlavor ->
        {
            dishFlavor.setDishId(dishDTO.getId());
        });
        dishFlavorMapper.insertBatch(flavors);
    }

    @Override
    public List<Dish> getByCategoryId(Long categoryId)
    {
        List<Dish> dishList = dishMapper.selectByCategoryId(categoryId);
        return dishList;
    }

    @Override
    public List<DishVO> getByCategoryIdWithFlavor(Long categoryId)
    {
        //1.根据分类Id查询菜品
        List<Dish> dishList = dishMapper.selectByCategoryId(categoryId);
        //2.根据菜品id查询口味
        List<DishVO> dishVOList = dishList.stream().map(dish ->
        {
            DishVO dishVO = getByIdWithFlavor(dish.getId());
            return dishVO;
        }).collect(Collectors.toList());
        //3.返回封装结果
        return dishVOList;
    }

    @Override
    public DishVO getByIdWithFlavor(Long id)
    {
        //1.查询菜品的基本信息
        Dish dish = dishMapper.selectById(id);
        //2.查询菜品的口味信息
        List<DishFlavor> dishFlavors = dishFlavorMapper.selectByDishId(id);
        //3.封装为dishVO
        DishVO dishVO = DishVO.builder().flavors(dishFlavors).build();
        BeanUtils.copyProperties(dish, dishVO);
        return dishVO;
    }


}
