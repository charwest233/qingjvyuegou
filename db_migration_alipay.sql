-- =============================================
-- 支付宝支付相关字段迁移
-- t_order 表新增 pay_no / pay_time
-- =============================================

ALTER TABLE `t_order`
  ADD COLUMN `pay_no` varchar(64) DEFAULT NULL COMMENT '支付宝交易号' AFTER `status`,
  ADD COLUMN `pay_time` datetime DEFAULT NULL COMMENT '支付时间' AFTER `pay_no`;
