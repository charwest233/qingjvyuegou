-- 人工客服消息表
DROP TABLE IF EXISTS `t_customer_service_message`;
CREATE TABLE `t_customer_service_message` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
  `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID（用户标识）',
  `sender_type` TINYINT NOT NULL COMMENT '发送方：1=用户，2=客服',
  `content` TEXT COMMENT '消息内容',
  `message_type` VARCHAR(20) DEFAULT 'text' COMMENT '消息类型：text/order',
  `user_nickname` VARCHAR(64) DEFAULT NULL COMMENT '用户昵称',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  INDEX `idx_session` (`session_id`),
  INDEX `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人工客服消息记录';
