package com.char1234.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.char1234.entity.Product;
import com.char1234.entity.ProductFavorite;
import com.char1234.mapper.ProductFavoriteMapper;
import com.char1234.service.ProductFavoriteService;
import com.char1234.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductFavoriteServiceImpl extends ServiceImpl<ProductFavoriteMapper, ProductFavorite>
        implements ProductFavoriteService {

    @Autowired
    private ProductService productService;

    @Override
    public boolean add(Long userId, Long productId) {
        Product p = productService.getById(productId);
        if (p == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        long c = count(new LambdaQueryWrapper<ProductFavorite>()
                .eq(ProductFavorite::getUserId, userId)
                .eq(ProductFavorite::getProductId, productId));
        if (c > 0) {
            return true;
        }
        ProductFavorite fav = new ProductFavorite();
        fav.setUserId(userId);
        fav.setProductId(productId);
        fav.setCreateTime(LocalDateTime.now());
        return save(fav);
    }

    @Override
    public boolean remove(Long userId, Long productId) {
        return remove(new LambdaQueryWrapper<ProductFavorite>()
                .eq(ProductFavorite::getUserId, userId)
                .eq(ProductFavorite::getProductId, productId));
    }

    @Override
    public boolean exists(Long userId, Long productId) {
        return count(new LambdaQueryWrapper<ProductFavorite>()
                .eq(ProductFavorite::getUserId, userId)
                .eq(ProductFavorite::getProductId, productId)) > 0;
    }

    @Override
    public Page<Product> pageProducts(Long userId, int page, int size) {
        Page<ProductFavorite> fp = new Page<>(page, size);
        Page<ProductFavorite> res = page(fp, new LambdaQueryWrapper<ProductFavorite>()
                .eq(ProductFavorite::getUserId, userId)
                .orderByDesc(ProductFavorite::getCreateTime));
        Page<Product> out = new Page<>(page, size);
        out.setTotal(res.getTotal());
        Map<Long, Integer> orderIndex = new LinkedHashMap<>();
        int z = 0;
        for (ProductFavorite rf : res.getRecords()) {
            orderIndex.putIfAbsent(rf.getProductId(), z++);
        }
        if (orderIndex.isEmpty()) {
            out.setRecords(List.of());
            return out;
        }
        List<Product> products = productService.listByIds(orderIndex.keySet());
        products.sort((a, b) -> Integer.compare(
                orderIndex.getOrDefault(a.getId(), 0),
                orderIndex.getOrDefault(b.getId(), 0)));
        out.setRecords(products);
        return out;
    }
}
