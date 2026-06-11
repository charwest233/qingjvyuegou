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

    /** 查询订单的售后记录 */
    @Select("SELECT * FROM t_order_refund WHERE order_id = #{orderId} ORDER BY apply_time DESC")
    List<OrderRefund> findByOrderId(@Param("orderId") Long orderId);

    /** 查询用户的所有售后记录 */
    @Select("SELECT * FROM t_order_refund WHERE user_id = #{userId} ORDER BY apply_time DESC")
    List<OrderRefund> findByUserId(@Param("userId") Long userId);
}
