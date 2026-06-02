package com.char1234.controller;

import com.char1234.common.Result;
import com.char1234.context.JwtContextHolder;
import com.char1234.entity.CartItem;
import com.char1234.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * 获取购物车列表
     */
    @GetMapping("/list")
    public Result<List<CartItem>> getCartList() {
        Long userId = JwtContextHolder.get().principalId();
        return Result.success(cartService.getCartList(userId));
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public Result<String> addToCart(@RequestBody Map<String, Object> params) {
        Long userId = JwtContextHolder.get().principalId();
        Long productId = Long.valueOf(params.get("productId").toString());
        Integer quantity = Integer.valueOf(params.getOrDefault("quantity", "1").toString());
        boolean success = cartService.addToCart(userId, productId, quantity);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    /**
     * 更新商品数量
     */
    @PutMapping("/update")
    public Result<String> updateQuantity(@RequestBody Map<String, Object> params) {
        Long userId = JwtContextHolder.get().principalId();
        Long productId = Long.valueOf(params.get("productId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        boolean success = cartService.updateQuantity(userId, productId, quantity);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("/remove/{productId}")
    public Result<String> removeFromCart(@PathVariable Long productId) {
        Long userId = JwtContextHolder.get().principalId();
        cartService.removeFromCart(userId, productId);
        return Result.success("删除成功");
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    public Result<String> clearCart() {
        Long userId = JwtContextHolder.get().principalId();
        cartService.clearCart(userId);
        return Result.success("购物车已清空");
    }

    /**
     * 切换选中状态
     */
    @PutMapping("/toggle")
    public Result<String> toggleSelected(@RequestBody Map<String, Object> params) {
        Long userId = JwtContextHolder.get().principalId();
        Long productId = Long.valueOf(params.get("productId").toString());
        Boolean selected = Boolean.valueOf(params.get("selected").toString());
        cartService.toggleSelected(userId, productId, selected);
        return Result.success("操作成功");
    }

    /**
     * 全选/取消全选
     */
    @PutMapping("/toggle-all")
    public Result<String> toggleAll(@RequestBody Map<String, Object> params) {
        Long userId = JwtContextHolder.get().principalId();
        Boolean selected = Boolean.valueOf(params.get("selected").toString());
        cartService.toggleAll(userId, selected);
        return Result.success("操作成功");
    }

    /**
     * 合并本地购物车
     */
    @PostMapping("/merge")
    public Result<String> mergeCart(@RequestBody List<CartItem> localItems) {
        Long userId = JwtContextHolder.get().principalId();
        cartService.mergeCart(userId, localItems);
        return Result.success("合并成功");
    }

    /**
     * 获取选中商品（用于结算）
     */
    @GetMapping("/selected")
    public Result<List<CartItem>> getSelectedItems() {
        Long userId = JwtContextHolder.get().principalId();
        return Result.success(cartService.getSelectedItems(userId));
    }
}
