package com.char1234.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.char1234.entity.ProductReview;

import java.util.List;
import java.util.Map;

public interface ProductReviewService extends IService<ProductReview> {

    /**
     * 提交评价（批量，一个订单多个商品）
     * @param reviews 评价列表，每个包含 orderId, orderItemId, productId, rating, content
     */
    void submitReviews(Long userId, List<Map<String, Object>> reviews);

    /**
     * 查询商品评价（分页，按时间倒序）
     */
    Page<ProductReview> pageByProduct(Long productId, int page, int size);

    /**
     * 查询用户对某订单已评价的 orderItemId 列表
     */
    List<Long> getReviewedItemIds(Long userId, Long orderId);

    /**
     * 查询用户自己的评价列表
     */
    Page<ProductReview> pageByUser(Long userId, int page, int size);

    /**
     * 删除评价（管理员直接删；用户只能删自己的，同时更新商品评分）
     */
    void deleteReview(Long reviewId, Long userId, boolean isAdmin);

    /**
     * 管理端分页查询所有评价（关联用户昵称）
     */
    Page<ProductReview> pageForAdmin(String keyword, int page, int size);
}
