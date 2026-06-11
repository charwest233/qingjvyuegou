package com.char1234.websocket;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.char1234.config.SpringContextHolder;
import com.char1234.service.CustomerServiceService;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 人工客服 WebSocket 端点
 * <p>
 * userKey = userId（持久标识，存储和会话聚合用）<br>
 * wsSid  = WebSocket session ID（连接路由用，每次连接变化）
 * <p>
 * 用户端：ws://localhost:8080/ws/customer-service/{token}
 * 管理端：ws://localhost:8080/ws/customer-service/agent_admin
 */
@Slf4j
@Component
@ServerEndpoint("/ws/customer-service/{token}")
public class CustomerServiceEndpoint {

    /** userKey → WebSocket Session（路由转发用） */
    private static final Map<String, Session> USER_SESSIONS = new ConcurrentHashMap<>();
    /** userKey → nickname */
    private static final Map<String, String> USER_NAMES = new ConcurrentHashMap<>();
    /** agentKey → WebSocket Session */
    private static final Map<String, Session> AGENT_SESSIONS = new ConcurrentHashMap<>();

    private String userKey;   // 持久标识：userId 或 agent_xxx
    private String wsSid;      // WebSocket session ID（路由用）
    private boolean isAgent;

    private CustomerServiceService getCsService() {
        try { return SpringContextHolder.getBean(CustomerServiceService.class); }
        catch (Exception e) { return null; }
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        this.wsSid = session.getId();

        if (token != null && token.startsWith("agent_")) {
            this.isAgent = true;
            this.userKey = token;
            AGENT_SESSIONS.put(userKey, session);
            log.info("客服上线: {}", userKey);
        } else {
            this.isAgent = false;
            // token 格式：user_{userId}_{nickname}
            String name = "用户";
            String key = wsSid;
            if (token != null && token.startsWith("user_")) {
                String[] parts = token.substring(5).split("_", 2);
                key = parts[0];                        // userId
                name = parts.length > 1 ? parts[1] : "用户";
            }
            this.userKey = key;
            USER_SESSIONS.put(userKey, session);
            USER_NAMES.put(userKey, name);
            log.info("用户上线: key={} name={}", userKey, name);
            broadcastToAgents(buildUserMsg("system", "用户 " + name + " 接入咨询", userKey, name));
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到消息 isAgent={} userKey={} raw={}", isAgent, userKey, message);
        try {
            JSONObject msg = JSONUtil.parseObj(message);
            String content = msg.getStr("content", "");
            String targetUser = msg.getStr("targetUser");

            if (isAgent) {
                log.info("客服发消息 targetUser={} online={}", targetUser, USER_SESSIONS.containsKey(targetUser));
                // 客服回复 → 转发给目标用户 + 写 DB
                if (targetUser != null && USER_SESSIONS.containsKey(targetUser)) {
                    String json = buildMsg("message", content);
                    sendToSession(USER_SESSIONS.get(targetUser), json);
                    log.info("已转发给用户: target={}", targetUser);
                    CustomerServiceService svc = getCsService();
                    if (svc != null) {
                        svc.saveMessage(targetUser, 2, content, "text", USER_NAMES.get(targetUser));
                    }
                }
            } else {
                // 用户消息 → 转发客服 + 写 DB
                String name = USER_NAMES.getOrDefault(userKey, "匿名用户");
                String json = buildUserMsg("message", content, userKey, name);
                log.info("用户消息转发给 {} 个在线客服 msg={}", AGENT_SESSIONS.size(), json);
                broadcastToAgents(json);
                CustomerServiceService svc = getCsService();
                if (svc != null) {
                    svc.saveMessage(userKey, 1, content, "text", name);
                }
            }
        } catch (Exception e) {
            log.error("消息处理异常", e);
        }
    }

    @OnClose
    public void onClose() {
        if (isAgent) {
            AGENT_SESSIONS.remove(userKey);
            log.info("客服下线: {}", userKey);
        } else {
            // 用户离线不移除 USER_NAMES，保留昵称供历史查询
            USER_SESSIONS.remove(userKey);
            log.info("用户离线: {}", userKey);
        }
    }

    @OnError
    public void onError(Throwable error) {
        log.error("WebSocket error: {}", error.getMessage());
    }

    // ==================== 工具方法 ====================

    private void broadcastToAgents(String json) {
        AGENT_SESSIONS.values().forEach(s -> sendToSession(s, json));
    }

    private void sendToSession(Session session, String json) {
        if (session != null && session.isOpen()) {
            try { session.getBasicRemote().sendText(json); }
            catch (IOException e) { log.error("发送失败", e); }
        }
    }

    private static String buildMsg(String type, String content) {
        return JSONUtil.toJsonStr(Map.of(
                "type", type, "content", content,
                "time", System.currentTimeMillis()));
    }

    private static String buildUserMsg(String type, String content,
                                        String userKey, String nickname) {
        return JSONUtil.toJsonStr(Map.of(
                "type", type, "content", content,
                "sessionId", userKey, "nickname", nickname,
                "time", System.currentTimeMillis()));
    }
}
