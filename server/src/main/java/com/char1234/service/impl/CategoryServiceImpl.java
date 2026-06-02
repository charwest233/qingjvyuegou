package com.char1234.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.char1234.entity.Category;
import com.char1234.mapper.CategoryMapper;
import com.char1234.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类 Service 实现类
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> listWithProductCount() {
        return categoryMapper.selectListWithProductCount();
    }
}
