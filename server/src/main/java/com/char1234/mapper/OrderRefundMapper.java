package com.char1234.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.char1234.entity.OrderRefund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderRefundMapper extends BaseMapper<OrderRefund> {

    /** 查询指定订单项是否已有售后记录 */
    @Select("SELECT COUNT(*) FROM t_order_refund WHERE order_item_id = #{orderItemId} AND status IN (0,1,2)")
    int countActiveByOrderItemId(@Param("orderItemId") Long orderItemId);

    /** 查询指定订单是否已有活跃的整单退款（order_item_id IS NULL 且未完结） */
    @Select("SELECT COUNT(*) FROM t_order_refund WHERE order_id = #{orderId} AND order_item_id IS NULL AND status IN (0,1,2)")
    int countActiveByOrderId(@Param("orderId") Long orderId);

    /** 查询指定订单是否有任何整单退款记录（不限状态，防止重复创建） */
    @Select("SELECT COUNT(*) FROM t_order_refund WHERE order_id = #{orderId} AND order_item_id IS NULL")
    int countByOrderIdAnyStatus(@Param("orderId") Long orderId);

    /** 查询指定订单的所有售后记录数（不限状态，用于冲突检查） */
    @Select("SELECT COUNT(*) FROM t_order_refund WHERE order_id = #{orderId}")
    int countByOrderId(@Param("orderId") Long orderId);

    /** 查询订单的售后记录 */
    @Select("SELECT * FROM t_order_refund WHERE order_id = #{orderId} ORDER BY apply_time DESC")
    List<OrderRefund> findByOrderId(@Param("orderId") Long orderId);

    /** 查询用户的所有售后记录 */
    @Select("SELECT * FROM t_order_refund WHERE user_id = #{userId} ORDER BY apply_time DESC")
    List<OrderRefund> findByUserId(@Param("userId") Long userId);
}
