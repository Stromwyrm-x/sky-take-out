package com.sky.controller.user;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userSetmealController")
@Slf4j
@RequestMapping("/user/setmeal")
public class SetmealController
{
    @Autowired
    private SetmealService setmealService;

    @GetMapping("/list")
    public Result<List<Setmeal>> getByCategoryId(Long categoryId)
    {
        List<Setmeal> setmealList = setmealService.getByCategoryId(categoryId);
        return Result.success(setmealList);
    }

    @GetMapping("/dish/{id}")
    public Result<List<DishItemVO>> getDishById(@PathVariable Long id)
    {
        List<DishItemVO>dishItemVOList=setmealService.getDishById(id);
        return Result.success(dishItemVOList);
    }


}
