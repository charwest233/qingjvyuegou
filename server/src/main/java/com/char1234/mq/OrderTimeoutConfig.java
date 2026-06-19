package com.char1234.mq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单超时取消 — RabbitMQ 延迟队列配置（TTL + DLX 模式）
 *
 * 流程：
 *   下单 → 消息发到 order.delay.queue（带 TTL）
 *        → TTL 过期 → 消息路由到 order.timeout.queue
 *                      → OrderTimeoutConsumer 检查订单状态
 *                          → status==0 则取消 + 回补库存
 *                          → 否则忽略
 */
@Configuration
public class OrderTimeoutConfig {

    public static final String DELAY_QUEUE = "order.delay.queue";
    public static final String DEAD_LETTER_EXCHANGE = "order.timeout.exchange";
    public static final String TIMEOUT_QUEUE = "order.timeout.queue";

    /** 死信交换机 */
    @Bean
    public DirectExchange orderTimeoutExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    /**
     * 延迟队列：设置 TTL 和死信交换机
     * TTL 时间从配置文件 order.timeout-seconds 读取（默认 900s = 15min）
     */
    @Bean
    public Queue orderDelayQueue(org.springframework.core.env.Environment env) {
        int ttlSeconds = Integer.parseInt(
                env.getProperty("order.timeout-seconds", "900"));
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", TIMEOUT_QUEUE);
        args.put("x-message-ttl", ttlSeconds * 1000L);
        return new Queue(DELAY_QUEUE, true, false, false, args);
    }

    /** 死信队列（实际消费队列） */
    @Bean
    public Queue orderTimeoutQueue() {
        return new Queue(TIMEOUT_QUEUE, true);
    }

    /** 绑定死信队列到交换机 */
    @Bean
    public Binding orderTimeoutBinding() {
        return BindingBuilder.bind(orderTimeoutQueue())
                .to(orderTimeoutExchange())
                .with(TIMEOUT_QUEUE);
    }
}
