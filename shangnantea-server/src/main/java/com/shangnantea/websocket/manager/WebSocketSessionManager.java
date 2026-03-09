package com.shangnantea.websocket.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket会话管理器
 * 管理所有活跃的WebSocket连接
 */
@Component
public class WebSocketSessionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketSessionManager.class);
    
    // 存储用户ID到WebSocket会话的映射
    // 一个用户可能有多个会话（多个标签页/窗口）
    private final Map<String, java.util.Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    
    /**
     * 添加会话
     */
    public void addSession(String userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                    .add(session);
        logger.debug("添加WebSocket会话: userId={}, sessionId={}, 当前会话数={}", 
            userId, session.getId(), userSessions.get(userId).size());
    }
    
    /**
     * 移除会话
     */
    public void removeSession(String userId, WebSocketSession session) {
        java.util.Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
            logger.debug("移除WebSocket会话: userId={}, sessionId={}, 剩余会话数={}", 
                userId, session.getId(), sessions.size());
        }
    }
    
    /**
     * 获取用户的所有会话
     */
    public java.util.Set<WebSocketSession> getSessions(String userId) {
        return userSessions.getOrDefault(userId, java.util.Collections.emptySet());
    }
    
    /**
     * 检查用户是否在线（有活跃的WebSocket连接）
     */
    public boolean isUserOnline(String userId) {
        java.util.Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return false;
        }
        // 清理已关闭的会话
        sessions.removeIf(session -> !session.isOpen());
        return !sessions.isEmpty();
    }
    
    /**
     * 发送消息给指定用户的所有会话
     */
    public void sendToUser(String userId, String message) {
        java.util.Set<WebSocketSession> sessions = getSessions(userId);
        if (sessions.isEmpty()) {
            logger.debug("用户无活跃会话，无法发送消息: userId={}", userId);
            return;
        }
        
        int successCount = 0;
        int failCount = 0;
        
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new org.springframework.web.socket.TextMessage(message));
                    successCount++;
                } catch (IOException e) {
                    logger.error("发送WebSocket消息失败: userId={}, sessionId={}, error={}", 
                        userId, session.getId(), e.getMessage());
                    failCount++;
                }
            } else {
                failCount++;
            }
        }
        
        logger.debug("发送消息给用户: userId={}, 成功={}, 失败={}", userId, successCount, failCount);
    }
    
    /**
     * 广播消息给所有在线用户
     */
    public void broadcast(String message) {
        int totalSent = 0;
        for (Map.Entry<String, java.util.Set<WebSocketSession>> entry : userSessions.entrySet()) {
            String userId = entry.getKey();
            for (WebSocketSession session : entry.getValue()) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new org.springframework.web.socket.TextMessage(message));
                        totalSent++;
                    } catch (IOException e) {
                        logger.error("广播消息失败: userId={}, sessionId={}, error={}", 
                            userId, session.getId(), e.getMessage());
                    }
                }
            }
        }
        logger.debug("广播消息完成: 发送给{}个会话", totalSent);
    }
    
    /**
     * 获取所有在线用户ID
     */
    public java.util.Set<String> getOnlineUserIds() {
        // 清理已关闭的会话
        userSessions.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(session -> !session.isOpen());
            return entry.getValue().isEmpty();
        });
        return userSessions.keySet();
    }
}
