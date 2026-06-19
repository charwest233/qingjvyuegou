package com.char1234.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体（含收货信息与明细列表；userName/items 等为填充字段）
 */
@Data
@TableName("t_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private BigDecimal totalAmount;

    /**
     * -1-已取消, 0-待支付, 1-已支付, 2-已发货, 3-已完成, 4-已完成(售后)
     */
    private Integer status;

    /** 支付宝交易号 */
    private String payNo;

    /** 支付时间 */
    private LocalDateTime payTime;

    /**
     * 下单时选用的地址表主键（可空）
     */
    private Long addressId;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    /** 快递公司 */
    private String expressCompany;

    /** 快递单号 */
    private String expressNo;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private String userAvatar;

    @TableField(exist = false)
    private List<OrderItem> items;

    /** 0-无售后 1-售后处理中 2-售后已完成 */
    @TableField(exist = false)
    private Integer refundStatus;
}
