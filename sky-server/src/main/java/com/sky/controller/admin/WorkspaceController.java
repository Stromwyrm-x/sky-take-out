package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.apache.ibatis.annotations.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/admin/workspace")
public class WorkspaceController
{
    @Autowired
    private WorkspaceService workspaceService;

    @GetMapping("/businessData")
    public Result<BusinessDataVO> businessData()
    {
        //获取当天的开始时间
        LocalDateTime beginTime = LocalDateTime.now().with(LocalTime.MIN);
        //获取当天的结束时间
        LocalDateTime endTime = LocalDateTime.now().with(LocalTime.MAX);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(beginTime, endTime);
        return Result.success(businessDataVO);
    }

    @GetMapping("/overviewOrders")
    public Result<OrderOverViewVO> overviewOrders()
    {
        //获取当天的开始时间
        LocalDateTime beginTime = LocalDateTime.now().with(LocalTime.MIN);
        //获取当天的结束时间
        LocalDateTime endTime = LocalDateTime.now().with(LocalTime.MAX);
        OrderOverViewVO orderOverViewVO = workspaceService.getOverviewOrders(beginTime, endTime);
        return Result.success(orderOverViewVO);
    }

    @GetMapping("/overviewDishes")
    public Result<DishOverViewVO> overviewDishes()
    {
        DishOverViewVO dishOverViewVO = workspaceService.getOverviewDishes();
        return Result.success(dishOverViewVO);
    }
    @GetMapping("/overviewSetmeals")
    public Result<SetmealOverViewVO> overviewSetmeals()
    {
        SetmealOverViewVO setmealOverViewVO = workspaceService.getOverviewSetmeals();
        return Result.success(setmealOverViewVO);
    }

}
