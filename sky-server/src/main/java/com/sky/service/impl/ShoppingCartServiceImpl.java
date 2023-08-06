package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService
{
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO)
    {
        //1.查询当前用户购物车中是否有该菜品-口味/套餐
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .dishId(shoppingCartDTO.getDishId())
                .dishFlavor(shoppingCartDTO.getDishFlavor())
                .setmealId(shoppingCartDTO.getSetmealId()).build();
        ShoppingCart shoppingCart_current = shoppingCartMapper.list(shoppingCart);
        //2.若有则number+1
        if (shoppingCart_current != null)
        {
            shoppingCart_current.setNumber(shoppingCart_current.getNumber() + 1);
            shoppingCartMapper.updateNumberById(shoppingCart_current);
        }
        //3.若没有则新增一条数据
        else
        {
            Long dishId = shoppingCart.getDishId();
            Long setmealId = shoppingCart.getSetmealId();
            if (dishId != null)
            {
                //3.1添加的是菜品，查询菜品信息，封装到shoppingCart中
                Dish dish = dishMapper.selectById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            } else if (setmealId != null)
            {
                //3.2添加的是套餐，查询套餐信息，封装到shoppingCart中
                Setmeal setmeal = setmealMapper.selectById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            //3.3新增
            shoppingCartMapper.insert(shoppingCart);
        }

    }

    @Override
    public List<ShoppingCart> getByUserId()
    {
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectByUserId(userId);
        return shoppingCartList;
    }

    @Override
    public void deleteByUserId()
    {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO)
    {
        //1.查询当前用户购物车中是否有该菜品-口味/套餐
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .dishId(shoppingCartDTO.getDishId())
                .dishFlavor(shoppingCartDTO.getDishFlavor())
                .setmealId(shoppingCartDTO.getSetmealId()).build();
        ShoppingCart shoppingCart_current = shoppingCartMapper.list(shoppingCart);
        //2.若number>1，则更新number为number-1
        if (shoppingCart_current != null && shoppingCart_current.getNumber() > 1)
        {
            shoppingCart_current.setNumber(shoppingCart_current.getNumber() - 1);
            shoppingCartMapper.updateNumberById(shoppingCart_current);
        }
        //3.若number=1，则删除该条数据
        else if (shoppingCart_current != null && shoppingCart_current.getNumber() == 1)
        {
            shoppingCartMapper.deleteById(shoppingCart_current.getId());
        }
    }
}
