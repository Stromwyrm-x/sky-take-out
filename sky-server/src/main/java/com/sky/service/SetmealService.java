package com.sky.service;

import com.sky.dto.*;
import com.sky.entity.Employee;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SetmealService
{
    @Transactional
    void addWithSetmealDish(SetmealDTO setmealDTO);

    PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);

    @Transactional
    void deleteByIdsWithSetmealDish(List<Long> ids);

    SetmealVO getByIdWithSetmeal(Long id);

    @Transactional
    void updateById(SetmealDTO setmealDTO);

    void changeStatus(Integer status, Long id);
}
