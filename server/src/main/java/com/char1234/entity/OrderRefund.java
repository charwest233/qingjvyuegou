package com.char1234.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_order_refund")
public class OrderRefund {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 售后单号 */
    private String refundNo;

    /** 订单ID */
    private Long orderId;

    /** 订单项ID(NULL=整单退款) */
    private Long orderItemId;

    /** 用户ID */
    private Long userId;

    /** 商品ID */
    private Long productId;

    /** 商品名称快照 */
    private String productName;

    /** 商品图片快照 */
    private String productImage;

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 退货数量 */
    private Integer quantity;

    /** 售后类型:1-仅退款,2-退货退款 */
    private Integer type;

    /** 退款原因 */
    private String reason;

    /** 问题描述 */
    private String description;

    /**
     * 状态:
     * 0-待审核, 1-审核通过, 2-待确认收货,
     * 3-已退款, 4-已驳回, 5-已撤销
     */
    private Integer status;

    /** 退货快递公司 */
    private String expressCompany;

    /** 退货快递单号 */
    private String expressNo;

    /** 审核备注 */
    private String auditRemark;

    /** 申请时间 */
    private LocalDateTime applyTime;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 退款完成时间 */
    private LocalDateTime refundTime;
}
