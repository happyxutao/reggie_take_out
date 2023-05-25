package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>implements CategoryService {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private DishService dishService;
    @Override
    public void remove(long id) {
       //看看套餐表里有没有该分类
        LambdaQueryWrapper<Setmeal> Wrapper1 = new LambdaQueryWrapper<>();
        Wrapper1.eq(Setmeal::getCategoryId,id);
        int count = setmealService.count(Wrapper1);
        if(count>0){
           throw new CustomException("当前分类关联了套餐,不能删除");
        }
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<Dish>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count1 = dishService.count(dishLambdaQueryWrapper);
        if(count1>0){
            //抛出异常
            throw new CustomException("当前分类关联了菜品,不能删除");
        }
       super.removeById(id);
    }
}
