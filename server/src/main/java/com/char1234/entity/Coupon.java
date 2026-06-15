package com.char1234.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_coupon")
public class Coupon {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    /** 1-满减券 2-折扣券 */
    private Integer type;
    private BigDecimal value;
    private BigDecimal minAmount;
    private Integer validDays;
    /** 档位 1-5 */
    private Integer tier;
    private Integer weight;
    /** 0-下架 1-上架 */
    private Integer status;
    private LocalDateTime createTime;
}
