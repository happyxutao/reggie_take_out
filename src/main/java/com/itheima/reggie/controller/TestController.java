package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.LunBo;
import com.itheima.reggie.service.LunBoService;
import com.sun.org.apache.bcel.internal.generic.ACONST_NULL;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class TestController {
    @Autowired
    LunBoService lunBoService;

    @GetMapping("/getlunbo")
    public R<List<String>> getLunBo(){
        System.out.println("haha");
        List<LunBo> list = lunBoService.list();
        List<String> strings = new ArrayList<String>();
        //将list里所有的url取出来
        for(LunBo item : list){
            strings.add(item.getUrl());
            log.info(item.getUrl());
            log.info("获取数据");
            System.out.println("haha");
        }
        //将得到的url返回
        return R.success(strings);
    }

}
