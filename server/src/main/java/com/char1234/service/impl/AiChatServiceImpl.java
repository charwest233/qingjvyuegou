package com.char1234.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.char1234.entity.AiMessage;
import com.char1234.entity.AiSession;
import com.char1234.mapper.AiMessageMapper;
import com.char1234.mapper.AiSessionMapper;
import com.char1234.service.AiChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final String SYSTEM_PROMPT = "你是一个专业的电商导购助手，名叫「悦选导购」。你的任务是帮助用户挑选合适的商品，解答购物疑问。" +
            "回答要简洁专业、热情友好。你可以推荐商品品类、对比产品特点、提供选购建议。如果用户问与购物无关的问题，礼貌地引导回购物话题。";

    private static final int MAX_CONTEXT_MESSAGES = 20;

    @Autowired
    private AiSessionMapper sessionMapper;

    @Autowired
    private AiMessageMapper messageMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${ai.siliconflow.base-url}")
    private String baseUrl;

    @Value("${ai.siliconflow.api-key}")
    private String apiKey;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

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
    @Transactional
    public void streamChat(Long userId, Long sessionId, String text, MultipartFile image, SseEmitter emitter) {
        try {
            // 1. 保存用户消息
            AiMessage userMsg = new AiMessage();
            userMsg.setSessionId(sessionId);
            userMsg.setRole("user");
            userMsg.setContent(text);
            userMsg.setCreatedAt(LocalDateTime.now());

            if (image != null && !image.isEmpty()) {
                String imageUrl = saveImage(image);
                userMsg.setImageUrl(imageUrl);
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

            // 3. 加载上下文消息
            List<AiMessage> history = messageMapper.selectList(
                    new LambdaQueryWrapper<AiMessage>()
                            .eq(AiMessage::getSessionId, sessionId)
                            .orderByAsc(AiMessage::getCreatedAt)
            );

            // 4. 构建 OpenAI 兼容的 messages
            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> sysMsg = new HashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", SYSTEM_PROMPT);
            messages.add(sysMsg);

            // 取最近 MAX_CONTEXT_MESSAGES 条历史消息（跳过 system prompt）
            List<AiMessage> recentHistory = history.size() > MAX_CONTEXT_MESSAGES
                    ? history.subList(history.size() - MAX_CONTEXT_MESSAGES, history.size())
                    : history;

            for (AiMessage msg : recentHistory) {
                Map<String, Object> m = new HashMap<>();
                m.put("role", msg.getRole());
                if (msg.getImageUrl() != null && !msg.getImageUrl().isEmpty()) {
                    // 多模态内容
                    List<Map<String, Object>> contentParts = new ArrayList<>();
                    Map<String, Object> textPart = new HashMap<>();
                    textPart.put("type", "text");
                    textPart.put("text", msg.getContent() != null ? msg.getContent() : "");
                    contentParts.add(textPart);

                    Map<String, Object> imagePart = new HashMap<>();
                    imagePart.put("type", "image_url");
                    Map<String, String> imageUrl = new HashMap<>();
                    imageUrl.put("url", msg.getImageUrl());
                    imagePart.put("image_url", imageUrl);
                    contentParts.add(imagePart);

                    m.put("content", contentParts);
                } else {
                    m.put("content", msg.getContent() != null ? msg.getContent() : "");
                }
                messages.add(m);
            }

            // 5. 调用 SiliconFlow API
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", session != null ? session.getModel() : "deepseek-ai/DeepSeek-V3");
            requestBody.put("messages", messages);
            requestBody.put("stream", true);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2048);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            StringBuilder fullResponse = new StringBuilder();

            // 6. 流式请求并转发 SSE
            restTemplate.execute(
                    baseUrl + "/v1/chat/completions",
                    org.springframework.http.HttpMethod.POST,
                    clientHttpRequest -> {
                        clientHttpRequest.getHeaders().putAll(headers);
                        try (java.io.OutputStream os = clientHttpRequest.getBody()) {
                            objectMapper.writeValue(os, requestBody);
                        }
                    },
                    clientHttpResponse -> {
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(clientHttpResponse.getBody(), StandardCharsets.UTF_8))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.startsWith("data: ")) {
                                    String data = line.substring(6).trim();
                                    if ("[DONE]".equals(data)) {
                                        emitter.send(SseEmitter.event().name("done").data(""));
                                        break;
                                    }
                                    try {
                                        JsonNode node = objectMapper.readTree(data);
                                        JsonNode choices = node.get("choices");
                                        if (choices != null && choices.size() > 0) {
                                            JsonNode delta = choices.get(0).get("delta");
                                            if (delta != null && delta.get("content") != null) {
                                                String chunk = delta.get("content").asText();
                                                fullResponse.append(chunk);
                                                emitter.send(SseEmitter.event()
                                                        .name("message")
                                                        .data(chunk, MediaType.TEXT_PLAIN));
                                            }
                                        }
                                    } catch (Exception e) {
                                        log.warn("解析SSE数据失败: {}", data, e);
                                    }
                                }
                            }
                        }
                        emitter.complete();
                        return null;
                    }
            );

            // 7. 保存 AI 回复到数据库
            AiMessage aiMsg = new AiMessage();
            aiMsg.setSessionId(sessionId);
            aiMsg.setRole("assistant");
            aiMsg.setContent(fullResponse.toString());
            aiMsg.setCreatedAt(LocalDateTime.now());
            messageMapper.insert(aiMsg);

            // 8. 更新会话时间
            session.setUpdatedAt(LocalDateTime.now());
            sessionMapper.updateById(session);

        } catch (Exception e) {
            log.error("AI流式聊天异常: sessionId={}", sessionId, e);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("抱歉，AI回复出错了，请稍后再试。", MediaType.TEXT_PLAIN));
                emitter.complete();
            } catch (Exception ignored) {
            }
        }
    }

    private String saveImage(MultipartFile file) {
        try {
            String dir = uploadDir + "/ai/" + LocalDateTime.now().toLocalDate().toString();
            Files.createDirectories(Paths.get(dir));
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(dir, filename);
            Files.copy(file.getInputStream(), filePath);
            return "/uploads/ai/" + LocalDateTime.now().toLocalDate().toString() + "/" + filename;
        } catch (Exception e) {
            log.error("保存AI上传图片失败", e);
            return null;
        }
    }
}
