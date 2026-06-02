package com.char1234.service.task;

import com.char1234.entity.Order;
import com.char1234.entity.OrderItem;
import com.char1234.entity.Product;
import com.char1234.mapper.OrderItemMapper;
import com.char1234.mapper.OrderMapper;
import com.char1234.mapper.ProductMapper;
import com.char1234.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单定时任务：自动取消超时未支付的订单
 */
@Component
public class OrderAutoCancelTask {

    private static final Logger log = LoggerFactory.getLogger(OrderAutoCancelTask.class);
    private static final int CANCEL_MINUTES = 15;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 每 1 分钟执行一次，自动取消创建超过 15 分钟且未支付的订单
     */
    @Scheduled(fixedRate = 60_000)
    @Transactional(rollbackFor = Exception.class)
    public void autoCancelExpiredOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(CANCEL_MINUTES);
        List<Order> expiredOrders = orderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, 0)
                        .lt(Order::getCreateTime, deadline));

        if (expiredOrders.isEmpty()) {
            return;
        }

        log.info("自动取消 {} 笔超时未支付订单", expiredOrders.size());

        for (Order order : expiredOrders) {
            try {
                // 恢复库存
                List<OrderItem> items = orderItemMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderItem>()
                                .eq(OrderItem::getOrderId, order.getId()));
                for (OrderItem item : items) {
                    Product product = productMapper.selectById(item.getProductId());
                    if (product != null) {
                        product.setStock((product.getStock() == null ? 0 : product.getStock()) + item.getQuantity());
                        productMapper.updateById(product);
                    }
                }
                // 取消订单
                order.setStatus(-1);
                orderMapper.updateById(order);
                log.info("订单 {} 已自动取消", order.getOrderNo());
            } catch (Exception e) {
                log.error("自动取消订单 {} 失败", order.getOrderNo(), e);
            }
        }
    }
}
