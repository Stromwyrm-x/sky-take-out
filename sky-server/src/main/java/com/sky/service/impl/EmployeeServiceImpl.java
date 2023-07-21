package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.BaseException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class EmployeeServiceImpl implements EmployeeService
{

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO)
    {
        //0.将传入的密码进行md5加密
        String password = DigestUtils.md5DigestAsHex(employeeLoginDTO.getPassword().getBytes());
        String username = employeeLoginDTO.getUsername();

        ValueOperations opsForValue = redisTemplate.opsForValue();
        //密码5分钟内输入超过5次，则锁定账号
        if (!ObjectUtils.isEmpty(opsForValue.get("lock:"+username)))
        {
            throw new AccountLockedException(MessageConstant.LOGIN_LOCK);
        }

        //1.根据用户名查找员工
        Employee employee = employeeMapper.selectByUsername(employeeLoginDTO.getUsername());
        //2.如果没找到，则返回异常信息
        if (employee==null)
        {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        //3.密码进行比对
        if (!password.equals(employee.getPassword()))
        {
            //3.1记录密码错误到redis中
            opsForValue.set(getKey(username),"-",5, TimeUnit.MINUTES);
            //3.2统计该员工的密码错误标记是否满5次
            if (Objects.requireNonNull(redisTemplate.keys("login:" + username + ":*")).size()>=5)
            {
                opsForValue.set("lock:"+username,"-",1,TimeUnit.HOURS);
                throw new AccountLockedException(MessageConstant.LOGIN_LOCK);
            }
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        //4.判断账号是否锁定
        if (Objects.equals(employee.getStatus(), StatusConstant.DISABLE))
        {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }
        //5.所有条件都满足，返回员工
        return employee;

    }

    @Override
    public void add(EmployeeDTO employeeDTO)
    {
        Employee employee=new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        employee.setStatus(StatusConstant.ENABLE);
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.insert(employee);
    }

    @Override
    public PageResult page(EmployeePageQueryDTO employeePageQueryDTO)
    {
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        Page<Employee>employeePage=employeeMapper.list(employeePageQueryDTO);
        return new PageResult(employeePage.getTotal(),employeePage.getResult());
    }

    @Override
    public void changeStatus(Integer status, Long id)
    {
        Employee employee = Employee.builder()
                .id(id)
                .status(status)
                .updateUser(BaseContext.getCurrentId())
                .updateTime(LocalDateTime.now())
                .build();
        employeeMapper.update(employee);
    }

    @Override
    public Employee getById(Long id)
    {
        Employee employee=employeeMapper.selectById(id);
        return employee;
    }

    @Override
    public void updateById(EmployeeDTO employeeDTO)
    {
        Employee employee=new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        employee.setUpdateUser(BaseContext.getCurrentId());
        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.update(employee);
    }

    private static String getKey(String username)
    {
        return "login:" + username + ":" + UUID.randomUUID();
    }



}
