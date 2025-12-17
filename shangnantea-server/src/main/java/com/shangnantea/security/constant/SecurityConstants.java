package com.shangnantea.security.constant;

/**
 * 安全相关常量
 */
public class SecurityConstants {
    /**
     * JWT密钥
     */
    public static final String JWT_SECRET_KEY = "shangnanTeaSecretKey";
    
    /**
     * Token过期时间（毫秒）- 24小时
     */
    public static final long TOKEN_EXPIRE_TIME = 24 * 60 * 60 * 1000;
    
    /**
     * Token刷新过期时间（毫秒）- 7天
     */
    public static final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000;
    
    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * Token在Header中的名称
     */
    public static final String TOKEN_HEADER = "Authorization";
    
    /**
     * 用户ID在JWT中的键名
     */
    public static final String JWT_USER_ID_KEY = "id";
    
    /**
     * 用户名在JWT中的键名
     */
    public static final String JWT_USERNAME_KEY = "username";
    
    /**
     * 用户角色在JWT中的键名
     */
    public static final String JWT_ROLE_KEY = "role";
    
    /**
     * 过期时间在JWT中的键名
     */
    public static final String JWT_EXPIRE_KEY = "exp";
} 