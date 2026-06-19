package com.char1234.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.char1234.ai.AiCustomerService;
import com.char1234.config.AiConfig;
import com.char1234.entity.AiMessage;
import com.char1234.entity.AiSession;
import com.char1234.mapper.AiMessageMapper;
import com.char1234.mapper.AiSessionMapper;
import com.char1234.service.AiChatService;
import com.char1234.config.AiConfig;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final int MAX_CONTEXT_MESSAGES = 20;

    /** 支持多模态的模型关键字列表 */
    private static final List<String> VISION_MODEL_KEYWORDS = List.of("VL", "vision", "Visual", "Qwen2.5-VL");

    /** 图片生成模型关键字 */
    private static final String IMAGE_GEN_MODEL_KEYWORD = "Kolors";

    @Autowired
    private AiSessionMapper sessionMapper;

    @Autowired
    private AiMessageMapper messageMapper;

    @Autowired
    private AiCustomerService aiCustomerService;

    @Autowired
    private ChatMemoryProvider chatMemoryProvider;

    @Autowired
    private AiConfig aiConfig;

    /**
     * 判断模型名是否支持视觉（是否包含 VL / vision 等关键字）
     */
    private boolean isVisionModel(String modelName) {
        if (modelName == null) return false;
        String upper = modelName.toUpperCase();
        return VISION_MODEL_KEYWORDS.stream().anyMatch(kw -> upper.contains(kw.toUpperCase()));
    }

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
    public void streamChat(Long userId, Long sessionId, String text, String model, MultipartFile image, SseEmitter emitter) {
        try {
            // 1. 保存用户消息
            AiMessage userMsg = new AiMessage();
            userMsg.setSessionId(sessionId);
            userMsg.setRole("user");
            userMsg.setContent(text);
            userMsg.setCreatedAt(LocalDateTime.now());

            // 保存图片 URL（如果有）
            String uploadedImageUrl = null;
            if (image != null && !image.isEmpty()) {
                // 简单存储：暂不上传到云存储，仅记录有图片
                uploadedImageUrl = "[图片]";
                userMsg.setImageUrl(uploadedImageUrl);
            }
            messageMapper.insert(userMsg);

            // 2. 更新会话标题
            AiSession session = sessionMapper.selectById(sessionId);
            if (session != null && ("新对话".equals(session.getTitle()) || session.getTitle() == null)) {
                String title = text.length() > 15 ? text.substring(0, 15) + "..." : text;
                session.setTitle(title);
                session.setUpdatedAt(LocalDateTime.now());
                sessionMapper.updateById(session);
            }

            // 3. 初始化 ChatMemory（仅文本记忆）
            ChatMemory memory = chatMemoryProvider.get(sessionId.toString());
            if (memory.messages().isEmpty()) {
                List<AiMessage> dbMessages = messageMapper.selectList(
                        new LambdaQueryWrapper<AiMessage>()
                                .eq(AiMessage::getSessionId, sessionId)
                                .orderByAsc(AiMessage::getCreatedAt)
                );
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

            // 4. 使用前端传入的模型（更新到会话中，确保实时生效）
            String modelName = model != null && !model.isEmpty() ? model
                    : (session != null ? session.getModel() : "deepseek-ai/DeepSeek-V3");
            if (session != null && !modelName.equals(session.getModel())) {
                session.setModel(modelName);
                session.setUpdatedAt(LocalDateTime.now());
                sessionMapper.updateById(session);
            }
            boolean hasImage = image != null && !image.isEmpty();
            boolean isImageGen = modelName != null && modelName.contains(IMAGE_GEN_MODEL_KEYWORD);
            boolean useVision = hasImage && isVisionModel(modelName);

            if (isImageGen) {
                // 图片生成模型处理
                handleImageGeneration(sessionId, session, text, emitter, modelName);
            } else if (useVision) {
                // 视觉模型处理（带图片）
                handleVisionChat(sessionId, session, text, image, emitter, modelName);
            } else if (hasImage && !useVision) {
                // 当前模型不支持视觉，提示用户
                try {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data("当前模型不支持图片识别，请切换为支持图片的模型（如 Qwen2.5-VL）后再试。",
                                    org.springframework.http.MediaType.TEXT_PLAIN));
                    emitter.send(SseEmitter.event().name("done").data(""));
                    emitter.complete();
                } catch (IOException e) {
                    log.warn("SSE 发送失败: sessionId={}", sessionId);
                }
            } else {
                // 纯文本模型处理（无图片）
                handleTextChat(sessionId, session, text, emitter, memory);
            }

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

    /**
     * 处理纯文本对话（使用 AiCustomerService 代理，保留记忆和工具调用）
     */
    private void handleTextChat(Long sessionId, AiSession session, String text,
                                SseEmitter emitter, ChatMemory memory) {
        final StringBuilder fullResponse = new StringBuilder();

        TokenStream tokenStream = aiCustomerService.streamChat(sessionId.toString(), text);
        tokenStream
                .onPartialResponse(chunk -> {
                    fullResponse.append(chunk);
                    try {
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data(chunk, org.springframework.http.MediaType.TEXT_PLAIN));
                    } catch (IOException e) {
                        log.warn("SSE 发送失败: sessionId={}", sessionId);
                        throw new RuntimeException(e);
                    }
                })
                .onCompleteResponse(response -> {
                    try {
                        saveAiResponse(sessionId, session, fullResponse.toString(), emitter);
                    } catch (IOException e) {
                        log.warn("SSE 完成通知发送失败: sessionId={}", sessionId);
                    }
                })
                .onError(error -> {
                    log.error("AI streaming error: sessionId={}", sessionId, error);
                    sendErrorToEmitter(emitter);
                })
                .start();
    }

    /**
     * 处理带图片的视觉对话（直接使用模型，不支持工具调用和记忆）
     */
    private void handleVisionChat(Long sessionId, AiSession session, String text,
                                  MultipartFile image, SseEmitter emitter, String modelName) throws IOException {
        // 1. 构造多模态用户消息
        List<Content> contents = new ArrayList<>();
        contents.add(TextContent.from(text != null && !text.isEmpty() ? text : "请描述这张图片"));

        // 2. 将图片转为 base64
        byte[] imageBytes = image.getBytes();
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String mimeType = image.getContentType();
        if (mimeType == null) {
            mimeType = "image/jpeg";
        }
        String dataUri = "data:" + mimeType + ";base64," + base64;
        contents.add(ImageContent.from(dataUri));

        UserMessage userMessage = UserMessage.from(contents.toArray(new Content[0]));

        // 3. 创建视觉模型实例并生成回复
        OpenAiStreamingChatModel visionModel = aiConfig.createStreamingModel(modelName);

        final StringBuilder fullResponse = new StringBuilder();

        visionModel.chat(List.of(userMessage), new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                fullResponse.append(partialResponse);
                try {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data(partialResponse, org.springframework.http.MediaType.TEXT_PLAIN));
                } catch (IOException e) {
                    log.warn("SSE 发送失败: sessionId={}", sessionId);
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                try {
                    saveAiResponse(sessionId, session, fullResponse.toString(), emitter);
                    log.info("视觉对话完成: sessionId={}, tokens={}", sessionId, fullResponse.length());
                } catch (IOException e) {
                    log.warn("SSE 完成通知发送失败: sessionId={}", sessionId);
                }
            }

            @Override
            public void onError(Throwable error) {
                log.error("视觉模型 streaming error: sessionId={}", sessionId, error);
                sendErrorToEmitter(emitter);
            }
        });
    }

    /**
     * 处理图片生成（调用 SiliconFlow images API）
     */
    private void handleImageGeneration(Long sessionId, AiSession session, String text,
                                       SseEmitter emitter, String modelName) {
        try {
            String prompt = (text != null && !text.isEmpty()) ? text : "一张美丽的风景画";
            String jsonBody = String.format(
                    "{\"model\":\"%s\",\"prompt\":\"%s\",\"image_size\":\"1024x1024\",\"batch_size\":1,\"num_inference_steps\":20}",
                    modelName, prompt.replace("\"", "\\\"").replace("\n", " ")
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiConfig.getBaseUrl() + "/v1/images/generations"))
                    .header("Authorization", "Bearer " + aiConfig.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // 解析 JSON 获取图片 URL
                String responseBody = response.body();
                // 简单解析：提取第一个 images[0].url
                String imageUrl = extractImageUrl(responseBody);

                if (imageUrl != null) {
                    // 保存 AI 消息（内容为图片 Markdown）
                    String markdownContent = "![生成的图片](" + imageUrl + ")";
                    AiMessage aiMsg = new AiMessage();
                    aiMsg.setSessionId(sessionId);
                    aiMsg.setRole("assistant");
                    aiMsg.setContent(markdownContent);
                    aiMsg.setImageUrl(imageUrl);
                    aiMsg.setCreatedAt(LocalDateTime.now());
                    messageMapper.insert(aiMsg);

                    session.setUpdatedAt(LocalDateTime.now());
                    sessionMapper.updateById(session);

                    // 发送 SSE
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data(markdownContent, org.springframework.http.MediaType.TEXT_PLAIN));
                    emitter.send(SseEmitter.event().name("done").data(""));
                    emitter.complete();
                } else {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("图片生成失败：无法解析返回结果", org.springframework.http.MediaType.TEXT_PLAIN));
                    emitter.complete();
                }
            } else {
                log.error("图片生成API调用失败 status={}, body={}", response.statusCode(), response.body());
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("图片生成失败，请稍后重试", org.springframework.http.MediaType.TEXT_PLAIN));
                emitter.complete();
            }
        } catch (Exception e) {
            log.error("图片生成异常 sessionId={}", sessionId, e);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("图片生成出错了：" + e.getMessage(), org.springframework.http.MediaType.TEXT_PLAIN));
                emitter.complete();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * 从图片生成 API 响应中提取第一个图片 URL
     */
    private String extractImageUrl(String responseBody) {
        try {
            // 查找 "images":[{"url":" 或类似格式
            String searchKey = "\"url\":\"";
            int urlStart = responseBody.indexOf(searchKey);
            if (urlStart < 0) {
                // 尝试其他格式
                searchKey = "\"url\": \"";
                urlStart = responseBody.indexOf(searchKey);
            }
            if (urlStart < 0) return null;
            urlStart += searchKey.length();
            int urlEnd = responseBody.indexOf("\"", urlStart);
            if (urlEnd < 0) return null;
            return responseBody.substring(urlStart, urlEnd);
        } catch (Exception e) {
            log.warn("解析图片URL失败 body={}", responseBody, e);
            return null;
        }
    }

    /**
     * 保存 AI 回复并通知前端流结束
     */
    private void saveAiResponse(Long sessionId, AiSession session, String responseText,
                                SseEmitter emitter) throws IOException {
        AiMessage aiMsg = new AiMessage();
        aiMsg.setSessionId(sessionId);
        aiMsg.setRole("assistant");
        aiMsg.setContent(responseText);
        aiMsg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(aiMsg);

        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(session);

        emitter.send(SseEmitter.event().name("done").data(""));
        emitter.complete();
    }

    private void sendErrorToEmitter(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data("抱歉，AI回复出错了，请稍后再试。",
                            org.springframework.http.MediaType.TEXT_PLAIN));
        } catch (IOException ignored) {
        }
        emitter.complete();
    }
}
