package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("addressBook")
public class AddressBookController {
    @Autowired
    AddressBookService addressBookService;
    @GetMapping("/list")
    public R<List<AddressBook>> list(HttpSession httpSession){
        //得到用户ID
        Long userId= (Long)httpSession.getAttribute("user");
        //根据用户ID查找用户的地址
        LambdaQueryWrapper<AddressBook> addressBookLambdaQueryWrapper = new LambdaQueryWrapper<>();
        addressBookLambdaQueryWrapper.eq(userId!=null,AddressBook::getUserId,userId);
        addressBookLambdaQueryWrapper.orderByDesc(AddressBook::getCreateTime);
        List<AddressBook> list = addressBookService.list(addressBookLambdaQueryWrapper);
        return R.success(list);
    }
    @PostMapping
    public R<String> add(@RequestBody AddressBook addressBook){
        Long currentId = BaseContext.getCurrentId();
        addressBook.setUserId(currentId);
        addressBookService.save(addressBook);
        return  R.success("添加成功");
    }
    @GetMapping("/{id}")
    public R<AddressBook> getById(@PathVariable Long id){
        AddressBook byId = addressBookService.getById(id);
        return R.success(byId);
    }
    @PutMapping
    public R<String> change(@RequestBody AddressBook addressBook){
        log.info("修改后地址信息，{}",addressBook);
        addressBookService.updateById(addressBook);
        return R.success("更新成功");
    }
    @PutMapping("/default")
    public R<String> setDefault(@RequestBody AddressBook addressBook){
        //将该用户所有的地址设为0，非默认
        Long currentId = BaseContext.getCurrentId();
        LambdaUpdateWrapper<AddressBook> addressBookLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        addressBookLambdaUpdateWrapper.eq(currentId!=null, AddressBook::getUserId,currentId);
        addressBookLambdaUpdateWrapper.set(AddressBook::getIsDefault,0);
        addressBookService.update(addressBookLambdaUpdateWrapper);
        //将我要设置的变为一
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return R.success("修改成功");
    }
    @DeleteMapping
    public R<String> delete(Long ids){
        addressBookService.removeById(ids);
        return R.success("删除成功");
    }
    @GetMapping("/default")
    public R<AddressBook> getDefault(){
        /**
         * 得到该用户默认地址
         */
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<AddressBook> addressBookLambdaQueryWrapper = new LambdaQueryWrapper<>();
        addressBookLambdaQueryWrapper.eq(AddressBook::getUserId,currentId);
        addressBookLambdaQueryWrapper.eq(AddressBook::getIsDefault,1);
        AddressBook one = addressBookService.getOne(addressBookLambdaQueryWrapper);
        if(one==null){
            throw new CustomException("无默认地址");
        }
        return R.success(one);
    }

}
