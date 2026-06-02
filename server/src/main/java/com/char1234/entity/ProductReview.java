package com.char1234.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_product_review")
public class ProductReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 订单项ID */
    private Long orderItemId;

    /** 商品ID */
    private Long productId;

    /** 用户ID */
    private Long userId;

    /** 评分 1-5 */
    private Integer rating;

    /** 评价内容 */
    private String content;

    /** 创建时间 */
    private LocalDateTime createTime;

    // ---- 非数据库字段 ----

    /** 用户昵称 */
    @TableField(exist = false)
    private String nickname;

    /** 用户头像 */
    @TableField(exist = false)
    private String avatarUrl;

    /** 商品名称 */
    @TableField(exist = false)
    private String productName;

    /** 商品图片 */
    @TableField(exist = false)
    private String productImage;
}
