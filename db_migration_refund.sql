-- ============================================
-- 售后/退款申请表
-- ============================================
CREATE TABLE IF NOT EXISTS `t_order_refund` (
  `id`              BIGINT          NOT NULL AUTO_INCREMENT,
  `refund_no`       VARCHAR(50)     NOT NULL COMMENT '售后单号',
  `order_id`        BIGINT          NOT NULL COMMENT '订单ID',
  `order_item_id`   BIGINT          DEFAULT NULL COMMENT '订单项ID(NULL=整单退款)',
  `user_id`         BIGINT          NOT NULL COMMENT '用户ID',
  `product_id`      BIGINT          DEFAULT NULL COMMENT '商品ID',
  `product_name`    VARCHAR(255)    DEFAULT NULL COMMENT '商品名称快照',
  `product_image`   VARCHAR(512)    DEFAULT NULL COMMENT '商品图片快照',
  `refund_amount`   DECIMAL(10,2)   NOT NULL COMMENT '退款金额',
  `quantity`        INT             DEFAULT NULL COMMENT '退货数量',
  `type`            TINYINT         NOT NULL COMMENT '售后类型:1-仅退款,2-退货退款',
  `reason`          VARCHAR(500)    DEFAULT NULL COMMENT '退款原因',
  `description`     TEXT            COMMENT '问题描述',
  `status`          TINYINT         NOT NULL DEFAULT 0 COMMENT '状态:0-待审核,1-审核通过,2-待确认收货,3-已退款,4-已驳回,5-已撤销',
  `express_company` VARCHAR(50)     DEFAULT NULL COMMENT '退货快递公司',
  `express_no`      VARCHAR(50)     DEFAULT NULL COMMENT '退货快递单号',
  `audit_remark`    VARCHAR(500)    DEFAULT NULL COMMENT '审核备注',
  `apply_time`      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `audit_time`      DATETIME        DEFAULT NULL COMMENT '审核时间',
  `refund_time`     DATETIME        DEFAULT NULL COMMENT '退款完成时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后/退款申请表';
