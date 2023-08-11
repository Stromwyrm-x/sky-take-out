package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

import java.time.LocalDateTime;

public interface WorkspaceService
{
    BusinessDataVO getBusinessData(LocalDateTime beginTime, LocalDateTime endTime);

    OrderOverViewVO getOverviewOrders(LocalDateTime beginTime, LocalDateTime endTime);

    DishOverViewVO getOverviewDishes();

    SetmealOverViewVO getOverviewSetmeals();
}
