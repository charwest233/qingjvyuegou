package com.char1234.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.char1234.entity.Order;
import com.char1234.entity.OrderItem;
import com.char1234.entity.OrderRefund;
import com.char1234.mapper.OrderItemMapper;
import com.char1234.mapper.OrderRefundMapper;
import com.char1234.service.OrderRefundService;
import com.char1234.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class OrderRefundServiceImpl
        extends ServiceImpl<OrderRefundMapper, OrderRefund>
        implements OrderRefundService {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemMapper orderItemMapper;

    // 使用雪花算法思想：时间戳 + 随机数，避免重启后序号重复

    @Override
    @Transactional
    public OrderRefund applyRefund(Long userId, Long orderId, Long orderItemId,
                                   Integer type, String reason, String description) {
        Order order = orderService.getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new RuntimeException("无权操作此订单");
        if (order.getStatus() < 1 || order.getStatus() > 3) throw new RuntimeException("当前订单状态不可申请售后");

        BigDecimal refundAmount;
        String productName = null, productImage = null, productId = null;
        Integer quantity = null;

        if (orderItemId != null) {
            OrderItem item = orderItemMapper.selectById(orderItemId);
            if (item == null || !item.getOrderId().equals(orderId)) throw new RuntimeException("订单项不存在");
            if (baseMapper.countActiveByOrderItemId(orderItemId) > 0) throw new RuntimeException("该商品已申请售后");
            refundAmount = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            productName = item.getProductName();
            productImage = item.getProductImage();
            quantity = item.getQuantity();
        } else {
            refundAmount = order.getTotalAmount();
        }

        OrderRefund refund = new OrderRefund();
        refund.setRefundNo(generateRefundNo());
        refund.setOrderId(orderId);
        refund.setOrderItemId(orderItemId);
        refund.setUserId(userId);
        refund.setRefundAmount(refundAmount);
        refund.setQuantity(quantity);
        refund.setType(type);
        refund.setReason(reason);
        refund.setDescription(description);
        refund.setApplyTime(LocalDateTime.now());
        // 仅退款→0待审核；退货退款→1待填物流（跳过审核，直接填物流）
        refund.setStatus(type == 1 ? 0 : 1);
        save(refund);
        return refund;
    }

    @Override public List<OrderRefund> getUserRefunds(Long userId) { return baseMapper.findByUserId(userId); }
    @Override public List<OrderRefund> getOrderRefunds(Long orderId) { return baseMapper.findByOrderId(orderId); }
    @Override public OrderRefund getRefundDetail(Long refundId) { return getById(refundId); }

    @Override
    @Transactional
    public void fillReturnLogistics(Long refundId, Long userId, String company, String trackingNo) {
        OrderRefund refund = getById(refundId);
        if (refund == null) throw new RuntimeException("售后单不存在");
        if (!refund.getUserId().equals(userId)) throw new RuntimeException("无权操作");
        if (refund.getStatus() != 1) throw new RuntimeException("当前状态不可填写物流");
        if (refund.getType() != 2) throw new RuntimeException("仅退货退款需填写物流");
        refund.setExpressCompany(company);
        refund.setExpressNo(trackingNo);
        refund.setStatus(2); // 退货中
        updateById(refund);
    }

    @Override
    @Transactional
    public void cancelRefund(Long refundId, Long userId) {
        OrderRefund refund = getById(refundId);
        if (refund == null) throw new RuntimeException("售后单不存在");
        if (!refund.getUserId().equals(userId)) throw new RuntimeException("无权操作");
        if (refund.getStatus() > 2) throw new RuntimeException("当前状态不可撤销");
        refund.setStatus(6);
        updateById(refund);
    }

    @Override
    @Transactional
    public void auditRefund(Long refundId, Long adminId, boolean approved, String remark) {
        OrderRefund refund = getById(refundId);
        if (refund == null) throw new RuntimeException("售后单不存在");

        if (refund.getType() == 1) {
            if (refund.getStatus() != 0) throw new RuntimeException("当前状态不可审核");
        } else {
            if (refund.getStatus() != 3) throw new RuntimeException("请先确认收货后再审核");
        }

        refund.setStatus(approved ? 4 : 5);
        refund.setAuditRemark(remark);
        refund.setAuditTime(LocalDateTime.now());
        if (approved) refund.setRefundTime(LocalDateTime.now());
        updateById(refund);
    }

    @Override
    @Transactional
    public void confirmReceipt(Long refundId, Long adminId) {
        OrderRefund refund = getById(refundId);
        if (refund == null) throw new RuntimeException("售后单不存在");
        if (refund.getStatus() != 2) throw new RuntimeException("当前状态不可确认收货");
        refund.setStatus(3); // 待退款（等待管理员审核）
        updateById(refund);
    }

    @Override
    public List<OrderRefund> getAllRefunds(Integer status) {
        LambdaQueryWrapper<OrderRefund> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(OrderRefund::getStatus, status);
        w.orderByDesc(OrderRefund::getApplyTime);
        return list(w);
    }

    @Override
    public String generateRefundNo() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rand = (int)(Math.random() * 900) + 100; // 3位随机数
        return "RF" + ts + rand;
    }
}
