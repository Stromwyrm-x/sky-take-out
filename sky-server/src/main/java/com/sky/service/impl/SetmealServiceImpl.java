package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService
{
    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @CacheEvict(value = "setmeal_",key = "#setmealDTO.categoryId")
    @Override
    public void addWithSetmealDish(SetmealDTO setmealDTO)
    {
        //1.新增setmeal表
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmeal.setStatus(StatusConstant.DISABLE);
        setmealMapper.insert(setmeal);

        //2.新增setmeal_dish表
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmeal.getId());
        });
        setmealDishMapper.insertBatch(setmealDishes);
    }

    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO)
    {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO>setmealVOPage=setmealMapper.list(setmealPageQueryDTO);
        return new PageResult(setmealVOPage.getTotal(),setmealVOPage.getResult());
    }

    @CacheEvict(value = "setmeal_",allEntries = true)
    @Override
    public void deleteByIdsWithSetmealDish(List<Long> ids)
    {
        //1.启售中的套餐不能删除
        Long count=setmealMapper.countEnableSetmealByIds(ids);
        if (count>0)
        {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        //2.删除setmeal
        setmealMapper.deleteByIds(ids);
        //3.删除setmeal_dish
        setmealDishMapper.deleteBySetmealIds(ids);
    }

    @Override
    public SetmealVO getByIdWithSetmeal(Long id)
    {
        //1.查询套餐的基本信息
        Setmeal setmeal=setmealMapper.selectById(id);
        //2.查询setmeal_dish的信息
        List<SetmealDish>setmealDishes=setmealDishMapper.selectBySetmealId(id);
        //3.封装
        SetmealVO setmealVO = SetmealVO.builder().setmealDishes(setmealDishes).build();
        BeanUtils.copyProperties(setmeal,setmealVO);
        return setmealVO;
    }

    @CacheEvict(value = "setmeal_",allEntries = true)
    @Override
    public void updateById(SetmealDTO setmealDTO)
    {
        //1.修改套餐信息
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);
        //2.删除setmeal_dish表
        setmealDishMapper.deleteBySetmealIds(Collections.singletonList(setmealDTO.getId()));
        //3.新增setmeal_dish表
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealDTO.getId());
        });
        setmealDishMapper.insertBatch(setmealDishes);
    }

    @CacheEvict(value = "setmeal_",allEntries = true)
    @Override
    public void changeStatus(Integer status, Long id)
    {
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);

    }

    @Cacheable(value = "setmeal_",key = "#categoryId",unless = "#result==null")
    @Override
    public List<Setmeal> getByCategoryId(Long categoryId)
    {
        List<Setmeal>setmealList=setmealMapper.selectByCategoryId(categoryId);
        return setmealList;
    }

    @Override
    public List<DishItemVO> getDishById(Long id)
    {
        List<DishItemVO>dishItemVOList=setmealDishMapper.selectDishBySetmealId(id);
        return dishItemVOList;
    }

}
