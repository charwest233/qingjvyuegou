package com.char1234.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.char1234.entity.CartItem;
import com.char1234.entity.CartItemEntity;
import com.char1234.entity.Product;
import com.char1234.mapper.CartItemMapper;
import com.char1234.mapper.ProductMapper;
import com.char1234.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 购物车 Service 实现（MySQL 数据库存储）
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    @Override
    public List<CartItem> getCartList(Long userId) {
        List<CartItemEntity> entities = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItemEntity>()
                        .eq(CartItemEntity::getUserId, userId)
                        .orderByDesc(CartItemEntity::getCreateTime)
        );
        return entities.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public boolean addToCart(Long userId, Long productId, Integer quantity) {
        // 检查购物车是否已存在该商品
        CartItemEntity existing = cartItemMapper.selectOne(
                new LambdaQueryWrapper<CartItemEntity>()
                        .eq(CartItemEntity::getUserId, userId)
                        .eq(CartItemEntity::getProductId, productId)
        );
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            existing.setUpdateTime(LocalDateTime.now());
            cartItemMapper.updateById(existing);
            return true;
        }
        // 查询商品信息
        Product product = productMapper.selectById(productId);
        if (product == null) return false;

        CartItemEntity entity = new CartItemEntity();
        entity.setUserId(userId);
        entity.setProductId(productId);
        entity.setProductName(product.getName());
        entity.setProductImage(product.getMainImage());
        entity.setPrice(product.getPrice());
        entity.setQuantity(quantity);
        entity.setSelected(1);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        cartItemMapper.insert(entity);
        return true;
    }

    @Override
    public boolean updateQuantity(Long userId, Long productId, Integer quantity) {
        CartItemEntity entity = cartItemMapper.selectOne(
                new LambdaQueryWrapper<CartItemEntity>()
                        .eq(CartItemEntity::getUserId, userId)
                        .eq(CartItemEntity::getProductId, productId)
        );
        if (entity == null) return false;
        if (quantity <= 0) {
            cartItemMapper.deleteById(entity.getId());
            return true;
        }
        entity.setQuantity(quantity);
        entity.setUpdateTime(LocalDateTime.now());
        cartItemMapper.updateById(entity);
        return true;
    }

    @Override
    public boolean removeFromCart(Long userId, Long productId) {
        cartItemMapper.delete(
                new LambdaQueryWrapper<CartItemEntity>()
                        .eq(CartItemEntity::getUserId, userId)
                        .eq(CartItemEntity::getProductId, productId)
        );
        return true;
    }

    @Override
    public boolean clearCart(Long userId) {
        cartItemMapper.delete(
                new LambdaQueryWrapper<CartItemEntity>().eq(CartItemEntity::getUserId, userId)
        );
        return true;
    }

    @Override
    public boolean toggleSelected(Long userId, Long productId, Boolean selected) {
        CartItemEntity entity = cartItemMapper.selectOne(
                new LambdaQueryWrapper<CartItemEntity>()
                        .eq(CartItemEntity::getUserId, userId)
                        .eq(CartItemEntity::getProductId, productId)
        );
        if (entity == null) return false;
        entity.setSelected(selected ? 1 : 0);
        entity.setUpdateTime(LocalDateTime.now());
        cartItemMapper.updateById(entity);
        return true;
    }

    @Override
    public boolean toggleAll(Long userId, Boolean selected) {
        CartItemEntity entity = new CartItemEntity();
        entity.setSelected(selected ? 1 : 0);
        entity.setUpdateTime(LocalDateTime.now());
        cartItemMapper.update(entity,
                new LambdaQueryWrapper<CartItemEntity>().eq(CartItemEntity::getUserId, userId));
        return true;
    }

    @Override
    public List<CartItem> getSelectedItems(Long userId) {
        List<CartItemEntity> entities = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItemEntity>()
                        .eq(CartItemEntity::getUserId, userId)
                        .eq(CartItemEntity::getSelected, 1)
                        .orderByDesc(CartItemEntity::getCreateTime)
        );
        return entities.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public boolean mergeCart(Long userId, List<CartItem> localItems) {
        for (CartItem localItem : localItems) {
            addToCart(userId, localItem.getProductId(), localItem.getQuantity());
        }
        return true;
    }

    /**
     * 将数据库实体转为前端 DTO
     */
    private CartItem toDTO(CartItemEntity entity) {
        CartItem dto = new CartItem();
        dto.setProductId(entity.getProductId());
        dto.setProductName(entity.getProductName());
        dto.setProductImage(entity.getProductImage());
        dto.setPrice(entity.getPrice().doubleValue());
        dto.setQuantity(entity.getQuantity());
        dto.setSelected(entity.getSelected() == 1);
        dto.setCreateTime(entity.getCreateTime() != null
                ? entity.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                : System.currentTimeMillis());
        return dto;
    }
}
