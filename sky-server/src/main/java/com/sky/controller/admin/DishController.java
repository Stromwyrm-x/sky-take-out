package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("adminDishController")
@Slf4j
@RequestMapping("/admin/dish")
public class DishController
{
    @Autowired
    private DishService dishService;
    @PostMapping
    public Result add(@RequestBody DishDTO dishDTO)
    {
        dishService.addWithFlavor(dishDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO)
    {
        PageResult pageResult=dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    public Result deleteByIds(@RequestParam List<Long>ids)
    {
        dishService.deleteByIdsWithFlavor(ids);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result changeStatus(@PathVariable Integer status,Long id)
    {
        dishService.changeStatus(status,id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id)
    {
        DishVO dishVO=dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @PutMapping
    public Result updateById(@RequestBody DishDTO dishDTO)
    {
        dishService.updateById(dishDTO);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<Dish>> getByCategoryId(Long categoryId)
    {
        List<Dish> dishList=dishService.getByCategoryId(categoryId);
        return Result.success(dishList);
    }

}
