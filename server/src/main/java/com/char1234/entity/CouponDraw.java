package com.char1234.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_coupon_draw")
public class CouponDraw {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate drawDate;
    private Integer drawCount;
}
