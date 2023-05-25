package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * httpRequest 用于存入用户信息
     * employee  用于接受json数据，如password，ID
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        String password = employee.getPassword();
        String pas = DigestUtils.md5DigestAsHex(password.getBytes());
        String name = employee.getUsername();
        LambdaQueryWrapper<Employee> wrapper=new LambdaQueryWrapper<Employee>();
        wrapper.eq(Employee::getUsername,name);
        Employee emp = employeeService.getOne(wrapper);
        if(emp==null){
            return R.error("登入失败");
        }
        if(!emp.getPassword().equals(pas)){
            return R.error("登入失败");
        }
        if(emp.getStatus()==0){
            return R.error("账号禁用");
        }
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        /**
         * 将session里的数据清除
         * 返回R对象
         */
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
//        log.info("得到的员工信息为：{}",employee.toString());
        /**
         * 设置初始密码为123456
         * 并使用MD5加密
         */
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser((long)request.getSession().getAttribute("employee"));
//        employee.setCreateUser((long)request.getSession().getAttribute("employee"));
        employeeService.save(employee);
        return R.success("保存成功");
    }
    @GetMapping("/page")
    public R<Page> page(Integer page,Integer pageSize,String name){
        Page pageInfo=new Page(page,pageSize);
        LambdaQueryWrapper<Employee> wrapper=new LambdaQueryWrapper<Employee>();
        wrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        wrapper.orderByDesc(Employee::getUpdateTime);
        employeeService.page(pageInfo,wrapper);
        return R.success(pageInfo);
    }
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        Object employee1 = request.getSession().getAttribute("employee");
        long empId=(long)employee1;//得到操作对象
//        employee.setUpdateUser(empId);//设置更新人
//        employee.setUpdateTime(LocalDateTime.now());//设置更新时间
        employeeService.updateById(employee);
        return R.success("更新成功");
    }
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        Employee employee=employeeService.getById(id);
        if(employee!=null){
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }


}
