package com.char1234.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.char1234.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品 Mapper 接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 根据商品名称模糊查询
     */
    @Select("SELECT * FROM t_product WHERE name LIKE CONCAT('%', #{name}, '%') LIMIT 1")
    Product selectByName(@Param("name") String name);

    /**
     * 查询热门商品(按销量 + 评分综合排序)
     */
    @Select("SELECT p.*, c.name as categoryName, IFNULL(o.salesCount, 0) as salesCount " +
            "FROM t_product p " +
            "LEFT JOIN t_category c ON p.category_id = c.id " +
            "LEFT JOIN (SELECT product_id, SUM(quantity) as salesCount FROM t_order_item GROUP BY product_id) o " +
            "ON p.id = o.product_id " +
            "ORDER BY IFNULL(o.salesCount, 0) DESC, IFNULL(p.avg_rating, 0) DESC, p.review_count DESC " +
            "LIMIT #{limit}")
    List<Product> selectHotProducts(@Param("limit") Integer limit);

    /**
     * 根据条件查询商品列表
     */
    List<Product> selectProductList(@Param("name") String name,
                                    @Param("categoryId") Long categoryId,
                                    @Param("minPrice") java.math.BigDecimal minPrice,
                                    @Param("maxPrice") java.math.BigDecimal maxPrice);
}
