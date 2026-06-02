package com.char1234.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.char1234.entity.Product;
import com.char1234.mapper.ProductMapper;
import com.char1234.service.ProductService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品 Service 实现类
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public Page<Product> pageList(Integer page, Integer size, String name, Long categoryId,
                                  BigDecimal minPrice, BigDecimal maxPrice, String sort) {
        Page<Product> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        
        // 商品名称模糊查询
        if (StringUtils.isNotBlank(name)) {
            wrapper.like(Product::getName, name);
        }
        
        // 分类筛选
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        
        // 价格区间
        if (minPrice != null) {
            wrapper.ge(Product::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(Product::getPrice, maxPrice);
        }
        
        // 动态排序
        if (sort == null || "default".equals(sort)) {
            // 综合排序：按销量降序 → 评分降序 → 评价数降序
            wrapper.orderByDesc(Product::getSalesCount);
            wrapper.orderByDesc(Product::getAvgRating);
            wrapper.orderByDesc(Product::getReviewCount);
        } else if ("price_asc".equals(sort)) {
            wrapper.orderByAsc(Product::getPrice);
        } else if ("price_desc".equals(sort)) {
            wrapper.orderByDesc(Product::getPrice);
        } else if ("sales".equals(sort)) {
            wrapper.orderByDesc(Product::getSalesCount);
        } else {
            // 默认按创建时间降序
            wrapper.orderByDesc(Product::getCreateTime);
        }
        
        return page(pageParam, wrapper);
    }

    @Override
    public List<Product> getHotProducts(Integer limit) {
        return productMapper.selectHotProducts(limit);
    }
}
