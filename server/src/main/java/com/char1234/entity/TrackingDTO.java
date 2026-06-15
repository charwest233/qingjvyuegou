package com.char1234.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 物流追踪数据 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingDTO {

    private Long orderId;
    private String orderNo;
    private String expressCompany;
    private String expressNo;
    /** shipping / delivered */
    private String status;
    /** 收货地址（前端路线模拟用） */
    private String receiverAddress;

    /** 轨迹点（连线用） */
    private List<Point> route;

    /** 物流步骤 */
    private List<TrackingStep> steps;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Point {
        private BigDecimal lng;
        private BigDecimal lat;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackingStep {
        private String time;
        private String desc;
        private BigDecimal lng;
        private BigDecimal lat;
    }
}
