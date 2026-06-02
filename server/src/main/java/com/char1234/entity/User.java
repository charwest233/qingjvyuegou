package com.char1234.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表: t_user
 */
@Data
@TableName("t_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 微信唯一标识
     */
    private String openid;

    /**
     * 微信多端统一时可返回
     */
    private String unionId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码(MD5)
     */
    @JsonIgnore
    private String password;

    /**
     * 1-管理员 2-普通用户
     */
    private Integer type;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 订单数量(非数据库字段)
     */
    @TableField(exist = false)
    private Integer orderCount;

    /**
     * 消费金额(非数据库字段)
     */
    @TableField(exist = false)
    private java.math.BigDecimal totalAmount;
}
