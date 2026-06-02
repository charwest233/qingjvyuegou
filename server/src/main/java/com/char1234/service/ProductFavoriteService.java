package com.char1234.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.char1234.entity.Product;
import com.char1234.entity.ProductFavorite;

public interface ProductFavoriteService extends IService<ProductFavorite> {

    boolean add(Long userId, Long productId);

    boolean remove(Long userId, Long productId);

    boolean exists(Long userId, Long productId);

    Page<Product> pageProducts(Long userId, int page, int size);
}
