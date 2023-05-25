package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.Dto.DishDto;
import com.itheima.reggie.Dto.SetmealDto;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    SetmealDishService setmealDishService;
    @Autowired
    SetmealService setmealService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    DishService dishService;

    @CacheEvict(value = "setmealCache",allEntries = true)
    @PostMapping
    public R<String> addSetmeal(@RequestBody SetmealDto setmealDto) {
        //先将基本信息保存下来
        setmealService.save(setmealDto);
        setmealDishService.savaList(setmealDto);
        return R.success("添加成功");
    }
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page,int pageSize,String name){
        Page<Setmeal> pageInfo= new Page(page, pageSize);
        Page<SetmealDto> page1 = new Page(page, pageSize);
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(name!=null,Setmeal::getName,name);
        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getCreateTime);
        setmealService.page(pageInfo,setmealLambdaQueryWrapper);
        //除了records其他都拷贝
        BeanUtils.copyProperties(pageInfo,page,"records");
        List<Setmeal> records = pageInfo.getRecords();
        ArrayList<SetmealDto> setmealDtos = new ArrayList<>();
        for(Setmeal setmeal:records){
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(setmeal,setmealDto,"categoryId");
            Long categoryId = setmeal.getCategoryId();
            Category byId = categoryService.getById(categoryId);
            setmealDto.setCategoryName(byId.getName());
            setmealDtos.add(setmealDto);
        }
        page1.setRecords(setmealDtos);
        return R.success(page1);
    }
    @CacheEvict(value = "setmealCache",allEntries = true)
    @DeleteMapping
    public R<String> delete(String ids){
       setmealService.deleteWithSetmealDish(ids);
        return R.success("删除成功");
    }
    @CacheEvict(value = "setmealCache",allEntries = true)
    @PostMapping("/status/{flag}")
    public R<String> update(@PathVariable Integer flag,String ids){
        String[] splits = ids.split(",");
        //得到每一个菜，改变其状态
        for(String id:splits){
            Setmeal setmeal = setmealService.getById(id);
            setmeal.setStatus(flag);
            setmealService.updateById(setmeal);
        }
        return R.success("修改成功");
    }
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        Long categoryId = setmeal.getCategoryId();
        Integer status = setmeal.getStatus();
        LambdaQueryWrapper<Setmeal> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(categoryId!=null,Setmeal::getCategoryId,categoryId);
        dishLambdaQueryWrapper.eq(status!=null,Setmeal::getStatus,status);
        List<Setmeal> list = setmealService.list(dishLambdaQueryWrapper);
        return R.success(list);
    }
}
