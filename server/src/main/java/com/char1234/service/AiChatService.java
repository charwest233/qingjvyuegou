package com.char1234.service;

import com.char1234.entity.AiMessage;
import com.char1234.entity.AiSession;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface AiChatService {

    /**
     * 创建新会话
     */
    AiSession createSession(Long userId, String model);

    /**
     * 获取用户会话列表
     */
    List<AiSession> listSessions(Long userId);

    /**
     * 删除会话
     */
    boolean deleteSession(Long userId, Long sessionId);

    /**
     * 获取会话消息列表
     */
    List<AiMessage> listMessages(Long sessionId);

    /**
     * SSE 流式聊天
     */
    void streamChat(Long userId, Long sessionId, String text, String model, MultipartFile image, SseEmitter emitter);
}
