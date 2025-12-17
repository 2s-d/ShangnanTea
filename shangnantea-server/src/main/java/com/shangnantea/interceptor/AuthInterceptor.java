package com.shangnantea.interceptor;

import com.shangnantea.common.api.ResultCode;
import com.shangnantea.exception.UnauthorizedException;
import com.shangnantea.model.entity.user.User;
import com.shangnantea.security.annotation.RequiresLogin;
import com.shangnantea.security.annotation.RequiresRoles;
import com.shangnantea.security.context.UserContext;
import com.shangnantea.security.util.JwtUtil;
import com.shangnantea.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * 权限拦截器
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 如果不是映射到方法，直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        // 从请求头中获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 获取方法上的RequiresLogin注解
        RequiresLogin requiresLogin = handlerMethod.getMethodAnnotation(RequiresLogin.class);
        // 获取方法上的RequiresRoles注解
        RequiresRoles requiresRoles = handlerMethod.getMethodAnnotation(RequiresRoles.class);
        
        // 如果没有任何权限注解，直接通过
        if (requiresLogin == null && requiresRoles == null) {
            return true;
        }
        
        // 验证token
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new UnauthorizedException(ResultCode.UNAUTHORIZED, "未登录或登录已过期");
        }
        
        // 设置当前用户
            String userId = jwtUtil.getUserIdFromToken(token);
            User user = userService.getUserById(userId);
            if (user == null) {
                throw new UnauthorizedException(ResultCode.UNAUTHORIZED, "用户不存在");
            }

            // 将用户信息存入上下文
            UserContext.setCurrentUser(user);

            // 验证角色权限
        if (requiresRoles != null) {
            int[] roles = requiresRoles.value();
            boolean hasRole = Arrays.stream(roles).anyMatch(role -> role == user.getRole());
            if (!hasRole) {
                throw new UnauthorizedException(ResultCode.FORBIDDEN, "没有权限访问");
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清除当前用户信息
        UserContext.clear();
    }
} 