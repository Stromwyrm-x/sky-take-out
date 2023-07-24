package com.sky.controller.admin;

import com.github.pagehelper.PageHelper;
import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController
{
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO)
    {
        log.info("员工登录,{}",employeeLoginDTO);
        Employee employee = employeeService.login(employeeLoginDTO);
        //登录成功，生成jwt令牌
        Map<String,Object>claims=new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID,employee.getId());
        String jwt = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);
        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(jwt)
                .build();

        return Result.success(employeeLoginVO);
    }

    @PostMapping
    public Result add(@RequestBody EmployeeDTO employeeDTO)
    {
        employeeService.add(employeeDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult>page(EmployeePageQueryDTO employeePageQueryDTO)
    {
        PageResult pageResult=employeeService.page(employeePageQueryDTO);
        return Result.success(pageResult);
    }


    @GetMapping("/{id}")
    public Result<Employee>getById(@PathVariable Long id)
    {
        Employee employee=employeeService.getById(id);
        return Result.success(employee);
    }

    @PostMapping("/status/{status}")
    public Result changeStatusById(@PathVariable Integer status,Long id)
    {
        employeeService.changeStatus(status,id);
        return Result.success();
    }

    @PutMapping
    public Result updateById(@RequestBody EmployeeDTO employeeDTO)
    {
        employeeService.updateById(employeeDTO);
        return Result.success();
    }

//    @PostMapping("/logout")
//    public Result logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
//    {
//        log.info("token为：{}",httpServletRequest.getHeader("token"));
//        //清理jwt的token
//        httpServletResponse.setHeader("token","fuck");
//        log.info("token为：{}",httpServletRequest.getHeader("token"));
//        return Result.success();
//    }

}
