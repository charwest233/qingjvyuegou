package com.char1234.controller;

import com.char1234.common.Result;
import com.char1234.context.JwtContextHolder;
import com.char1234.entity.AiMessage;
import com.char1234.entity.AiSession;
import com.char1234.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    /**
     * 可用模型列表
     */
    @GetMapping("/models")
    public Result<List<Map<String, String>>> listModels() {
        List<Map<String, String>> models = Arrays.asList(
                // DeepSeek 系列（默认推荐）
                modelItem("deepseek-ai/DeepSeek-V3", "DeepSeek V3（推荐）"),
                // 支持图片的视觉模型
                modelItem("Qwen/Qwen3-VL-30B-A3B-Instruct", "Qwen3-VL-30B（支持图片识别）"),
                modelItem("Qwen/Qwen3-VL-8B-Instruct", "Qwen3-VL-8B（支持图片识别）"),
                // 图片生成模型（免费）
                modelItem("Kwai-Kolors/Kolors", "Kolors 图片生成（免费）")
        );
        return Result.success(models);
    }

    /**
     * 创建新会话
     */
    @PostMapping("/session")
    public Result<AiSession> createSession(@RequestBody Map<String, String> body) {
        Long userId = JwtContextHolder.get().principalId();
        String model = body.getOrDefault("model", "deepseek-ai/DeepSeek-V3");
        AiSession session = aiChatService.createSession(userId, model);
        return Result.success(session);
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/session/list")
    public Result<List<AiSession>> listSessions() {
        Long userId = JwtContextHolder.get().principalId();
        return Result.success(aiChatService.listSessions(userId));
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/session/{id}")
    public Result<String> deleteSession(@PathVariable Long id) {
        Long userId = JwtContextHolder.get().principalId();
        if (aiChatService.deleteSession(userId, id)) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    /**
     * 获取会话消息列表
     */
    @GetMapping("/session/{id}/messages")
    public Result<List<AiMessage>> listMessages(@PathVariable Long id) {
        return Result.success(aiChatService.listMessages(id));
    }

    /**
     * SSE 流式聊天
     */
    @PostMapping("/chat/{sessionId}")
    public SseEmitter chat(
            @PathVariable Long sessionId,
            @RequestParam("text") String text,
            @RequestParam(value = "model", required = false, defaultValue = "") String model,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        Long userId = JwtContextHolder.get().principalId();
        SseEmitter emitter = new SseEmitter(180_000L);
        aiChatService.streamChat(userId, sessionId, text, model, image, emitter);
        return emitter;
    }

    private static Map<String, String> modelItem(String id, String name) {
        Map<String, String> item = new HashMap<>();
        item.put("id", id);
        item.put("name", name);
        return item;
    }
}
