package com.sky.service.impl;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class UserServiceImpl implements UserService
{
    @Autowired
    private Gson gson;

    public static final String WX_LOGIN_URL="https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(UserLoginDTO userLoginDTO)
    {
        String code = userLoginDTO.getCode();
        //1.调用微信接口，实现登录操作
        Map<String,String>map=new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        String result = HttpClientUtil.doGet(WX_LOGIN_URL, map);

        //2.获取openid
        JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
        JsonElement jsonElement = jsonObject.get("openid");
        String openid = jsonElement.getAsString();
        log.info("openid:{}",openid);
        if (!StringUtils.hasLength(openid))
        {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //3.若用户是第一次访问小程序，则需要注册
        User user=userMapper.selectByOpenid(openid);
        if (ObjectUtils.isEmpty(user))
        {
            user= User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        //4.返回用户信息
        return user;
    }
}
