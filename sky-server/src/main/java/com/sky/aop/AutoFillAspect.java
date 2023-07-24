package com.sky.aop;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import com.sky.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect
{
    @Around("execution(* com.sky.mapper.*.*(..)) && @annotation(autoFill)")
    public void autoFill(ProceedingJoinPoint proceedingJoinPoint, AutoFill autoFill) throws Throwable
    {
        //1.获取原始方法运行时传入的参数
        Object[] args = proceedingJoinPoint.getArgs();
        if (ObjectUtils.isEmpty(args))
        {
            return;
        }
        Object entity=args[0];

        //2.通过反射获取到对象方法的公共属性
        Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
        Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
        Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
        Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
        //3.获取注解对应的value属性
        OperationType operationType = autoFill.value();
        //4.为公共字段赋值
        if(operationType == OperationType.INSERT)
        {
            //通过反射为对象属性赋值
            setCreateTime.invoke(entity,LocalDateTime.now());
            setCreateUser.invoke(entity,BaseContext.getCurrentId());
            setUpdateTime.invoke(entity,LocalDateTime.now());
            setUpdateUser.invoke(entity,BaseContext.getCurrentId());

        }
        else if(operationType == OperationType.UPDATE)
        {
            //通过反射为对象属性赋值
            setUpdateTime.invoke(entity,LocalDateTime.now());
            setUpdateUser.invoke(entity,BaseContext.getCurrentId());
        }
        proceedingJoinPoint.proceed();
    }

}
