package com.char1234.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单超时消息 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTimeoutMessage implements Serializable {
    private Long orderId;
    private Long userId;
}
