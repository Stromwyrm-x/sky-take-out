package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderTask
{
    @Autowired
    private OrdersMapper ordersMapper;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void processTimeoutOrder()
    {
        //1.查询待付款并且超时的订单
        LocalDateTime beforeTime = LocalDateTime.now().minusMinutes(15);
        List<Orders>ordersList=ordersMapper.selectByStatusAndTime(Orders.PENDING_PAYMENT,beforeTime);

        //2.取消订单
        if (!CollectionUtils.isEmpty(ordersList))
        {
            ordersList.stream().forEach(orders -> {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelTime(LocalDateTime.now());
                orders.setCancelReason("支付超时，自动取消");
                ordersMapper.update(orders);
            });
        }

    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder()
    {
        //1.查询派送中并且在12点之前的订单
        LocalDateTime beforeTime = LocalDateTime.now().minusHours(1);
        List<Orders> ordersList = ordersMapper.selectByStatusAndTime(Orders.DELIVERY_IN_PROGRESS, beforeTime);

        //2.完成订单
        if (!CollectionUtils.isEmpty(ordersList))
        {
            ordersList.stream().forEach(orders -> {
                orders.setStatus(Orders.COMPLETED);
                orders.setDeliveryTime(LocalDateTime.now());
                ordersMapper.update(orders);
            });
        }


    }

}
