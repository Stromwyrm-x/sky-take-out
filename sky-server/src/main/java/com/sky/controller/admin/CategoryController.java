package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Slf4j
public class CategoryController
{
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public Result add(@RequestBody CategoryDTO categoryDTO)
    {
        categoryService.add(categoryDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO)
    {
        PageResult pageResult=categoryService.page(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/status/{status}")
    public Result changeStatus(@PathVariable Integer status,Long id)
    {
        categoryService.changeStatus(status,id);
        return Result.success();
    }

    @PutMapping
    public Result updateById(@RequestBody CategoryDTO categoryDTO)
    {
        categoryService.updateById(categoryDTO);
        return Result.success();
    }

    @DeleteMapping
    public Result deleteById(Long id)
    {
        categoryService.deleteById(id);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<Category>>listByType(Integer type)
    {
        List<Category>categoryList=categoryService.listByType(type);
        return Result.success(categoryList);
    }
}
