package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController(value = "adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
public class ShopController
{
    @Autowired
    private RedisTemplate redisTemplate;

    public static final String SHOP_STATUS_KEY="SHOP_STATUS";
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status)
    {
        redisTemplate.opsForValue().set(SHOP_STATUS_KEY,status);
        return Result.success();
    }

    @GetMapping("/status")
    public Result<Integer> getStatus()
    {
        Integer status = (Integer) redisTemplate.opsForValue().get(SHOP_STATUS_KEY);
        return Result.success(status);
    }
}
