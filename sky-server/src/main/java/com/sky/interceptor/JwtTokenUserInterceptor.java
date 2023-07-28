package com.sky.interceptor;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;


/**
 * 小程序端的jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;


    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.从请求头中获取令牌
        String jwt = request.getHeader(jwtProperties.getUserTokenName());
        //2.判断令牌是否存在，若不存在，则返回401
        if (!StringUtils.hasLength(jwt))
        {
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return false;
        }
        //3.解析令牌，若失败，则返回401
        try
        {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), jwt);
            Long userId = Long.parseLong(claims.get(JwtClaimsConstant.USER_ID).toString());
            BaseContext.setCurrentId(userId);
        } catch (Exception e)
        {
            e.printStackTrace();
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return false;
        }
        //4.放行
        return true;


    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception
    {
        BaseContext.removeCurrentId();
    }
}
