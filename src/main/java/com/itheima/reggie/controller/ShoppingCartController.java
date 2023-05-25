package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.Dto.DishDto;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    SetmealService setmealService;
    @Autowired
    DishService dishService;
    @Autowired
    DishFlavorService dishFlavorService;
    @Autowired
    ShoppingCartService shoppingCartService;
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        /**
         * 将用户的购物车展示出来
         */
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,currentId);
        shoppingCartLambdaQueryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(shoppingCartLambdaQueryWrapper);
        return R.success(list);
    }
    @PostMapping("/add")
    public R<String> add(@RequestBody ShoppingCart shoppingCart){

        //如果是套餐，加入购物车表单
        if(shoppingCart.getDishId()==null){
            Long setmealId = shoppingCart.getSetmealId();
            //先表里看看，该商品有没有在表里，有的话将它的个数加一，没有的话，创建新的商品
            LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getSetmealId,setmealId);
            ShoppingCart byId1 = shoppingCartService.getOne(shoppingCartLambdaQueryWrapper);
            if(byId1!=null){
                //已经存在该商品
                //得到原来的商品数量
                Integer number = byId1.getNumber();
                number+=1;
                LambdaUpdateWrapper<ShoppingCart> shoppingCartLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                //将它的创建时间和商品数量修改
                shoppingCartLambdaUpdateWrapper.eq(ShoppingCart::getSetmealId,setmealId);
                shoppingCartLambdaUpdateWrapper.set(ShoppingCart::getNumber,number);
                shoppingCartLambdaUpdateWrapper.set(ShoppingCart::getCreateTime, LocalDateTime.now());
                shoppingCartService.update(shoppingCartLambdaUpdateWrapper);
            }else {
                //在表里找到改套餐
                Setmeal byId = setmealService.getById(setmealId);
                shoppingCart.setName(byId.getName());
                shoppingCart.setImage(byId.getImage());
                //添加用户ID
                Long currentId = BaseContext.getCurrentId();
                shoppingCart.setUserId(currentId);
//                shoppingCart.setSetmealId(setmealId);
                shoppingCart.setNumber(1);
                shoppingCart.setAmount(byId.getPrice());
                shoppingCart.setCreateTime(LocalDateTime.now());
                //将套餐加入到购物车的商品中
                shoppingCartService.save(shoppingCart);
            }
        }else{
            //如果是菜品
            Long dishId = shoppingCart.getDishId();
            LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getDishId,dishId);
            ShoppingCart one = shoppingCartService.getOne(shoppingCartLambdaQueryWrapper);
            if(one!=null){
                //该菜品的数量
                Integer number = one.getNumber();
                number+=1;
                LambdaUpdateWrapper<ShoppingCart> shoppingCartLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                shoppingCartLambdaUpdateWrapper.eq(ShoppingCart::getDishId,dishId);
                shoppingCartLambdaUpdateWrapper.set(ShoppingCart::getNumber,number);
                shoppingCartLambdaUpdateWrapper.set(ShoppingCart::getCreateTime,LocalDateTime.now());
                shoppingCartService.update(shoppingCartLambdaUpdateWrapper);
            }else{
                //没有该菜品
                //得到该菜品
                Dish byId = dishService.getById(dishId);
                shoppingCart.setName(byId.getName());
                shoppingCart.setImage(byId.getImage());
                shoppingCart.setUserId(BaseContext.getCurrentId());
//                shoppingCart.setDishId(dishId);
                shoppingCart.setNumber(1);
                shoppingCart.setAmount(byId.getPrice());
                shoppingCart.setCreateTime(LocalDateTime.now());
                shoppingCartService.save(shoppingCart);
            }
        }
       return R.success("加入成功");
    }
    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        //SQL:delete from shopping_cart where user_id = ?

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }
    /**
     * 减一
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据：{}",shoppingCart);

        //获取当前是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);

        Long dishId = shoppingCart.getDishId();
        if (dishId != null){
            //添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            //添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        //查询购物车中当前菜品或者套餐
        //SQL :select * from shopping_cart where user_id = ?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);

        //在原来数量基础上减一
        Integer number = cartServiceOne.getNumber();
        cartServiceOne.setNumber(number - 1);
        if(number-1==0){
            shoppingCartService.remove(queryWrapper);
        }else {
            shoppingCartService.updateById(cartServiceOne);
        }
        return R.success(cartServiceOne);
    }

}
