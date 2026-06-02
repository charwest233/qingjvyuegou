package com.char1234.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.char1234.entity.Order;
import com.char1234.entity.OrderItem;
import com.char1234.entity.Product;
import com.char1234.entity.ProductReview;
import com.char1234.entity.User;
import com.char1234.mapper.OrderItemMapper;
import com.char1234.mapper.OrderMapper;
import com.char1234.mapper.ProductMapper;
import com.char1234.mapper.ProductReviewMapper;
import com.char1234.mapper.UserMapper;
import com.char1234.service.ProductReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductReviewServiceImpl extends ServiceImpl<ProductReviewMapper, ProductReview>
        implements ProductReviewService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReviews(Long userId, List<Map<String, Object>> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            throw new IllegalArgumentException("评价内容不能为空");
        }

        Set<Long> productIds = new HashSet<>();
        Long orderId = null;

        for (Map<String, Object> item : reviews) {
            Long oi = Long.valueOf(item.get("orderItemId").toString());
            Long pid = Long.valueOf(item.get("productId").toString());
            Integer rating = Integer.valueOf(item.get("rating").toString());
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("评分必须在 1-5 之间");
            }

            // 校验订单项
            OrderItem oiEntity = orderItemMapper.selectById(oi);
            if (oiEntity == null || !oiEntity.getProductId().equals(pid)) {
                throw new IllegalArgumentException("订单项数据异常");
            }
            if (orderId == null) {
                orderId = oiEntity.getOrderId();
            } else if (!orderId.equals(oiEntity.getOrderId())) {
                throw new IllegalArgumentException("评价必须属于同一订单");
            }

            // 校验订单已完成且属于当前用户
            Order order = orderMapper.selectById(orderId);
            if (order == null || order.getStatus() != 3) {
                throw new IllegalArgumentException("订单未完成，无法评价");
            }
            if (!order.getUserId().equals(userId)) {
                throw new IllegalArgumentException("无权评价该订单");
            }

            // 检查是否已评价
            long count = count(new LambdaQueryWrapper<ProductReview>()
                    .eq(ProductReview::getOrderItemId, oi));
            if (count > 0) {
                throw new IllegalArgumentException("该商品已评价，请勿重复提交");
            }

            ProductReview review = new ProductReview();
            review.setOrderId(orderId);
            review.setOrderItemId(oi);
            review.setProductId(pid);
            review.setUserId(userId);
            review.setRating(rating);
            review.setContent((String) item.getOrDefault("content", ""));
            review.setCreateTime(LocalDateTime.now());
            save(review);

            productIds.add(pid);
        }

        // 更新所有涉及商品的评分
        for (Long pid : productIds) {
            updateProductRating(pid);
        }
    }

    @Override
    public Page<ProductReview> pageByProduct(Long productId, int page, int size) {
        Page<ProductReview> p = new Page<>(page, size);
        Page<ProductReview> result = page(p, new LambdaQueryWrapper<ProductReview>()
                .eq(ProductReview::getProductId, productId)
                .orderByDesc(ProductReview::getCreateTime));

        fillUserInfo(result.getRecords());
        return result;
    }

    @Override
    public List<Long> getReviewedItemIds(Long userId, Long orderId) {
        return list(new LambdaQueryWrapper<ProductReview>()
                .eq(ProductReview::getUserId, userId)
                .eq(ProductReview::getOrderId, orderId))
                .stream()
                .map(ProductReview::getOrderItemId)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductReview> pageByUser(Long userId, int page, int size) {
        Page<ProductReview> p = new Page<>(page, size);
        Page<ProductReview> result = page(p, new LambdaQueryWrapper<ProductReview>()
                .eq(ProductReview::getUserId, userId)
                .orderByDesc(ProductReview::getCreateTime));
        fillProductInfo(result.getRecords());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReview(Long reviewId, Long userId, boolean isAdmin) {
        ProductReview review = getById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("评价不存在");
        }
        if (!isAdmin && !review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权删除该评价");
        }
        removeById(reviewId);
        // 更新商品评分
        updateProductRating(review.getProductId());
    }

    @Override
    public Page<ProductReview> pageForAdmin(String keyword, int page, int size) {
        Page<ProductReview> p = new Page<>(page, size);
        LambdaQueryWrapper<ProductReview> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ProductReview::getContent, keyword);
        }
        wrapper.orderByDesc(ProductReview::getCreateTime);
        Page<ProductReview> result = page(p, wrapper);
        fillUserInfo(result.getRecords());
        return result;
    }

    private void fillUserInfo(List<ProductReview> reviews) {
        if (reviews.isEmpty()) return;
        Set<Long> userIds = reviews.stream()
                .map(ProductReview::getUserId)
                .collect(Collectors.toSet());
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        for (ProductReview r : reviews) {
            User u = userMap.get(r.getUserId());
            if (u != null) {
                r.setNickname(u.getNickname());
                r.setAvatarUrl(u.getAvatarUrl());
            }
        }
    }

    private void fillProductInfo(List<ProductReview> reviews) {
        if (reviews.isEmpty()) return;
        Set<Long> productIds = reviews.stream()
                .map(ProductReview::getProductId)
                .collect(Collectors.toSet());
        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (ProductReview r : reviews) {
            Product p = productMap.get(r.getProductId());
            if (p != null) {
                r.setProductName(p.getName());
                r.setProductImage(p.getMainImage());
            }
        }
    }

    private void updateProductRating(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) return;

        List<ProductReview> all = list(new LambdaQueryWrapper<ProductReview>()
                .eq(ProductReview::getProductId, productId));
        int count = all.size();
        if (count > 0) {
            int sum = all.stream().mapToInt(ProductReview::getRating).sum();
            BigDecimal avg = BigDecimal.valueOf(sum)
                    .divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP);
            product.setAvgRating(avg);
        } else {
            product.setAvgRating(BigDecimal.ZERO);
        }
        product.setReviewCount(count);
        productMapper.updateById(product);
    }
}
