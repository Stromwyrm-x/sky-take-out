package com.sky.mapper;

import com.sky.dto.UserReportDTO;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper
{
    @Select("select * from user where openid=#{openid}")
    User selectByOpenid(String openid);

    @Options(useGeneratedKeys = true,keyProperty = "id")
    @Insert("insert into user (openid, name, phone, sex, id_number, avatar, create_time) " +
            "values (#{openid},#{name},#{phone},#{sex},#{idNumber},#{avatar},#{createTime})")
    void insert(User user);

    @Select("select * from user where id=#{userId}")
    User getById(Long userId);

    List<UserReportDTO> selectNewUserList(LocalDateTime beginTime, LocalDateTime endTime);

    @Select("select count(*) from user where create_time < #{beginTime}")
    Integer countTotalByCreateTime(LocalDateTime beginTime);
}
