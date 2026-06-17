package com.char1234.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.char1234.entity.Order;
import com.char1234.entity.OrderItem;
import com.char1234.entity.OrderRefund;
import com.char1234.mapper.OrderItemMapper;
import com.char1234.mapper.OrderRefundMapper;
import com.char1234.service.AlipayService;
import com.char1234.service.OrderRefundService;
import com.char1234.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    @Autowired
    private AlipayService alipayService;

    @Override
    @Transactional
    public OrderRefund applyRefund(Long userId, Long orderId, Long orderItemId,
                                   Integer type, String reason, String description) {
        Order order = orderService.getById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new RuntimeException("无权操作此订单");
        if (order.getStatus() < 1 || order.getStatus() > 3) throw new RuntimeException("当前订单状态不可申请售后");

        // ===== 防重复申请检查 =====
        // 整单退款：检查是否存在任何整单退款记录（含已驳回/已撤销，防止重复创建）
        if (orderItemId == null) {
            if (baseMapper.countByOrderIdAnyStatus(orderId) > 0) {
                throw new RuntimeException("该订单已申请过整单退款，不能重复申请");
            }
            // 已有单品退款的，也不能再申请整单退款
            if (baseMapper.countByOrderId(orderId) > 0) {
                throw new RuntimeException("该订单已有商品申请售后，请先撤销后再申请整单退款");
            }
        } else {
            // 单品退款：已有整单退款的不能单品退款
            if (baseMapper.countByOrderIdAnyStatus(orderId) > 0) {
                throw new RuntimeException("该订单已申请整单退款，不能再对单个商品申请售后");
            }
            if (baseMapper.countActiveByOrderItemId(orderItemId) > 0) {
                throw new RuntimeException("该商品已申请售后");
            }
        }

        // ===== 计算退款金额 =====
        BigDecimal refundAmount;
        String productName = null, productImage = null, productId = null;
        Integer quantity = null;

        if (orderItemId != null) {
            OrderItem item = orderItemMapper.selectById(orderItemId);
            if (item == null || !item.getOrderId().equals(orderId)) throw new RuntimeException("订单项不存在");

            // 单品退款：按价格比例分摊优惠券
            BigDecimal itemOriginal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            // 查询订单所有商品原总价
            List<OrderItem> allItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
            BigDecimal allOriginal = allItems.stream()
                    .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal paidAmount = order.getTotalAmount(); // 已扣优惠券
            if (paidAmount.compareTo(BigDecimal.ZERO) > 0
                    && allOriginal.compareTo(paidAmount) > 0) {
                // 有优惠券，按比例分摊：退款 = 商品原价 × (实付 / 原总价)
                refundAmount = itemOriginal.multiply(paidAmount)
                        .divide(allOriginal, 2, RoundingMode.HALF_UP);
            } else {
                refundAmount = itemOriginal;
            }

            productName = item.getProductName();
            productImage = item.getProductImage();
            quantity = item.getQuantity();
        } else {
            // 整单退款金额 = 订单实付金额（已在创建时扣减优惠券）
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
        refund.setProductName(productName);
        refund.setProductImage(productImage);
        if (productId != null) {
            refund.setProductId(Long.valueOf(productId));
        }
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
    @Transactional(rollbackFor = Exception.class)
    public void auditRefund(Long refundId, Long adminId, boolean approved, String remark) {
        OrderRefund refund = getById(refundId);
        if (refund == null) throw new RuntimeException("售后单不存在");

        if (refund.getType() == 1) {
            if (refund.getStatus() != 0) throw new RuntimeException("当前状态不可审核");
        } else {
            if (refund.getStatus() != 3) throw new RuntimeException("请先确认收货后再审核");
        }

        if (approved) {
            // 查找订单获取支付宝交易号
            Order order = orderService.getById(refund.getOrderId());
            if (order != null) {
                String tradeNo = order.getPayNo();
                // 如果有支付宝交易号，调用支付宝退款
                if (tradeNo != null && !tradeNo.isEmpty()) {
                    String reason = refund.getReason() != null ? refund.getReason() : "用户申请退款";
                    boolean refundOk = alipayService.refund(tradeNo, refund.getRefundAmount(), reason);
                    if (!refundOk) {
                        log.error("支付宝退款失败，事务回滚 refundId={}, tradeNo={}", refundId, tradeNo);
                        throw new RuntimeException("支付宝退款失败，请联系管理员");
                    }
                } else {
                    log.warn("订单无支付宝交易号，跳过实际退款（模拟支付退款）orderId={}", order.getId());
                }

                // 更新订单状态为"已售后"
                if (refund.getType() == 1) {
                    if (order.getStatus() == 1) {
                        order.setStatus(4);
                        orderService.updateById(order);
                    }
                } else {
                    // 退货退款：订单已发货/已完成，同样标记为已售后
                    order.setStatus(4);
                    orderService.updateById(order);
                }
            }

            refund.setStatus(4); // 已退款
            refund.setRefundTime(LocalDateTime.now());
        } else {
            refund.setStatus(5); // 审核驳回
        }
        refund.setAuditRemark(remark);
        refund.setAuditTime(LocalDateTime.now());
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
