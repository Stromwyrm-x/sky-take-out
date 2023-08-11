package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersReportDTO;
import com.sky.dto.TurnoverReportDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrdersMapper
{
    void insert(Orders orders);
    /**
     * 根据订单号和用户id查询订单
     * @param orderNumber
     * @param userId
     */
    @Select("select * from orders where number = #{orderNumber} and user_id= #{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 先查询该用户的历史订单
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> list(OrdersPageQueryDTO ordersPageQueryDTO);


    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    Integer countByStatus(LocalDateTime beginTime, LocalDateTime endTime, Integer status);

    @Select("select * from orders where status=#{status} and order_time < #{beforeTime}")
    List<Orders> selectByStatusAndTime(Integer status, LocalDateTime beforeTime);

    List<TurnoverReportDTO> selectTurnoverStatistics(LocalDateTime beginTime, LocalDateTime endTime, Integer status);

    List<OrdersReportDTO> selectOrderStatistics(LocalDateTime beginTime, LocalDateTime endTime, Integer status);

    List<GoodsSalesDTO> selectTop10(LocalDateTime beginTime, LocalDateTime endTime, Integer status);
}
