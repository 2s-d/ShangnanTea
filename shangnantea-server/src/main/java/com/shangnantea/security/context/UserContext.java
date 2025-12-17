package com.shangnantea.security.context;

import com.shangnantea.model.entity.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户上下文，用于存储当前线程中的用户信息
 * 优化版本 - 确保线程安全和角色验证
 */
public class UserContext {
    
    private static final Logger logger = LoggerFactory.getLogger(UserContext.class);
    
    /** 用ThreadLocal存储当前用户，确保线程安全 */
    private static final ThreadLocal<User> CURRENT_USER = new ThreadLocal<>();
    
    /**
     * 设置当前用户
     *
     * @param user 用户对象
     */
    public static void setCurrentUser(User user) {
        if (user == null) {
            logger.warn("尝试设置null用户到上下文");
            return;
        }
        
        // 确保角色值合法
        Integer role = user.getRole();
        if (role == null || (role != 1 && role != 2 && role != 3)) {
            logger.warn("尝试设置无效角色用户到上下文: {}", role);
            return;
        }
        
        logger.debug("设置当前用户到上下文: ID={}, 角色={}", user.getId(), user.getRole());
        CURRENT_USER.set(user);
    }
    
    /**
     * 获取当前用户
     *
     * @return 当前用户对象，如果未设置则返回null
     */
    public static User getCurrentUser() {
        return CURRENT_USER.get();
    }
    
    /**
     * 获取当前用户ID
     * 
     * @return 当前用户ID，如果未设置则返回null
     */
    public static String getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
    
    /**
     * 获取当前用户名
     *
     * @return 当前用户名
     */
    public static String getCurrentUsername() {
        User user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }
    
    /**
     * 获取当前用户角色
     * 
     * @return 当前用户角色，如果未设置则返回null
     */
    public static Integer getCurrentUserRole() {
        User user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }
    
    /**
     * 检查当前用户是否管理员(角色=1)
     * 
     * @return 是否是管理员
     */
    public static boolean isAdmin() {
        Integer role = getCurrentUserRole();
        return role != null && role == 1;
    }
    
    /**
     * 检查当前用户是否普通用户(角色=2)
     * 
     * @return 是否是普通用户
     */
    public static boolean isUser() {
        Integer role = getCurrentUserRole();
        return role != null && role == 2;
    }
    
    /**
     * 检查当前用户是否商家(角色=3)
     * 
     * @return 是否是商家
     */
    public static boolean isShop() {
        Integer role = getCurrentUserRole();
        return role != null && role == 3;
    }
    
    /**
     * 检查当前用户是否有指定角色
     * 
     * @param roles 角色数组
     * @return 是否有指定角色
     */
    public static boolean hasAnyRole(Integer... roles) {
        Integer currentRole = getCurrentUserRole();
        if (currentRole == null || roles == null || roles.length == 0) {
            return false;
        }
        
        for (Integer role : roles) {
            if (currentRole.equals(role)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 清除当前用户信息
     */
    public static void clear() {
        logger.debug("清除当前用户上下文");
        CURRENT_USER.remove();
    }
} 