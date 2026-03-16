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
 * Redis KeyEvent 监听器：登录会话(JTI)变更推送
 * 监听 login:user:* 的 set/del/expired 事件，并触发管理员页面的 loginSessionsUpdate 广播。
 */
@Component
public class LoginSessionKeyEventListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(LoginSessionKeyEventListener.class);

    @Autowired
    private WebSocketService webSocketService;

    @Override
    public void onMessage(@NonNull Message message, @Nullable byte[] pattern) {
        try {
            String key = message.toString();
            if (key == null || !key.startsWith("login:user:")) {
                return;
            }
            // 只要 login:user:* 发生变更，就广播一次全量会话快照（只推管理员）
            if (webSocketService != null) {
                webSocketService.broadcastLoginSessionsUpdate();
            }
        } catch (Exception e) {
            logger.debug("处理登录会话Key事件失败: {}", e.getMessage());
        }
    }
}

