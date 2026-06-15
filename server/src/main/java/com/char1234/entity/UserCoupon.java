package com.char1234.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_user_coupon")
public class UserCoupon {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long couponId;
    private String couponNo;
    private String name;
    /** 1-满减券 2-折扣券 */
    private Integer type;
    private BigDecimal value;
    private BigDecimal minAmount;
    /** 0-未使用 1-已使用 2-已过期 */
    private Integer status;
    private Long orderId;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private LocalDateTime createTime;
}
