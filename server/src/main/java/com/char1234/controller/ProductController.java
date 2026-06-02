package com.char1234.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.char1234.common.Result;
import com.char1234.entity.Product;
import com.char1234.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品 Controller
 */
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 获取商品列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sort) {

        Page<Product> pageResult = productService.pageList(page, size, name, categoryId, minPrice, maxPrice, sort);

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        result.put("pages", pageResult.getPages());

        return Result.success(result);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }

    /**
     * 创建商品
     */
    @PostMapping
    public Result<Product> create(@RequestBody Product product) {
        boolean success = productService.save(product);
        if (!success) {
            return Result.error("创建失败");
        }
        return Result.success(product);
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public Result<Product> update(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        boolean success = productService.updateById(product);
        if (!success) {
            return Result.error("更新失败");
        }
        return Result.success(product);
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean success = productService.removeById(id);
        if (!success) {
            return Result.error("删除失败");
        }
        return Result.success(true);
    }

    /**
     * 更新商品状态
     */
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        // 状态字段在商品表中不存在，这里预留接口
        return Result.success(true);
    }

    /**
     * 获取热门商品
     */
    @GetMapping("/hot")
    public Result<List<Product>> hotProducts(@RequestParam(defaultValue = "12") Integer limit) {
        List<Product> list = productService.getHotProducts(limit);
        return Result.success(list);
    }
}
