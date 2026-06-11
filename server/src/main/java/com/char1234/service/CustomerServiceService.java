package com.char1234.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.char1234.entity.CustomerServiceMessage;

import java.util.List;

public interface CustomerServiceService extends IService<CustomerServiceMessage> {

    /** 保存消息 */
    void saveMessage(String sessionId, int senderType, String content, String messageType, String userNickname);

    /** 查询会话消息 */
    List<CustomerServiceMessage> getSessionMessages(String sessionId);

    /** 最近活跃会话列表 */
    List<String> getRecentSessions(int limit);
}
