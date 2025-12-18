package com.shangnantea.security.util;

import com.shangnantea.common.constants.Constants;
import com.shangnantea.model.entity.user.User;
import com.shangnantea.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT工具类，用于生成和验证JWT令牌
 * 优化版本 - 与前端认证系统整合，统一token结构
 */
@Component
public class JwtUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    
    // JWT密钥
    @Value("${jwt.secret:shangnantea-default-secret-key}")
    private String secret;
    
    // 令牌过期时间（毫秒），默认24小时
    @Value("${jwt.expiration:86400000}")
    private long expiration;
    
    @Autowired
    private UserService userService;
    
    /**
     * 从令牌中获取用户ID
     *
     * @param token 令牌
     * @return 用户ID
     */
    public String getUserIdFromToken(String token) {
        String userId;
        try {
            Claims claims = getClaimsFromToken(token);
            userId = claims.getSubject();
        } catch (Exception e) {
            logger.error("获取用户ID失败: " + e.getMessage());
            userId = null;
        }
        return userId;
    }
    
    /**
     * 从令牌中获取用户角色
     *
     * @param token 令牌
     * @return 用户角色
     */
    public Integer getRoleFromToken(String token) {
        Integer role;
        try {
            Claims claims = getClaimsFromToken(token);
            role = claims.get("role", Integer.class);
        } catch (Exception e) {
            logger.error("获取用户角色失败: " + e.getMessage());
            role = null;
        }
        return role;
    }
    
    /**
     * 从令牌中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        String username;
        try {
            Claims claims = getClaimsFromToken(token);
            username = claims.get("username", String.class);
        } catch (Exception e) {
            logger.error("获取用户名失败: " + e.getMessage());
            username = null;
        }
        return username;
    }
    
    /**
     * 从令牌中获取用户对象
     *
     * @param token 令牌
     * @return 用户对象
     */
    public User getUserFromToken(String token) {
        String userId = getUserIdFromToken(token);
        if (userId != null) {
            return userService.getUserById(userId);
        }
        return null;
    }
    
    /**
     * 验证令牌
     *
     * @param token 令牌
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            // 验证基本token结构
            Claims claims = getClaimsFromToken(token);
            
            // 验证token是否过期
            if (isTokenExpired(claims)) {
                logger.warn("令牌已过期");
                return false;
            }
            
            // 验证必要字段存在
            if (claims.getSubject() == null) {
                logger.warn("令牌缺少用户ID");
                return false;
            }
            
            if (claims.get("role") == null) {
                logger.warn("令牌缺少角色信息");
                return false;
            }
            
            if (claims.get("username") == null) {
                logger.warn("令牌缺少用户名");
                return false;
            }
            
            // 验证角色值是否合法（只允许1-管理员，2-普通用户，3-商家）
            Integer role = claims.get("role", Integer.class);
            if (role == null || (role != 1 && role != 2 && role != 3)) {
                logger.warn("令牌中角色值不合法: " + role);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.error("JWT验证失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证令牌并返回用户ID
     *
     * @param token 令牌
     * @return 用户ID，如果令牌无效则返回null
     */
    public String validateTokenAndGetUserId(String token) {
        try {
            if (!validateToken(token)) {
                return null;
            }
            return getUserIdFromToken(token);
        } catch (Exception e) {
            logger.error("JWT验证失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 为用户生成令牌
     *
     * @param user 用户
     * @return 令牌
     */
    public String generateToken(User user) {
        logger.info("开始生成JWT令牌，用户ID: " + user.getId());
        try {
            // 确保只有三种有效角色：1-管理员，2-普通用户，3-商家
            int role = user.getRole();
            if (role != 1 && role != 2 && role != 3) {
                logger.error("用户角色无效: " + role);
                return null;
            }
            
            // 创建JWT载荷
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", role);  // 用户角色
            claims.put("username", user.getUsername());  // 用户名
            claims.put("jti", UUID.randomUUID().toString());  // JWT ID - 令牌唯一标识
            
            // 生成令牌
            String token = doGenerateToken(claims, user.getId());
            logger.info("JWT令牌生成成功，长度: " + token.length());
            return token;
        } catch (Exception e) {
            logger.error("JWT令牌生成失败: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 判断令牌是否过期
     *
     * @param token 令牌
     * @return 是否过期
     */
    private boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration != null && expiration.before(new Date());
    }
    
    /**
     * 判断令牌是否过期
     *
     * @param claims 令牌声明
     * @return 是否过期
     */
    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
    
    /**
     * 从令牌中获取过期时间
     *
     * @param token 令牌
     * @return 过期时间
     */
    private Date getExpirationDateFromToken(String token) {
        Date expiration;
        try {
            Claims claims = getClaimsFromToken(token);
            expiration = claims.getExpiration();
        } catch (Exception e) {
            logger.error("获取过期时间失败: " + e.getMessage());
            expiration = null;
        }
        return expiration;
    }
    
    /**
     * 从令牌中获取声明
     *
     * @param token 令牌
     * @return 声明
     */
    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            // 令牌过期，但仍返回声明以便查看内容
            claims = e.getClaims();
        }
        return claims;
    }
    
    /**
     * 生成令牌
     *
     * @param claims  声明
     * @param subject 主题(用户ID)
     * @return 令牌
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        
        return Jwts.builder()
                .claims(claims)
                .subject(subject)  // 用户ID
                .issuedAt(now)  // 发行时间
                .expiration(expirationDate)  // 过期时间
                .issuer(Constants.Security.JWT_ISSUER)  // 发行者
                .signWith(key)
                .compact();
    }
} 