package com.char1234.controller;

import com.char1234.common.Result;
import com.char1234.entity.Category;
import com.char1234.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 分类 Controller
 */
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取分类列表
     */
    @GetMapping("/list")
    public Result<List<Category>> list() {
        List<Category> list = categoryService.listWithProductCount();
        return Result.success(list);
    }

    /**
     * 获取分类树
     */
    @GetMapping("/tree")
    public Result<List<Category>> tree() {
        // 简单返回列表，实际项目中可能需要递归构建树结构
        List<Category> list = categoryService.listWithProductCount();
        return Result.success(list);
    }

    /**
     * 创建分类
     */
    @PostMapping
    public Result<Category> create(@RequestBody Category category) {
        boolean success = categoryService.save(category);
        if (!success) {
            return Result.error("创建失败");
        }
        return Result.success(category);
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public Result<Category> update(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        boolean success = categoryService.updateById(category);
        if (!success) {
            return Result.error("更新失败");
        }
        return Result.success(category);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = categoryService.removeById(id);
        if (!success) {
            return Result.error("删除失败");
        }
        return Result.success(true);
    }

    /**
     * 更新分类排序
     */
    @PutMapping("/{id}/sort")
    public Result<Boolean> updateSort(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Integer sort = params.get("sort");
        if (sort == null) {
            return Result.error("排序值不能为空");
        }

        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }

        category.setSort(sort);
        boolean success = categoryService.updateById(category);
        return Result.success(success);
    }
}
