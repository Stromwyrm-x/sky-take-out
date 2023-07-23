package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService
{
    void add(CategoryDTO categoryDTO);

    PageResult page(CategoryPageQueryDTO categoryPageQueryDTO);

    void changeStatus(Integer status, Long id);

    void updateById(CategoryDTO categoryDTO);

    void deleteById(Long id);

    List<Category> listByType(Integer type);
}
