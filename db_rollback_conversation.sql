-- 回滚：删掉会话管理相关
DROP TABLE IF EXISTS `t_customer_conversation`;
ALTER TABLE `t_customer_service_message` DROP COLUMN `conversation_id`;
