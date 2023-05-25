package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("category")
public class CategoryController {
    @Autowired
    CategoryService categoryService;
    /**
     * 菜品表分页
     */
    @GetMapping("/page")
    public R<Page> page(Integer page,Integer pageSize){
        Page pageInfo= new Page(page, pageSize);
        LambdaQueryWrapper<Category> wrapper=new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Category::getSort);
        categoryService.page(pageInfo,wrapper);
        return R.success(pageInfo);
    }
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("添加菜品");
        categoryService.save(category);
        return R.success("添加成功");
    }
    @DeleteMapping
    public R<String> delete(Long id){
        log.info("删除分类");
        categoryService.remove(id);
        return R.success("删除成功");
    }
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("更新分类");
        categoryService.updateById(category);
        return R.success("更新成功");
    }
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(category.getType()!=null,Category::getType,category.getType());
        wrapper.orderByAsc(Category::getSort).orderByAsc(Category::getCreateTime);
        List<Category> list = categoryService.list(wrapper);
        return R.success(list);
    }
}
