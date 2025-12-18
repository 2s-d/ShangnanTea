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

        // 如果接口不需要登录也不需要角色验证，直接放行
        if (!requiresLogin && requiresRoles == null) {
            logger.debug("接口不需要认证，直接放行: {}", requestURI);
            return true;
        }

        // 验证令牌
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
        
        // 从数据库获取最新的用户信息
        User user = jwtUtil.getUserFromToken(token);
        if (user == null) {
            logger.warn("无法找到用户，拒绝访问: {}, 用户ID: {}", requestURI, userId);
            throw new UnauthorizedException(ResultCode.UNAUTHORIZED, "用户不存在");
        }
        
        // 验证用户是否被禁用、删除等
        if (user.getStatus() != null && user.getStatus() != 1) {
            logger.warn("用户已被禁用，拒绝访问: {}, 用户ID: {}", requestURI, userId);
            throw new UnauthorizedException(ResultCode.FORBIDDEN, "账户已被禁用");
        }
        
        // 验证token中的角色与数据库中的角色是否一致
        if (!user.getRole().equals(role)) {
            logger.warn("用户角色不匹配，拒绝访问: {}, Token角色: {}, 数据库角色: {}", 
                requestURI, role, user.getRole());
            throw new UnauthorizedException(ResultCode.UNAUTHORIZED, "身份验证信息不一致");
        }

        // 设置用户信息到上下文
        UserContext.setCurrentUser(user);
        logger.debug("用户已认证，ID: {}, 角色: {}", userId, role);

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
} 