package com.sky.service.impl;

import com.sky.dto.OrdersReportDTO;
import com.sky.dto.TurnoverReportDTO;
import com.sky.dto.UserReportDTO;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkspaceServiceImpl implements WorkspaceService
{
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public BusinessDataVO getBusinessData(LocalDateTime beginTime, LocalDateTime endTime)
    {
        List<TurnoverReportDTO> turnoverReportDTOList = ordersMapper.selectTurnoverStatistics(beginTime, endTime, Orders.COMPLETED);
        Double turnover = CollectionUtils.isEmpty(turnoverReportDTOList) ? 0.0 : turnoverReportDTOList.get(0).getOrderMoney().doubleValue();

        List<OrdersReportDTO> validOrdersReportDTOList = ordersMapper.selectOrderStatistics(beginTime, endTime, Orders.COMPLETED);
        Integer validOrderCount = CollectionUtils.isEmpty(validOrdersReportDTOList) ? 0 : validOrdersReportDTOList.get(0).getOrderCount();

        List<OrdersReportDTO> ordersReportDTOList = ordersMapper.selectOrderStatistics(beginTime, endTime, null);
        Integer totalOrderCount = CollectionUtils.isEmpty(ordersReportDTOList) ? 0 : ordersReportDTOList.get(0).getOrderCount();

        Double orderCompletionRate = 0.0;
        Double unitPrice = 0.0;
        if (validOrderCount != 0 && totalOrderCount != 0 && turnover != 0)
        {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
            unitPrice = turnover / validOrderCount;
        }

        List<UserReportDTO> userReportDTOList = userMapper.selectNewUserList(beginTime, endTime);

        Integer newUsers = CollectionUtils.isEmpty(userReportDTOList) ? 0 : userReportDTOList.get(0).getUserCount();

        BusinessDataVO businessDataVO = BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();

        return businessDataVO;
    }

    @Override
    public OrderOverViewVO getOverviewOrders(LocalDateTime beginTime, LocalDateTime endTime)
    {
        //待接单数量
        Integer waitingOrders = ordersMapper.countByStatus(beginTime, endTime, Orders.TO_BE_CONFIRMED);

        //待派送数量
        Integer deliveredOrders = ordersMapper.countByStatus(beginTime, endTime, Orders.CONFIRMED);

        //已完成数量
        Integer completedOrders = ordersMapper.countByStatus(beginTime, endTime, Orders.COMPLETED);

        //已取消数量
        Integer cancelledOrders = ordersMapper.countByStatus(beginTime, endTime, Orders.CANCELLED);

        //全部订单
        Integer allOrders = ordersMapper.countByStatus(beginTime, endTime, null);

        return new OrderOverViewVO(waitingOrders, deliveredOrders, completedOrders, cancelledOrders, allOrders);
    }

    @Override
    public DishOverViewVO getOverviewDishes()
    {
        // 已启售数量
        Integer sold = dishMapper.countByStatus(1);
        // 已停售数量
        Integer discontinued = dishMapper.countByStatus(0);

        DishOverViewVO dishOverViewVO = DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
        return dishOverViewVO;
    }

    @Override
    public SetmealOverViewVO getOverviewSetmeals()
    {
        // 已启售数量
        Integer sold = setmealMapper.countByStatus(1);
        // 已停售数量
        Integer discontinued = setmealMapper.countByStatus(0);

        SetmealOverViewVO setmealOverViewVO = SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
        return setmealOverViewVO;
    }


}
