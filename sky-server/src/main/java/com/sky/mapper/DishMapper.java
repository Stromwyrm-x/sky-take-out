package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper
{

    @Select("select count(*) from dish where category_id=#{id}")
    Integer countByCategoryId(Long id);

    //返回添加数据的主键
    @Options(useGeneratedKeys = true,keyProperty = "id")
    @AutoFill(OperationType.INSERT)
    @Insert("insert into dish (name, category_id, price, image, description, create_time, update_time, create_user, update_user) "+
    "values (#{name},#{categoryId},#{price},#{image},#{description},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Dish dish);

    Page<DishVO> list(DishPageQueryDTO dishPageQueryDTO);


    @Select("select * from dish where id=#{id}")
    Dish selectById(Long id);


    Long countEnableDishByIds(List<Long> ids);


    void deleteByIds(List<Long> ids);

    @AutoFill(OperationType.UPDATE)
    void updateById(Dish dish);
}
