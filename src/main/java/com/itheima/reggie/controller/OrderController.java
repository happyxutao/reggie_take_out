package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.Dto.OrderDto;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    OrdersService ordersService;
    @Autowired
    OrderDetailService orderDetailService;
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        ordersService.submit(orders);
        return R.success("订单上传成功");
    }
    @GetMapping("/userPage")
    public R<Page> getUserPage(Integer page,Integer pageSize){
        //分页构造器对象
        Page<Orders> pageInfo=new Page<Orders>(page,pageSize);
        Page<OrderDto> pageDto=new Page<OrderDto>(page,pageSize);
        //构造条件查修对象
        LambdaQueryWrapper<Orders> query = new LambdaQueryWrapper<Orders>();
        query.eq(Orders::getUserId, BaseContext.getCurrentId());
        //按更新时间降序排列
        query.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo,query);
        //通过OrderId查询对应的OrderDetail
        //对OrderDto进行需要的属性赋值
        List<Orders> records=pageInfo.getRecords();
        //收集每个订单的订单明细
        List<OrderDto> orderDtoList=records.stream().map((item)->{
            OrderDto orderDto=new OrderDto();
            //获取订单id
            Long orderId=item.getId();
            List<OrderDetail> orderDetailList=this.getOrderDetailListByOrderId(orderId);
            BeanUtils.copyProperties(item,orderDto);
            orderDto.setOrderDetailList(orderDetailList);
            orderDto.setSumNum(0);
            return orderDto;
        }).collect(Collectors.toList());
        BeanUtils.copyProperties(pageInfo,pageDto,"records");
        pageDto.setRecords(orderDtoList);
        return R.success(pageDto);
    }

    /**
     * 通过用户Id找到该订单，根据订单找到订单明细
     * @param orderId
     * @return
     */
    public List<OrderDetail> getOrderDetailListByOrderId(Long orderId){
        LambdaQueryWrapper<OrderDetail> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId,orderId);
        List<OrderDetail> orderDetailList=orderDetailService.list(queryWrapper);
        return orderDetailList;
    }
}
