package com.shangnantea.websocket.listener;

import com.shangnantea.websocket.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Redis过期事件监听器
 * 监听用户在线状态key的过期事件，当key过期时推送离线状态
 */
@Component
public class UserOnlineExpiredListener implements MessageListener {
    
    private static final Logger logger = LoggerFactory.getLogger(UserOnlineExpiredListener.class);
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Override
    public void onMessage(@NonNull Message message, @Nullable byte[] pattern) {
        try {
            String expiredKey = message.toString();
            logger.debug("收到Redis过期事件: key={}", expiredKey);
            
            // 只处理用户在线状态的key
            if (expiredKey != null && expiredKey.startsWith("online:user:")) {
                String userId = expiredKey.substring("online:user:".length());
                logger.info("用户在线状态key过期，推送离线状态: userId={}, key={}", userId, expiredKey);
                
                // 推送离线状态变更
                try {
                    webSocketService.notifyUserOnlineChanged(userId, false);
                } catch (Exception e) {
                    logger.warn("推送用户离线状态失败: userId={}, error={}", userId, e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("处理Redis过期事件失败: error={}", e.getMessage(), e);
        }
    }
}
