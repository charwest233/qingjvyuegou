package com.char1234.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 订单超时消息生产者
 * 下单后发送延迟消息，TTL 过期后由 OrderTimeoutConsumer 处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送订单超时延迟消息
     */
    public void sendTimeout(Long orderId, Long userId) {
        OrderTimeoutMessage msg = new OrderTimeoutMessage(orderId, userId);
        rabbitTemplate.convertAndSend(OrderTimeoutConfig.DELAY_QUEUE, msg);
        log.info("订单超时消息已发送: orderId={}, userId={}", orderId, userId);
    }
}
