package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.Dto.SetmealDto;
import com.itheima.reggie.entity.SetmealDish;

public interface SetmealDishService extends IService<SetmealDish> {
    public void savaList(SetmealDto setmealDto);
}
