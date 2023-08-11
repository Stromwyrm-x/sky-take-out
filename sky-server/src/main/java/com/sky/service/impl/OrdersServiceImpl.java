package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.gson.Gson;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrdersService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl implements OrdersService
{
    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Value("${sky.shop.address}")
    private String shopAddress;

    @Value("${sky.baidu.ak}")
    private String ak;

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private Gson gson;

    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO)
    {
        //0.1.查询地址信息，若为空，则抛出异常
        AddressBook addressBook = addressBookMapper.selectById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null)
        {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //0.2.检查用户的地址是否超出配送范围
        checkOutOfRange(addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());

        //0.3.查询当前用户的购物车信息，若为空，则抛出异常
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectByUserId(BaseContext.getCurrentId());
        if (CollectionUtils.isEmpty(shoppingCartList))
        {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //1.新增订单(userName要不要)
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setNumber(String.valueOf(System.nanoTime()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(BaseContext.getCurrentId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());

        ordersMapper.insert(orders);

        //2.新增订单明细
        List<OrderDetail> orderDetailList = shoppingCartList.stream().map(shoppingCart ->
        {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            return orderDetail;
        }).collect(Collectors.toList());

        orderDetailMapper.insertBatch(orderDetailList);

        //3.删除当前用户的购物车
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());

        //4.封装返回数据
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder().id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber()).build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception
    {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );

        //因为无法调用微信支付接口，所以模拟一下
        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID"))
        {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) throws IOException
    {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单号查询当前用户的订单
        Orders ordersDB = ordersMapper.getByNumberAndUserId(outTradeNo, userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        ordersMapper.update(orders);

        //来单提醒
        Map<String,Object>map=new HashMap<>();
        map.put("type", 1);//消息类型，1表示来单提醒
        map.put("orderId", orders.getId());
        map.put("content", "订单号：" + outTradeNo);
        webSocketServer.sendToAll(gson.toJson(map));

    }

    @Override
    public PageResult page(OrdersPageQueryDTO ordersPageQueryDTO)
    {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        Page<Orders> ordersPage = ordersMapper.list(ordersPageQueryDTO);
        List<OrderVO> orderVOList = ordersPage.stream().map(orders ->
        {
            List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            orderVO.setOrderDetailList(orderDetailList);
            return orderVO;
        }).collect(Collectors.toList());

        return new PageResult(ordersPage.getTotal(), orderVOList);
    }

    @Override
    public OrderVO getByIdWithOrderDetail(Long id)
    {
        //1.查询订单信息
        Orders orders = ordersMapper.getById(id);
        //2.查询订单明细信息
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        //3.封装
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    @Override
    public void cancelById(Long id)
    {
        Orders orders = ordersMapper.getById(id);
        //1.若商家已经接单，则无法取消
        if (orders.getStatus() > 2)
        {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //2.若在下单情况下，则需要退款
        if (orders.getStatus().equals(Orders.TO_BE_CONFIRMED))
        {
//            weChatPayUtil.refund(orders.getNumber(), //商户订单号
//                    orders.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));
            orders.setPayStatus(Orders.REFUND);
        }
        //3.更新订单状态
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason("用户取消订单");
        ordersMapper.update(orders);
    }

    @Override
    public void orderAgain(Long id)
    {
        //1.查询订单明细
        Orders orders = ordersMapper.getById(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        //2.添加到当前用户购物车
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(orderDetail ->
        {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO)
    {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        //管理端查询订单，应该查询所有订单
//        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        Page<Orders> ordersPage = ordersMapper.list(ordersPageQueryDTO);
        List<OrderVO> orderVOList = ordersPage.stream().map(orders ->
        {
            List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
            List<String> stringList = orderDetailList.stream().map(orderDetail ->
            {
                String orderDish = orderDetail.getName() + "*" + orderDetail.getNumber() + ";";
                return orderDish;
            }).collect(Collectors.toList());
            String orderDishes = String.join("", stringList);

            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            orderVO.setOrderDishes(orderDishes);

            return orderVO;
        }).collect(Collectors.toList());

        return new PageResult(ordersPage.getTotal(), orderVOList);
    }

    @Override
    public OrderStatisticsVO countByStatus()
    {
        //待接单数量
        Integer toBeConfirmed = ordersMapper.countByStatus(null,null,Orders.TO_BE_CONFIRMED);
        //待派送数量
        Integer confirmed = ordersMapper.countByStatus(null,null,Orders.CONFIRMED);
        //派送中数量
        Integer deliveryInProgress = ordersMapper.countByStatus(null,null,Orders.DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO = OrderStatisticsVO.builder()
                .toBeConfirmed(toBeConfirmed)
                .confirmed(confirmed)
                .deliveryInProgress(deliveryInProgress)
                .build();
        return orderStatisticsVO;
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO)
    {
        Orders orders = ordersMapper.getById(ordersCancelDTO.getId());
        //若在下单情况下，则需要退款
        if (orders.getStatus().equals(Orders.TO_BE_CONFIRMED))
        {
//            weChatPayUtil.refund(orders.getNumber(), //商户订单号
//                    orders.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));
            orders.setPayStatus(Orders.REFUND);
        }
        //3.更新订单状态
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        ordersMapper.update(orders);
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO)
    {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        ordersMapper.update(orders);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO)
    {
        Orders orders = ordersMapper.getById(ordersRejectionDTO.getId());
        //若在下单情况下，则需要退款
        if (orders.getStatus().equals(Orders.TO_BE_CONFIRMED))
        {
//            weChatPayUtil.refund(orders.getNumber(), //商户订单号
//                    orders.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));
            orders.setPayStatus(Orders.REFUND);
        }

        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orders.setStatus(Orders.CANCELLED);

        ordersMapper.update(orders);
    }

    @Override
    public void delivery(Long id)
    {
        Orders orders = ordersMapper.getById(id);
        //查看是否接单
        if (!orders.getStatus().equals(Orders.CONFIRMED))
        {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //deliveryTime代表送达时间
//        orders.setDeliveryTime(LocalDateTime.now());
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        ordersMapper.update(orders);
    }

    @Override
    public void complete(Long id)
    {
        Orders orders = ordersMapper.getById(id);
        //查看是否派送订单
        if (!orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS))
        {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        ordersMapper.update(orders);
    }

    @Override
    public void reminder(Long id) throws IOException
    {
        Orders orders = ordersMapper.getById(id);
        Map<String,Object>map=new HashMap<>();
        map.put("type", 2);//消息类型，2表示催单提醒
        map.put("orderId", orders.getId());
        map.put("content", "订单号：" + orders.getNumber());
        webSocketServer.sendToAll(gson.toJson(map));
    }

    /**
     * 检查客户的收货地址是否超出配送范围
     *
     * @param address
     */
    private void checkOutOfRange(String address)
    {
        Map map = new HashMap();
        map.put("address", shopAddress);
        map.put("output", "json");
        map.put("ak", ak);

        //获取店铺的经纬度坐标
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if (!jsonObject.getString("status").equals("0"))
        {
            throw new OrderBusinessException("店铺地址解析失败");
        }

        //数据解析
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        //店铺经纬度坐标
        String shopLngLat = lat + "," + lng;

        map.put("address", address);
        //获取用户收货地址的经纬度坐标
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        jsonObject = JSON.parseObject(userCoordinate);
        if (!jsonObject.getString("status").equals("0"))
        {
            throw new OrderBusinessException("收货地址解析失败");
        }

        //数据解析
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        //用户收货地址经纬度坐标
        String userLngLat = lat + "," + lng;

        map.put("origin", shopLngLat);
        map.put("destination", userLngLat);
        map.put("steps_info", "0");

        //路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);

        jsonObject = JSON.parseObject(json);
        if (!jsonObject.getString("status").equals("0"))
        {
            throw new OrderBusinessException("配送路线规划失败");
        }

        //数据解析
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if (distance > 5000)
        {
            //配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }
    }


}
