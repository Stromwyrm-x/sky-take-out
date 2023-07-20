package com.sky.handler;

import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 数据库重复异常
     * @param duplicateKeyException
     * @return
     */
    @ExceptionHandler
    public Result duplicateKeyException(DuplicateKeyException duplicateKeyException)
    {
        String message = duplicateKeyException.getCause().getMessage();
        if (StringUtils.hasLength(message))
        {
            String[] split = message.split(" ");
            return Result.error(split[2]+"已存在！");
        }
        return Result.error("未知错误！");
    }

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result baseExceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler
    public Result exceptionHandler(Exception ex)
    {
        return Result.error("未知错误！");
    }
}
