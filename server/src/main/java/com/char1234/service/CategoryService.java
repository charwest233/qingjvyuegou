package com.char1234.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.char1234.entity.Category;

import java.util.List;

/**
 * 分类 Service 接口
 */
public interface CategoryService extends IService<Category> {

    /**
     * 查询分类列表(带商品数量)
     */
    List<Category> listWithProductCount();
}
