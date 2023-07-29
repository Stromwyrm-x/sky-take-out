package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DishService
{
    @Transactional
    void addWithFlavor(DishDTO dishDTO);

    PageResult page(DishPageQueryDTO dishPageQueryDTO);

    @Transactional
    void deleteByIdsWithFlavor(List<Long> ids);

    DishVO getByIdWithFlavor(Long id);

    void changeStatus(Integer status, Long id);
    @Transactional
    void updateById(DishDTO dishDTO);

    List<Dish> getByCategoryId(Long categoryId);

    List<DishVO> getByCategoryIdWithFlavor(Long categoryId);
}
