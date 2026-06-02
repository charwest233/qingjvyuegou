package com.char1234.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.char1234.common.Result;
import com.char1234.context.JwtContextHolder;
import com.char1234.entity.Product;
import com.char1234.service.ProductFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 小程序商品收藏。
 */
@RestController
@RequestMapping("/api/user/favorites")
public class ProductFavoriteMpController {

    @Autowired
    private ProductFavoriteService productFavoriteService;

    @GetMapping
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) {
            return Result.error(403, "仅小程序用户可访问");
        }
        Page<Product> p = productFavoriteService.pageProducts(ctx.principalId(), page, size);
        Map<String, Object> map = new HashMap<>();
        map.put("list", p.getRecords());
        map.put("total", p.getTotal());
        return Result.success(map);
    }

    @PostMapping
    public Result<Boolean> add(@RequestBody Map<String, Long> body) {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) {
            return Result.error(403, "仅小程序用户可访问");
        }
        Long productId = body.get("productId");
        if (productId == null) {
            return Result.error("productId 不能为空");
        }
        try {
            productFavoriteService.add(ctx.principalId(), productId);
            return Result.success(true);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{productId}")
    public Result<Boolean> remove(@PathVariable Long productId) {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) {
            return Result.error(403, "仅小程序用户可访问");
        }
        return Result.success(productFavoriteService.remove(ctx.principalId(), productId));
    }

    @GetMapping("/check/{productId}")
    public Result<Map<String, Boolean>> check(@PathVariable Long productId) {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) {
            return Result.error(403, "仅小程序用户可访问");
        }
        boolean ok = productFavoriteService.exists(ctx.principalId(), productId);
        return Result.success(Map.of("favored", ok));
    }
}
