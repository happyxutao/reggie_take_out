package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.Dto.DishDto;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {
    @Autowired
    DishService dishService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    DishFlavorService dishFlavorService;
    @Autowired
    RedisTemplate redisTemplate;
    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize,String name)
    {
        Page<Dish> dishPage = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<Dish>();
        dishLambdaQueryWrapper.eq(name!=null,Dish::getName,name);
        dishLambdaQueryWrapper.orderByDesc(Dish::getCreateTime);
        dishService.page(dishPage,dishLambdaQueryWrapper);
        //将除了records的属性先copy，records要单独处理
        BeanUtils.copyProperties(dishPage,dishDtoPage,"records");
        List<Dish> records = dishPage.getRecords();
        List<DishDto> dishes = new ArrayList<>();
        for (Dish dish:records){
            //先将除了id的copy
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish,dishDto,"categoryId");
            Long id = dish.getCategoryId();
            Category category = categoryService.getById(id);
            dishDto.setCategoryName(category.getName());
            dishes.add(dishDto);
        }
        dishDtoPage.setRecords(dishes);
        return R.success(dishDtoPage);
    }
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);
        //清除该类的缓存
        String key="dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
        return R.success("添加成功");
    }
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
       dishService.updateWithFlavor(dishDto);
        //清除该类的缓存
        String key="dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
       return R.success("修改成功");
    }
    @DeleteMapping
    public R<String> delete(String ids){
        //将ids根据，隔开
        String[] dishIds = ids.split(",");
        dishService.removeByIds(Arrays.asList(dishIds));
        Set keys = redisTemplate.keys("dish_");
        redisTemplate.delete(keys);
        return R.success("删除成功");
    }
    @PostMapping("/status/{flag}")
    public R<String> update(@PathVariable Integer flag,String ids){
        String[] splits = ids.split(",");
        //得到每一个菜，改变其状态
        for(String id:splits){
            Dish dish = dishService.getById(id);
            dish.setStatus(flag);
            dishService.updateById(dish);
        }
        Set keys = redisTemplate.keys("dish_");
        redisTemplate.delete(keys);
        return R.success("修改成功");
    }
//    @GetMapping("/list")
//    public R<List<Dish>> getList(Long categoryId){
//        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
//        dishLambdaQueryWrapper.eq(categoryId!=null,Dish::getCategoryId,categoryId);
//        dishLambdaQueryWrapper.orderByDesc(Dish::getCreateTime);
//        List<Dish> list = dishService.list(dishLambdaQueryWrapper);
//        return R.success(list);
//    }
      @GetMapping("/list")
      public R<List<DishDto>> getList(Long categoryId,int status){
        //先看看缓存里有无该信息
          String key="dish_"+categoryId+"_1";
          List<DishDto> key1 =(List<DishDto>) redisTemplate.opsForValue().get(key);
          if(key1!=null){
              //找到了将结果返回
              return R.success(key1);
          }
          LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
          dishLambdaQueryWrapper.eq(categoryId!=null,Dish::getCategoryId,categoryId);
          dishLambdaQueryWrapper.orderByDesc(Dish::getCreateTime);
          List<Dish> list = dishService.list(dishLambdaQueryWrapper);
          ArrayList<DishDto> dishDtos = new ArrayList<>();
          //将dish转为dishDto
          for(Dish dish:list){
              DishDto dishDto = new DishDto();
              BeanUtils.copyProperties(dish,dishDto);
              //加入口味信息
              Long id = dish.getId();
              LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
              dishFlavorLambdaQueryWrapper.eq(id!=null,DishFlavor::getDishId,id);
              List<DishFlavor> list1 = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
              dishDto.setFlavors(list1);
              //加入到集合中
              dishDtos.add(dishDto);
          }
          //将数据加入缓存中
          redisTemplate.opsForValue().set(key,dishDtos);
          return R.success(dishDtos);
      }
    }
