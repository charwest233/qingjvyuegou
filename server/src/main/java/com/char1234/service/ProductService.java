package com.char1234.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.char1234.entity.Product;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品 Service 接口
 */
public interface ProductService extends IService<Product> {

    /**
     * 分页查询商品列表
     */
    Page<Product> pageList(Integer page, Integer size, String name, Long categoryId,
                           BigDecimal minPrice, BigDecimal maxPrice, String sort);

    /**
     * 查询热门商品
     */
    List<Product> getHotProducts(Integer limit);
}
