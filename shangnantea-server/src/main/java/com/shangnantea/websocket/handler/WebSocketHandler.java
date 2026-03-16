package com.shangnantea.websocket.handler;

import com.shangnantea.websocket.manager.WebSocketSessionManager;
import com.shangnantea.websocket.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket消息处理器
 * 处理客户端连接、断开、消息接收和发送
 */
@Component
public class WebSocketHandler extends TextWebSocketHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    
    @Autowired
    private WebSocketSessionManager sessionManager;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;
    
    /**
     * 连接建立后触发
     */
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        // 从session属性中获取用户ID（由拦截器设置）
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            sessionManager.addSession(userId, session);
            logger.info("WebSocket连接建立: userId={}, sessionId={}", userId, session.getId());
            
            // 记录在线状态（基于WebSocket连接）
            markUserOnline(userId);
            
            // 发送连接成功消息
            sendMessage(session, "{\"type\":\"connected\",\"message\":\"连接成功\"}");
            
            // 广播在线用户列表更新（通知所有管理员）
            try {
                webSocketService.notifyUserOnlineChanged(userId, true);
            } catch (Exception e) {
                logger.debug("广播在线用户列表更新失败，不影响连接: userId={}, error={}", userId, e.getMessage());
            }
        } else {
            logger.warn("WebSocket连接建立失败: 未找到用户ID, sessionId={}", session.getId());
            session.close(CloseStatus.BAD_DATA.withReason("未找到用户ID"));
        }
    }
    
    /**
     * 接收客户端消息
     */
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        String payload = message.getPayload();
        logger.debug("收到WebSocket消息: userId={}, message={}", userId, payload);

        // 处理通道心跳（ping0）：只用于保持通道存活 / 探测半开连接，不刷新在线TTL
        if ("ping0".equals(payload)) {
            sendMessage(session, "pong0");
            return;
        }

        // 处理业务心跳（ping1）：用于刷新用户在线TTL
        if ("ping1".equals(payload)) {
            if (userId != null) {
                markUserOnline(userId);
            }
            sendMessage(session, "pong1");
            return;
        }
        
        // 可以在这里处理其他类型的消息
        // 例如：客户端请求获取在线用户列表等
        try {
            if (payload != null && payload.trim().startsWith("{")) {
                // 简单解析JSON消息，根据type进行路由（当前只关心在线用户列表请求）
                com.alibaba.fastjson2.JSONObject json = com.alibaba.fastjson2.JSON.parseObject(payload);
                String type = json.getString("type");
                if ("requestOnlineUsers".equals(type)) {
                    logger.debug("收到管理员请求在线用户列表: userId={}", userId);
                    // 复用现有逻辑：向所有管理员广播一条 onlineUsersUpdate
                    webSocketService.broadcastOnlineUsersUpdate();
                }
            }
        } catch (Exception e) {
            logger.debug("处理业务消息失败: userId={}, payload={}, error={}", userId, payload, e.getMessage());
        }
    }
    
    /**
     * 连接关闭后触发
     */
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            sessionManager.removeSession(userId, session);
            logger.info("WebSocket连接关闭: userId={}, sessionId={}, status={}", 
                userId, session.getId(), status);
            
            // 连接关闭时清理在线状态（兜底依然有TTL）
            clearUserOnline(userId);
            
            // 广播在线用户列表更新（通知所有管理员）
            try {
                webSocketService.notifyUserOnlineChanged(userId, false);
            } catch (Exception e) {
                logger.debug("广播在线用户列表更新失败，不影响断开: userId={}, error={}", userId, e.getMessage());
            }
        }
    }
    
    /**
     * 发送消息给指定session
     */
    private void sendMessage(WebSocketSession session, @NonNull String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            logger.error("发送WebSocket消息失败: sessionId={}, error={}", 
                session.getId(), e.getMessage());
        }
    }
    
    /**
     * 标记用户在线（基于WebSocket活动），并设置短TTL。
     * 如果之前是离线状态（key不存在），会推送上线状态变更。
     */
    private void markUserOnline(String userId) {
        if (redisTemplate == null || userId == null) {
            logger.warn("标记用户在线失败: redisTemplate={}, userId={}", redisTemplate != null, userId);
            return;
        }
        try {
            String key = "online:user:" + userId;
            
            // 检查之前是否在线（key是否存在）
            Boolean wasOnline = redisTemplate.hasKey(key);

            // 业务心跳（ping1）为10秒一跳（测试用），在线TTL 给 15 秒（测试用）
            // 前端无操作10秒会停止 ping1，15秒后Redis key过期，触发离线状态推送
            redisTemplate.opsForValue().set(key, "1", 15, TimeUnit.SECONDS);
            
            // 如果之前是离线状态（key不存在），现在重新上线，推送状态变更
            if (wasOnline == null || !wasOnline) {
                logger.debug("用户从离线状态恢复在线: userId={}", userId);
                try {
                    webSocketService.notifyUserOnlineChanged(userId, true);
                } catch (Exception e) {
                    logger.debug("推送用户上线状态失败，不影响流程: userId={}, error={}", userId, e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.warn("记录用户在线状态失败(WebSocket), userId={}, error={}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * 清理用户在线状态（在WebSocket连接关闭或显式下线时调用）。
     */
    private void clearUserOnline(String userId) {
        if (redisTemplate == null || userId == null) {
            return;
        }
        try {
            String key = "online:user:" + userId;
            Boolean removed = redisTemplate.delete(key);
            logger.debug("清理用户在线状态(WebSocket): userId={}, key={}, removed={}", userId, key, removed);
        } catch (Exception e) {
            logger.debug("清理用户在线状态失败(WebSocket), userId={}, error={}", userId, e.getMessage());
        }
    }
}
