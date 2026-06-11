package com.char1234.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.char1234.entity.OrderRefund;

import java.util.List;

public interface OrderRefundService extends IService<OrderRefund> {

    /** 申请售后 */
    OrderRefund applyRefund(Long userId, Long orderId, Long orderItemId,
                            Integer type, String reason, String description);

    /** 查询用户售后列表 */
    List<OrderRefund> getUserRefunds(Long userId);

    /** 查询指定订单的售后 */
    List<OrderRefund> getOrderRefunds(Long orderId);

    /** 查询售后详情 */
    OrderRefund getRefundDetail(Long refundId);

    /** 用户填写退货物流 */
    void fillReturnLogistics(Long refundId, Long userId, String company, String trackingNo);

    /** 用户撤销申请 */
    void cancelRefund(Long refundId, Long userId);

    /** 管理员审核 */
    void auditRefund(Long refundId, Long adminId, boolean approved, String remark);

    /** 管理员确认收货 */
    void confirmReceipt(Long refundId, Long adminId);

    /** 查询所有售后（管理员） */
    List<OrderRefund> getAllRefunds(Integer status);

    /** 生成售后单号 */
    String generateRefundNo();
}
