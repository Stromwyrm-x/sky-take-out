package com.sky.controller.user;

import com.sky.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import static com.sky.controller.admin.ShopController.SHOP_STATUS_KEY;

@RestController("userShopController")
@RequestMapping("/user/shop")
public class ShopController
{
    @Autowired
    private RedisTemplate redisTemplate;

//    private static final String SHOP_STATUS_KEY="SHOP_STATUS";

    @GetMapping("/status")
    public Result<Integer> getStatus()
    {
        Integer status = (Integer) redisTemplate.opsForValue().get(SHOP_STATUS_KEY);
        return Result.success(status);
    }
}
