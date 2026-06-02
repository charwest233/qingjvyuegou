package com.char1234.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.char1234.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 订单 Mapper 接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 查询订单列表(带用户信息)
     */
    List<Order> selectOrderList(@Param("orderNo") String orderNo,
                                @Param("status") Integer status,
                                @Param("userId") Long userId);

    /**
     * 查询订单详情
     */
    Order selectOrderById(@Param("id") Long id);

    /**
     * 查询订单统计
     */
    @Select("SELECT " +
            "status, COUNT(*) as count " +
            "FROM t_order " +
            "GROUP BY status")
    List<Map<String, Object>> selectOrderStatistics();

    /**
     * 查询今日订单数
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE DATE(create_time) = CURDATE()")
    Integer selectTodayOrderCount();

    /**
     * 查询今日销售额
     */
    @Select("SELECT IFNULL(SUM(total_amount), 0) FROM t_order " +
            "WHERE DATE(create_time) = CURDATE() AND status > 0")
    java.math.BigDecimal selectTodaySales();
}
