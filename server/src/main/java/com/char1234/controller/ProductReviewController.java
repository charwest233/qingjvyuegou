package com.char1234.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.char1234.common.Result;
import com.char1234.context.JwtContextHolder;
import com.char1234.entity.ProductReview;
import com.char1234.service.ProductReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品评价控制器
 */
@RestController
@RequestMapping("/api/review")
public class ProductReviewController {

    @Autowired
    private ProductReviewService productReviewService;

    /**
     * 提交评价（用户）
     * POST /api/review
     * Body: { reviews: [{ orderItemId, productId, rating, content }] }
     */
    @PostMapping
    public Result<Void> submit(@RequestBody Map<String, Object> body) {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) {
            return Result.error(403, "仅小程序用户可访问");
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> reviews = (List<Map<String, Object>>) body.get("reviews");
        if (reviews == null || reviews.isEmpty()) {
            return Result.error("评价内容不能为空");
        }
        try {
            productReviewService.submitReviews(ctx.principalId(), reviews);
            return Result.success(null);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询商品评价（公开）
     * GET /api/review/product/{productId}?page=1&size=10
     */
    @GetMapping("/product/{productId}")
    public Result<Map<String, Object>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<ProductReview> p = productReviewService.pageByProduct(productId, page, size);
        Map<String, Object> map = new HashMap<>();
        map.put("list", p.getRecords());
        map.put("total", p.getTotal());
        map.put("pages", p.getPages());
        return Result.success(map);
    }

    /**
     * 查询某订单已评价的 orderItemId（用户）
     * GET /api/review/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public Result<Map<String, Object>> getOrderReviews(@PathVariable Long orderId) {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) {
            return Result.error(403, "仅小程序用户可访问");
        }
        List<Long> reviewed = productReviewService.getReviewedItemIds(ctx.principalId(), orderId);
        return Result.success(Map.of("reviewedItemIds", reviewed));
    }

    /**
     * 查询当前用户的评价列表
     * GET /api/review/user?page=1&size=10
     */
    @GetMapping("/user")
    public Result<Map<String, Object>> getUserReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isMpUser()) {
            return Result.error(403, "仅小程序用户可访问");
        }
        Page<ProductReview> p = productReviewService.pageByUser(ctx.principalId(), page, size);
        Map<String, Object> map = new HashMap<>();
        map.put("list", p.getRecords());
        map.put("total", p.getTotal());
        map.put("pages", p.getPages());
        return Result.success(map);
    }

    // ---- 管理端接口 ----

    /**
     * 管理端分页查询评价
     * GET /api/review/list?keyword=&page=1&size=10
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> listForAdmin(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null || !ctx.isAdmin()) {
            return Result.error(403, "仅管理员可访问");
        }
        Page<ProductReview> p = productReviewService.pageForAdmin(keyword, page, size);
        Map<String, Object> map = new HashMap<>();
        map.put("list", p.getRecords());
        map.put("total", p.getTotal());
        map.put("pages", p.getPages());
        return Result.success(map);
    }

    /**
     * 删除评价（管理员可删任意；普通用户只能删自己的）
     * DELETE /api/review/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        JwtContextHolder.Context ctx = JwtContextHolder.get();
        if (ctx == null) {
            return Result.error(403, "未登录");
        }
        try {
            productReviewService.deleteReview(id, ctx.principalId(), ctx.isAdmin());
            return Result.success(null);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}
