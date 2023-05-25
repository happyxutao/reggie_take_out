package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.Dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service

public class DishServiceImpl extends ServiceImpl<DishMapper, Dish>implements DishService {
    @Autowired
    DishFlavorService dishFlavorService;

    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto){
        this.save(dishDto);
        Long id = dishDto.getId();
        /**
         * 得到口味的数据
         */
        List<DishFlavor> flavors = dishDto.getFlavors();
        /**
         * 遍历每一个口味，为其赋值
         */
        for(DishFlavor flavor:flavors){
            flavor.setDishId(id);
        }
        dishFlavorService.saveBatch(flavors);
    }
    @Override
    @Transactional
    public DishDto getByIdWithFlavor(Long id){
        //先查基本信息
        DishDto dishDto = new DishDto();
        Dish dish = this.getById(id);
        BeanUtils.copyProperties(dish,dishDto);
        //将口味信息存入dishDto中
        LambdaQueryWrapper<DishFlavor> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(id!=null,DishFlavor::getDishId,id);
        List<DishFlavor> list = dishFlavorService.list(dishLambdaQueryWrapper);
        dishDto.setFlavors(list);
        return dishDto;
    }
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto){
        //先将基本信息更新
       this.updateById(dishDto);
       //将原来的口味信息删除
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.eq(dishDto.getId()!=null,DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(dishFlavorLambdaQueryWrapper);
        //添加现有口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        for(DishFlavor flavor:flavors){
            flavor.setDishId(dishDto.getId());
        }
        dishFlavorService.saveBatch(flavors);
    }
}
