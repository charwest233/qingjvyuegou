package com.char1234.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.char1234.entity.CustomerServiceMessage;
import com.char1234.mapper.CustomerServiceMessageMapper;
import com.char1234.service.CustomerServiceService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerServiceServiceImpl
        extends ServiceImpl<CustomerServiceMessageMapper, CustomerServiceMessage>
        implements CustomerServiceService {

    @Override
    public void saveMessage(String sessionId, int senderType, String content, String messageType, String userNickname) {
        CustomerServiceMessage msg = new CustomerServiceMessage();
        msg.setSessionId(sessionId);
        msg.setSenderType(senderType);
        msg.setContent(content);
        msg.setMessageType(messageType);
        msg.setUserNickname(userNickname);
        msg.setCreatedAt(LocalDateTime.now());
        save(msg);
    }

    @Override
    public List<CustomerServiceMessage> getSessionMessages(String sessionId) {
        return baseMapper.findBySessionId(sessionId);
    }

    @Override
    public List<String> getRecentSessions(int limit) {
        return baseMapper.findRecentSessions(limit);
    }
}
