package com.shangnantea.websocket.service;

import com.shangnantea.mapper.ChatSessionMapper;
import com.shangnantea.mapper.ShopMapper;
import com.shangnantea.mapper.UserFollowMapper;
import com.shangnantea.model.entity.shop.Shop;
import com.shangnantea.websocket.manager.WebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * WebSocket服务类
 * 提供推送消息的便捷方法
 */
@Service
public class WebSocketService {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);
    
    @Autowired
    private WebSocketSessionManager sessionManager;
    
    @Autowired
    private UserFollowMapper userFollowMapper;
    
    @Autowired
    private ChatSessionMapper chatSessionMapper;
    
    @Autowired
    private ShopMapper shopMapper;
    
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
        // 仅推送给管理员，避免普通用户获取全站在线列表
        sessionManager.broadcastToAdmins(message);
        logger.debug("广播在线用户列表更新: 在线用户数={}", onlineUserIds.size());
    }
    
    /**
     * 用户上线/下线事件：
     * - 给管理员推送全量 onlineUsersUpdate（管理员需要全站视图）
     * - 给与该用户有关联的用户推送 onlineStatus（关注/会话/店铺关注等）
     *
     * @param changedUserId 发生状态变化的用户ID
     * @param online 是否在线
     */
    public void notifyUserOnlineChanged(String changedUserId, boolean online) {
        if (changedUserId == null || changedUserId.trim().isEmpty()) {
            return;
        }
        
        // 1) 管理员全量更新
        try {
            broadcastOnlineUsersUpdate();
        } catch (Exception e) {
            logger.debug("推送管理员在线列表更新失败，不影响流程: userId={}, error={}", changedUserId, e.getMessage());
        }
        
        // 2) 构造增量消息（只推送“某个用户状态变化”）
        String onlineStatusMsg = String.format(
                "{\"type\":\"onlineStatus\",\"userId\":\"%s\",\"online\":%s}",
                changedUserId, online
        );
        
        // 3) 计算需要接收该消息的用户集合
        Set<String> receivers = new HashSet<>();
        
        // 3.1 关注了该用户的人（粉丝）
        try {
            List<String> followerIds = userFollowMapper.selectFollowerIdsByFollow("user", changedUserId);
            if (followerIds != null) {
                receivers.addAll(followerIds);
            }
        } catch (Exception e) {
            logger.debug("查询用户粉丝失败，不影响推送: userId={}, error={}", changedUserId, e.getMessage());
        }
        
        // 3.2 与该用户有会话的人（最近会话列表来源）
        try {
            List<String> relatedUserIds = chatSessionMapper.selectRelatedUserIds(changedUserId);
            if (relatedUserIds != null) {
                receivers.addAll(relatedUserIds);
            }
        } catch (Exception e) {
            logger.debug("查询会话关联用户失败，不影响推送: userId={}, error={}", changedUserId, e.getMessage());
        }
        
        // 3.3 关注了该用户店铺的人（如果该用户是店主）
        try {
            Shop shop = shopMapper.selectByOwnerId(changedUserId);
            if (shop != null && shop.getId() != null) {
                List<String> shopFollowerIds = userFollowMapper.selectFollowerIdsByFollow("shop", shop.getId());
                if (shopFollowerIds != null) {
                    receivers.addAll(shopFollowerIds);
                }
            }
        } catch (Exception e) {
            logger.debug("查询店铺粉丝失败，不影响推送: userId={}, error={}", changedUserId, e.getMessage());
        }
        
        // 4) 只推送给当前在线的用户（去除自己，避免自我刷新引起抖动）
        receivers.remove(changedUserId);
        int onlineReceivers = 0;
        for (String receiverId : receivers) {
            // 只推送给当前在线的用户
            if (sessionManager.isUserOnline(receiverId)) {
                try {
                    sessionManager.sendToUser(receiverId, onlineStatusMsg);
                    onlineReceivers++;
                } catch (Exception e) {
                    logger.debug("推送在线状态失败，不影响其他用户: receiverId={}, userId={}, error={}",
                            receiverId, changedUserId, e.getMessage());
                }
            }
        }
        
        logger.debug("用户在线状态推送完成: changedUserId={}, online={}, 总接收者={}, 在线接收者={}", 
                changedUserId, online, receivers.size(), onlineReceivers);
    }
}
