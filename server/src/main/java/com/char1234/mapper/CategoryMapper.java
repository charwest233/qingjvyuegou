package com.char1234.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.char1234.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分类 Mapper 接口
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 查询分类及其商品数量
     */
    @Select("SELECT c.*, COUNT(p.id) as productCount " +
            "FROM t_category c " +
            "LEFT JOIN t_product p ON c.id = p.category_id " +
            "GROUP BY c.id " +
            "ORDER BY c.sort ASC")
    List<Category> selectListWithProductCount();
}
