package com.char1234.mq;

import com.char1234.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单超时消费者
 * 接收死信队列中的消息，检查订单是否超时未支付
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = OrderTimeoutConfig.TIMEOUT_QUEUE)
    public void handleOrderTimeout(OrderTimeoutMessage message) {
        Long orderId = message.getOrderId();
        log.info("收到订单超时消息: orderId={}, userId={}", orderId, message.getUserId());

        try {
            boolean cancelled = orderService.cancelOrder(orderId);
            if (cancelled) {
                log.info("订单已自动取消（超时未支付）: orderId={}", orderId);
            } else {
                log.info("订单无需取消（已支付或已被手动取消）: orderId={}", orderId);
            }
        } catch (Exception e) {
            log.error("订单超时取消失败: orderId={}", orderId, e);
        }
    }
}
