package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper
{
    ShoppingCart list(ShoppingCart shoppingCart);

    @Update("update shopping_cart set number=#{number} where id=#{id}")
    void updateNumberById(ShoppingCart shoppingCartCurrent);

    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
            "values (#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    @Select("select * from shopping_cart where user_id=#{userId}")
    List<ShoppingCart> selectByUserId(Long userId);

    @Delete("delete from shopping_cart where user_id=#{userId}")
    void deleteByUserId(Long userId);

    @Delete("delete from shopping_cart where id=#{id}")
    void deleteById(Long id);
}
