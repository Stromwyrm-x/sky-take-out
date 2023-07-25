package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/setmeal")
public class SetmealController
{
    @Autowired
    private SetmealService setmealService;
    @PostMapping
    public Result add(@RequestBody SetmealDTO setmealDTO)
    {
        setmealService.addWithSetmealDish(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO)
    {
        PageResult pageResult=setmealService.page(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    public Result deleteByIds(@RequestParam List<Long>ids)
    {
        setmealService.deleteByIdsWithSetmealDish(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<SetmealVO> getById(@PathVariable Long id)
    {
        SetmealVO setmealVO=setmealService.getByIdWithSetmeal(id);
        return Result.success(setmealVO);
    }

    @PutMapping
    public Result updateById(@RequestBody SetmealDTO setmealDTO)
    {
        setmealService.updateById(setmealDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result changeStatus(@PathVariable Integer status,Long id)
    {
        setmealService.changeStatus(status,id);
        return Result.success();
    }

}
