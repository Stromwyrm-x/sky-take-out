package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper
{
    @Select("select count(*)from setmeal where category_id=#{id}")
    Integer countByCategoryId(Long id);

    @AutoFill(OperationType.INSERT)
    @Options(useGeneratedKeys = true,keyProperty = "id")
    @Insert("insert into setmeal (category_id, name, price, description, image, create_time, update_time, create_user, update_user)" +
            " values (#{categoryId},#{name},#{price},#{description},#{image},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Setmeal setmeal);

    Page<SetmealVO> list(SetmealPageQueryDTO setmealPageQueryDTO);


    Long countEnableSetmealByIds(List<Long> ids);

    void deleteByIds(List<Long> ids);

    @Select("select * from setmeal where id=#{id}")
    Setmeal selectById(Long id);

    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    @Select("select * from setmeal where category_id=#{categoryId} and status=1")
    List<Setmeal> selectByCategoryId(Long categoryId);

    @Select("select count(*) from setmeal where status=#{status}")
    Integer countByStatus(Integer status);
}
