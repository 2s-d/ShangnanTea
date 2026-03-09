package com.shangnantea.websocket.service;

import com.shangnantea.websocket.manager.WebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * WebSocket服务类
 * 提供推送消息的便捷方法
 */
@Service
public class WebSocketService {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);
    
    @Autowired
    private WebSocketSessionManager sessionManager;
    
    /**
     * 推送在线状态更新给指定用户
     * @param userId 用户ID
     * @param onlineStatus 在线状态
     */
    public void pushOnlineStatus(String userId, boolean onlineStatus) {
        String message = String.format(
            "{\"type\":\"onlineStatus\",\"userId\":\"%s\",\"online\":%s}",
            userId, onlineStatus
        );
        sessionManager.sendToUser(userId, message);
        logger.debug("推送在线状态: userId={}, online={}", userId, onlineStatus);
    }
    
    /**
     * 推送通知消息给指定用户
     * @param userId 用户ID
     * @param notification 通知内容
     */
    public void pushNotification(String userId, Object notification) {
        try {
            String message = String.format(
                "{\"type\":\"notification\",\"data\":%s}",
                com.alibaba.fastjson2.JSON.toJSONString(notification)
            );
            sessionManager.sendToUser(userId, message);
            logger.debug("推送通知: userId={}", userId);
        } catch (Exception e) {
            logger.error("推送通知失败: userId={}, error={}", userId, e.getMessage());
        }
    }
    
    /**
     * 推送聊天消息给指定用户
     * @param userId 用户ID
     * @param chatMessage 聊天消息
     */
    public void pushChatMessage(String userId, Object chatMessage) {
        try {
            String message = String.format(
                "{\"type\":\"chatMessage\",\"data\":%s}",
                com.alibaba.fastjson2.JSON.toJSONString(chatMessage)
            );
            sessionManager.sendToUser(userId, message);
            logger.debug("推送聊天消息: userId={}", userId);
        } catch (Exception e) {
            logger.error("推送聊天消息失败: userId={}, error={}", userId, e.getMessage());
        }
    }
    
    /**
     * 广播在线用户列表更新（给管理员）
     */
    public void broadcastOnlineUsersUpdate() {
        java.util.Set<String> onlineUserIds = sessionManager.getOnlineUserIds();
        String message = String.format(
            "{\"type\":\"onlineUsersUpdate\",\"onlineUserIds\":%s}",
            com.alibaba.fastjson2.JSON.toJSONString(onlineUserIds)
        );
        sessionManager.broadcast(message);
        logger.debug("广播在线用户列表更新: 在线用户数={}", onlineUserIds.size());
    }
}
