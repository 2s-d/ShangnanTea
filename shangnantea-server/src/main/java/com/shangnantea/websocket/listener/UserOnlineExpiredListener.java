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
        logger.info("========== [阶段3] 收到Redis过期事件 ==========");
        
        try {
            String expiredKey = message.toString();
            String patternStr = pattern != null ? new String(pattern) : "null";
            logger.info("[阶段3-步骤1] 解析过期事件: key={}, pattern={}, messageBody={}", 
                expiredKey, patternStr, message.getBody() != null ? new String(message.getBody()) : "null");
            
            // 只处理用户在线状态的key
            if (expiredKey != null && expiredKey.startsWith("online:user:")) {
                String userId = expiredKey.substring("online:user:".length());
                logger.info("[阶段3-步骤2] 匹配到用户在线状态key: userId={}, key={}", userId, expiredKey);
                logger.info("[阶段3-步骤3] 开始调用推送服务推送离线状态");
                
                // 推送离线状态变更
                try {
                    webSocketService.notifyUserOnlineChanged(userId, false);
                    logger.info("========== [阶段3] 成功: 已调用notifyUserOnlineChanged推送离线状态: userId={} ==========", userId);
                } catch (Exception e) {
                    logger.error("========== [阶段3] 失败: 推送用户离线状态失败 ==========");
                    logger.error("userId={}, key={}, error={}", userId, expiredKey, e.getMessage(), e);
                }
            } else {
                logger.debug("[阶段3-步骤2] Redis过期事件key不匹配，忽略: key={} (不匹配前缀'online:user:')", expiredKey);
            }
        } catch (Exception e) {
            logger.error("========== [阶段3] 异常: 处理Redis过期事件失败 ==========");
            logger.error("error={}", e.getMessage(), e);
        }
    }
}
