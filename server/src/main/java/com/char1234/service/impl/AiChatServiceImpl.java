package com.char1234.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.char1234.ai.AiCustomerService;
import com.char1234.entity.AiMessage;
import com.char1234.entity.AiSession;
import com.char1234.mapper.AiMessageMapper;
import com.char1234.mapper.AiSessionMapper;
import com.char1234.service.AiChatService;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final int MAX_CONTEXT_MESSAGES = 20;

    @Autowired
    private AiSessionMapper sessionMapper;

    @Autowired
    private AiMessageMapper messageMapper;

    @Autowired
    private AiCustomerService aiCustomerService;

    @Autowired
    private ChatMemoryProvider chatMemoryProvider;

    @Override
    @Transactional
    public AiSession createSession(Long userId, String model) {
        AiSession session = new AiSession();
        session.setUserId(userId);
        session.setModel(model != null ? model : "deepseek-ai/DeepSeek-V3");
        session.setTitle("新对话");
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.insert(session);
        return session;
    }

    @Override
    public List<AiSession> listSessions(Long userId) {
        return sessionMapper.selectList(
                new LambdaQueryWrapper<AiSession>()
                        .eq(AiSession::getUserId, userId)
                        .orderByDesc(AiSession::getUpdatedAt)
        );
    }

    @Override
    @Transactional
    public boolean deleteSession(Long userId, Long sessionId) {
        AiSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return false;
        }
        sessionMapper.deleteById(sessionId);
        // 同时清除内存中的 ChatMemory
        return true;
    }

    @Override
    public List<AiMessage> listMessages(Long sessionId) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<AiMessage>()
                        .eq(AiMessage::getSessionId, sessionId)
                        .orderByAsc(AiMessage::getCreatedAt)
        );
    }

    @Override
    @Async
    public void streamChat(Long userId, Long sessionId, String text, MultipartFile image, SseEmitter emitter) {
        try {
            // 1. 保存用户消息
            AiMessage userMsg = new AiMessage();
            userMsg.setSessionId(sessionId);
            userMsg.setRole("user");
            userMsg.setContent(text);
            userMsg.setCreatedAt(LocalDateTime.now());

            if (image != null && !image.isEmpty()) {
                userMsg.setImageUrl(text); // 简化：先忽略图片上传逻辑
            }
            messageMapper.insert(userMsg);

            // 2. 更新会话标题（仅首条消息）
            AiSession session = sessionMapper.selectById(sessionId);
            if (session != null && ("新对话".equals(session.getTitle()) || session.getTitle() == null)) {
                String title = text.length() > 15 ? text.substring(0, 15) + "..." : text;
                session.setTitle(title);
                session.setUpdatedAt(LocalDateTime.now());
                sessionMapper.updateById(session);
            }

            // 3. 初始化 ChatMemory：从 DB 加载历史消息
            ChatMemory memory = chatMemoryProvider.get(sessionId.toString());
            if (memory.messages().isEmpty()) {
                List<AiMessage> dbMessages = messageMapper.selectList(
                        new LambdaQueryWrapper<AiMessage>()
                                .eq(AiMessage::getSessionId, sessionId)
                                .orderByAsc(AiMessage::getCreatedAt)
                );
                // 取最近 MAX_CONTEXT_MESSAGES 条
                List<AiMessage> recentHistory = dbMessages.size() > MAX_CONTEXT_MESSAGES
                        ? dbMessages.subList(dbMessages.size() - MAX_CONTEXT_MESSAGES, dbMessages.size())
                        : dbMessages;

                for (AiMessage msg : recentHistory) {
                    if ("user".equals(msg.getRole()) && msg.getContent() != null) {
                        memory.add(UserMessage.from(msg.getContent()));
                    } else if ("assistant".equals(msg.getRole()) && msg.getContent() != null) {
                        memory.add(dev.langchain4j.data.message.AiMessage.from(msg.getContent()));
                    }
                }
            }

            // 4. 调用 AiCustomerService 流式对话
            final StringBuilder fullResponse = new StringBuilder();

            TokenStream tokenStream = aiCustomerService.streamChat(sessionId.toString(), text);
            tokenStream
                    .onPartialResponse(chunk -> {
                        // 流式 token 到达
                        fullResponse.append(chunk);
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("message")
                                    .data(chunk, org.springframework.http.MediaType.TEXT_PLAIN));
                        } catch (IOException e) {
                            log.warn("SSE 发送失败，可能客户端已断开: sessionId={}", sessionId);
                            throw new RuntimeException(e);
                        }
                    })
                    .onCompleteResponse(response -> {
                        // 流结束
                        try {
                            // 5. 保存 AI 回复到数据库
                            AiMessage aiMsg = new AiMessage();
                            aiMsg.setSessionId(sessionId);
                            aiMsg.setRole("assistant");
                            aiMsg.setContent(fullResponse.toString());
                            aiMsg.setCreatedAt(LocalDateTime.now());
                            messageMapper.insert(aiMsg);

                            // 6. 更新会话时间
                            session.setUpdatedAt(LocalDateTime.now());
                            sessionMapper.updateById(session);

                            // 通知前端流结束
                            emitter.send(SseEmitter.event().name("done").data(""));
                            emitter.complete();

                            log.info("AI对话完成: sessionId={}, tokens={}",
                                    sessionId, fullResponse.length());
                        } catch (IOException e) {
                            log.warn("SSE 完成通知发送失败: sessionId={}", sessionId);
                        }
                    })
                    .onError(error -> {
                        // 流式错误
                        log.error("AI streaming error: sessionId={}", sessionId, error);
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data("抱歉，AI回复出错了，请稍后再试。",
                                            org.springframework.http.MediaType.TEXT_PLAIN));
                        } catch (IOException ignored) {
                        }
                        emitter.complete();
                    })
                    .start();

        } catch (Exception e) {
            log.error("AI流式聊天异常: sessionId={}", sessionId, e);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("抱歉，AI回复出错了，请稍后再试。",
                                org.springframework.http.MediaType.TEXT_PLAIN));
                emitter.complete();
            } catch (Exception ignored) {
            }
        }
    }
}
