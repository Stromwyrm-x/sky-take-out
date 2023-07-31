package com.sky.controller.user;

import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@Slf4j
@RequestMapping("/user/dish")
public class DishController
{
    @Autowired
    private DishService dishService;

    @GetMapping("/list")
    public Result<List<DishVO>> getByCategoryId(Long categoryId)
    {
        List<DishVO>dishVOList = dishService.getByCategoryIdWithFlavor(categoryId);
        return Result.success(dishVOList);
    }

}
