package com.shangnantea.websocket.interceptor;

import com.shangnantea.security.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket认证拦截器
 * 在WebSocket握手时验证JWT token
 */
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 握手前验证
     * 从请求参数或Header中获取token并验证
     */
    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            
            // 从查询参数中获取token
            String token = servletRequest.getServletRequest().getParameter("token");
            
            // 如果查询参数中没有，尝试从Header中获取
            if (token == null || token.isEmpty()) {
                String authHeader = servletRequest.getServletRequest().getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }
            
            if (token == null || token.isEmpty()) {
                logger.warn("WebSocket握手失败: 未提供token");
                return false;
            }
            
            // 验证token
            if (!jwtUtil.validateToken(token)) {
                logger.warn("WebSocket握手失败: token无效");
                return false;
            }
            
            // 从token中获取用户ID
            String userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                logger.warn("WebSocket握手失败: 无法从token中获取用户ID");
                return false;
            }
            
            // 从token中获取用户角色（用于后续按角色推送消息）
            Integer role = jwtUtil.getRoleFromToken(token);
            
            // 将用户ID存入session属性，供后续使用
            attributes.put("userId", userId);
            // 保存原始 token，用于后续 WebSocket 消息处理时做会话/过期二次校验（防止登出后“在线被复活”）
            attributes.put("token", token);
            if (role != null) {
                attributes.put("role", role);
            }
            logger.info("WebSocket握手成功: userId={}", userId);
            return true;
        }
        
        return false;
    }
    
    /**
     * 握手后处理
     */
    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                               @NonNull WebSocketHandler wsHandler, @Nullable Exception exception) {
        if (exception != null) {
            logger.error("WebSocket握手后处理异常: {}", exception.getMessage());
        }
    }
}
