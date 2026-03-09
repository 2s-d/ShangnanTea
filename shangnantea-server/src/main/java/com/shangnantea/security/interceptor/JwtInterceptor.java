package com.shangnantea.security.interceptor;

import com.shangnantea.common.api.ResultCode;
import com.shangnantea.common.constants.Constants;
import com.shangnantea.exception.UnauthorizedException;
import com.shangnantea.model.entity.user.User;
import com.shangnantea.security.annotation.RequiresLogin;
import com.shangnantea.security.annotation.RequiresRoles;
import com.shangnantea.security.context.UserContext;
import com.shangnantea.security.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * JWT拦截器，用于验证请求中的JWT令牌和权限
 * 优化版本 - 与前端认证系统整合，统一身份验证流程
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 在线状态缓存（可选，用于记录最近活跃用户）。
     */
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;
    
    // 说明：为避免在应用启动阶段出现 Bean 装配问题，这里暂时不直接依赖 UserService，
    // 而是仅基于 JWT 中的载荷构造一个简化的 User 放入 UserContext。
    // 如果后续需要从数据库校验用户状态/角色，可再通过其他方式引入 UserService。

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录当前请求路径
        String requestURI = request.getRequestURI();
        logger.debug("处理请求: {}, 方法: {}", requestURI, request.getMethod());
        
        // 如果不是方法处理器，直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 清除用户上下文，确保线程安全
        UserContext.clear();

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        Class<?> clazz = method.getDeclaringClass();

        // 获取JWT令牌
        String token = getTokenFromRequest(request);

        // 检查是否需要登录
        boolean requiresLogin = method.isAnnotationPresent(RequiresLogin.class) || clazz.isAnnotationPresent(RequiresLogin.class);
        
        // 检查是否需要特定角色
        RequiresRoles requiresRoles = method.getAnnotation(RequiresRoles.class);
        if (requiresRoles == null) {
            requiresRoles = clazz.getAnnotation(RequiresRoles.class);
        }

        // 如果接口不需要登录也不需要角色验证，尝试可选认证（如果token存在则设置UserContext）
        if (!requiresLogin && requiresRoles == null) {
            if (token != null && jwtUtil.validateToken(token)) {
                // 可选认证：如果token存在且有效，设置UserContext（用于返回isLiked等状态）
                try {
                    String userId = jwtUtil.getUserIdFromToken(token);
                    String username = jwtUtil.getUsernameFromToken(token);
                    Integer role = jwtUtil.getRoleFromToken(token);
                    
                    if (userId != null && username != null && role != null) {
                        User user = new User();
                        user.setId(userId);
                        user.setUsername(username);
                        user.setRole(role);
                        UserContext.setCurrentUser(user);
                        logger.debug("可选认证成功，设置用户上下文: ID={}, 角色={}", userId, role);
                        markUserOnline(userId);
                    }
                } catch (Exception e) {
                    logger.debug("可选认证失败，忽略token: {}", e.getMessage());
                }
            }
            logger.debug("接口不需要认证，直接放行: {}", requestURI);
            return true;
        }

        // 验证令牌（接口需要登录或角色验证）
        if (token == null) {
            logger.warn("未提供JWT令牌，拒绝访问: {}", requestURI);
            throw new UnauthorizedException(ResultCode.UNAUTHORIZED, "未登录或登录已过期");
        }
        
        // 使用优化后的验证方法验证令牌
        if (!jwtUtil.validateToken(token)) {
            logger.warn("JWT令牌验证失败，拒绝访问: {}", requestURI);
            throw new UnauthorizedException(ResultCode.UNAUTHORIZED, "未登录或登录已过期");
        }

        // 从token中直接获取必要信息
        String userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        Integer role = jwtUtil.getRoleFromToken(token);
        
        if (userId == null || username == null || role == null) {
            logger.warn("JWT令牌缺少必要信息，拒绝访问: {}", requestURI);
            throw new UnauthorizedException(ResultCode.UNAUTHORIZED, "身份验证信息不完整");
        }
        
        // 基于 token 中的数据构造一个简化的 User 对象放入上下文
        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setRole(role);
        // TODO: 如需校验用户状态/角色是否被后台修改，可在其他位置通过 UserService 再做一次数据库校验

        UserContext.setCurrentUser(user);
        logger.debug("用户已认证（基于JWT），ID: {}, 角色: {}", userId, role);
        markUserOnline(userId);
        
        // 通过WebSocket推送在线状态更新（如果WebSocket连接存在）
        // 注意：这里不直接注入WebSocketService，避免循环依赖
        // WebSocket状态更新由WebSocketHandler在连接建立时处理

        // 验证角色
        if (requiresRoles != null && requiresRoles.value().length > 0) {
            int userRole = user.getRole();
            boolean hasRole = false;
            
            for (int requiredRole : requiresRoles.value()) {
                if (userRole == requiredRole) {
                    hasRole = true;
                    break;
                }
            }
            
            if (!hasRole) {
                logger.warn("用户权限不足，拒绝访问: {}, 用户角色: {}, 需要角色: {}", 
                    requestURI, userRole, Arrays.toString(requiresRoles.value()));
                throw new UnauthorizedException(ResultCode.FORBIDDEN, "没有访问权限");
            }
            
            logger.debug("角色验证通过，允许访问: {}", requestURI);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求完成后清除用户上下文，确保线程安全
        UserContext.clear();
    }

    /**
     * 从请求中获取令牌
     *
     * @param request 请求对象
     * @return 令牌字符串，如果没有则返回null
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(Constants.Security.JWT_HEADER);
        if (bearerToken != null && bearerToken.startsWith(Constants.Security.JWT_TOKEN_PREFIX)) {
            return bearerToken.substring(Constants.Security.JWT_TOKEN_PREFIX.length());
        }
        return null;
    }
    
    /**
     * 记录用户最近在线状态（基于任意一次通过JWT的请求）。
     */
    private void markUserOnline(String userId) {
        if (redisTemplate == null || userId == null) {
            return;
        }
        try {
            String key = "online:user:" + userId;
            // 这里设置一个短TTL，比如5分钟，期间有任何请求都会刷新这个TTL
            redisTemplate.opsForValue().set(key, "1", 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            logger.debug("记录用户在线状态失败，userId={}, error={}", userId, e.getMessage());
        }
    }
} 