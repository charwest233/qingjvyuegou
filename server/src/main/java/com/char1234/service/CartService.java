package com.char1234.service;

import com.char1234.entity.CartItem;
import java.util.List;

public interface CartService {
    /**
     * 获取购物车列表
     */
    List<CartItem> getCartList(Long userId);

    /**
     * 添加商品到购物车
     */
    boolean addToCart(Long userId, Long productId, Integer quantity);

    /**
     * 更新购物车商品数量
     */
    boolean updateQuantity(Long userId, Long productId, Integer quantity);

    /**
     * 删除购物车商品
     */
    boolean removeFromCart(Long userId, Long productId);

    /**
     * 清空购物车
     */
    boolean clearCart(Long userId);

    /**
     * 切换选中状态
     */
    boolean toggleSelected(Long userId, Long productId, Boolean selected);

    /**
     * 全选/取消全选
     */
    boolean toggleAll(Long userId, Boolean selected);

    /**
     * 获取购物车选中商品列表（用于结算）
     */
    List<CartItem> getSelectedItems(Long userId);

    /**
     * 合并本地购物车到服务端
     */
    boolean mergeCart(Long userId, List<CartItem> localItems);
}
