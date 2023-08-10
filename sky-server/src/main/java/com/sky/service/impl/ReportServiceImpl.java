package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersReportDTO;
import com.sky.dto.TurnoverReportDTO;
import com.sky.dto.UserReportDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService
{
    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end)
    {
        //1.生成dateList
        List<String> dateList = getDateList(begin, end);

        //2.生成turnoverList
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        //2.1生成TurnoverReportDTO
        List<TurnoverReportDTO> turnoverReportDTOList = ordersMapper.selectTurnoverStatistics(beginTime, endTime, Orders.COMPLETED);
        //2.2生成map(查询出来的是一定有销售额的日期，还有日期如果没有销售额，则还需要赋值为0)
        Map<String, BigDecimal> map = turnoverReportDTOList.stream().collect(Collectors.toMap(TurnoverReportDTO::getOrderDate, TurnoverReportDTO::getOrderMoney));
        //2.3生成turnoverList
        List<BigDecimal> turnoverList = dateList.stream().map(date ->
        {
            return map.get(date) == null ? new BigDecimal(0) : map.get(date);
        }).collect(Collectors.toList());

        //3.封装返回结果
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        turnoverReportVO.setDateList(String.join(",", dateList));
        turnoverReportVO.setTurnoverList(StringUtils.join(turnoverList, ","));

        return turnoverReportVO;
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end)
    {
        //1.生成dateList
        List<String> dateList = getDateList(begin, end);

        //2.生成newUserList
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        //2.1查询原始的数据
        List<UserReportDTO> userReportDTOList = userMapper.selectNewUserList(beginTime, endTime);
        //2.2转换为map
        Map<String, Integer> map = userReportDTOList.stream().collect(Collectors.toMap(UserReportDTO::getCreateDate, UserReportDTO::getUserCount));
        //2.3生成newUserList
        List<Integer> newUserList = dateList.stream().map(date -> map.get(date) == null ? 0 : map.get(date)).collect(Collectors.toList());

        //3.生成totalUserList
        //3.1查询在beginTime以前的用户总数
        Integer baseCount = userMapper.countTotalByCreateTime(beginTime);
        //3.2再添加每一天新增的用户数
        List<Integer> totalUserList = new ArrayList<>();
        for (Integer add : newUserList)
        {
            baseCount += add;
            totalUserList.add(baseCount);
        }
//        newUserList.forEach(integer -> {
//            baseCount+=integer;   //为什么lambda表达式中只能用final
//            totalUserList.add(baseCount);
//        });

        //4.封装userReportVO
        UserReportVO userReportVO = new UserReportVO();
        userReportVO.setDateList(StringUtils.join(dateList, ","));
        userReportVO.setNewUserList(StringUtils.join(newUserList, ","));
        userReportVO.setTotalUserList(StringUtils.join(totalUserList, ","));

        return userReportVO;
    }

    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end)
    {
        //1.获取日期
        List<String> dateList = getDateList(begin, end);

        //2.获取每日订单数
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<OrdersReportDTO> ordersReportDTOList = ordersMapper.selectOrderStatistics(beginTime, endTime, null);
        Map<String, Integer> orderMap = ordersReportDTOList.stream().collect(Collectors.toMap(OrdersReportDTO::getOrderDate, OrdersReportDTO::getOrderCount));
        List<Integer> orderCountList = dateList.stream().map(date -> orderMap.get(date) == null ? 0 : orderMap.get(date)).collect(Collectors.toList());

        //3.获取每日有效订单数
        List<OrdersReportDTO> validOrdersReportDTOList = ordersMapper.selectOrderStatistics(beginTime, endTime, Orders.COMPLETED);
        Map<String, Integer> validOrderMap = validOrdersReportDTOList.stream().collect(Collectors.toMap(OrdersReportDTO::getOrderDate, OrdersReportDTO::getOrderCount));
        List<Integer> validOrderCountList = dateList.stream().map(date -> validOrderMap.get(date) == null ? 0 : validOrderMap.get(date)).collect(Collectors.toList());

        //4.获取订单总数、有效订单总数、订单完成率
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        Double orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;

        //5.封装返回结果
        OrderReportVO orderReportVO = OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();

        return orderReportVO;
    }

    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end)
    {
        //1.获取nameList
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO>goodsSalesDTOList=ordersMapper.selectTop10(beginTime,endTime,Orders.COMPLETED);

        List<String> nameList = goodsSalesDTOList.stream().map(goodsSalesDTO ->
        {
            return goodsSalesDTO.getName();
        }).collect(Collectors.toList());

        //2.获取numberList
        List<Integer> numberList = goodsSalesDTOList.stream().map(goodsSalesDTO ->
        {
            return goodsSalesDTO.getNumber();
        }).collect(Collectors.toList());

        //3.封装结果
        SalesTop10ReportVO salesTop10ReportVO = SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
        return salesTop10ReportVO;
    }

    private List<String> getDateList(LocalDate begin, LocalDate end)
    {
        List<LocalDate> localDateList = begin.datesUntil(end.plusDays(1)).collect(Collectors.toList());
        List<String> dateList = localDateList.stream().map(localDate ->
        {
            return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }).collect(Collectors.toList());
        return dateList;
    }
}
