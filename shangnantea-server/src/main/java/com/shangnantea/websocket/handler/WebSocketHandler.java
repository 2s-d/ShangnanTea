package com.shangnantea.websocket.handler;

import com.shangnantea.security.util.JwtUtil;
import com.shangnantea.websocket.manager.WebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;

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
    private JwtUtil jwtUtil;
    
    /**
     * 连接建立后触发
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从session属性中获取用户ID（由拦截器设置）
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            sessionManager.addSession(userId, session);
            logger.info("WebSocket连接建立: userId={}, sessionId={}", userId, session.getId());
            
            // 发送连接成功消息
            sendMessage(session, "{\"type\":\"connected\",\"message\":\"连接成功\"}");
        } else {
            logger.warn("WebSocket连接建立失败: 未找到用户ID, sessionId={}", session.getId());
            session.close(CloseStatus.BAD_DATA.withReason("未找到用户ID"));
        }
    }
    
    /**
     * 接收客户端消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        String payload = message.getPayload();
        logger.debug("收到WebSocket消息: userId={}, message={}", userId, payload);
        
        // 处理心跳消息
        if ("ping".equals(payload)) {
            sendMessage(session, "pong");
            return;
        }
        
        // 可以在这里处理其他类型的消息
        // 例如：客户端请求获取在线用户列表等
    }
    
    /**
     * 连接关闭后触发
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            sessionManager.removeSession(userId, session);
            logger.info("WebSocket连接关闭: userId={}, sessionId={}, status={}", 
                userId, session.getId(), status);
            
            // 广播在线用户列表更新（通知所有管理员）
            try {
                org.springframework.context.ApplicationContext applicationContext = 
                    org.springframework.web.context.support.WebApplicationContextUtils
                        .getWebApplicationContext(session.getAttributes().get("servletContext"));
                if (applicationContext != null) {
                    com.shangnantea.websocket.service.WebSocketService webSocketService = 
                        applicationContext.getBean(com.shangnantea.websocket.service.WebSocketService.class);
                    if (webSocketService != null) {
                        webSocketService.broadcastOnlineUsersUpdate();
                    }
                }
            } catch (Exception e) {
                logger.debug("广播在线用户列表更新失败，不影响断开: userId={}, error={}", userId, e.getMessage());
            }
        }
    }
    
    /**
     * 发送消息给指定session
     */
    private void sendMessage(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            logger.error("发送WebSocket消息失败: sessionId={}, error={}", 
                session.getId(), e.getMessage());
        }
    }
}
