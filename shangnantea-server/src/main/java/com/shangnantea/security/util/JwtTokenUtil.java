package com.shangnantea.security.util;

import com.shangnantea.common.constants.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类，用于生成和解析JWT令牌
 */
@Component
public class JwtTokenUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Value("${jwt.secret:shangnantea-default-secret-key-for-jwt-signature}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    /**
     * 获取JWT中的用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public String getUserIdFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 获取JWT的过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从JWT中获取指定声明
     *
     * @param token          JWT令牌
     * @param claimsResolver 声明解析函数
     * @param <T>            声明类型
     * @return 声明值
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从JWT中获取所有声明
     *
     * @param token JWT令牌
     * @return 所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 检查JWT是否已过期
     *
     * @param token JWT令牌
     * @return 是否已过期
     */
    private Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * 生成JWT令牌
     *
     * @param userId 用户ID
     * @return JWT令牌
     */
    public String generateToken(String userId) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userId);
    }

    /**
     * 生成JWT令牌，包含自定义声明
     *
     * @param userId 用户ID
     * @param claims 自定义声明
     * @return JWT令牌
     */
    public String generateToken(String userId, Map<String, Object> claims) {
        return doGenerateToken(claims, userId);
    }

    /**
     * 生成JWT令牌
     *
     * @param claims 自定义声明
     * @param userId 用户ID
     * @return JWT令牌
     */
    private String doGenerateToken(Map<String, Object> claims, String userId) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .claims(claims)
                .subject(userId)
                .issuedAt(now)
                .expiration(expirationDate)
                .issuer(Constants.Security.JWT_ISSUER)
                .signWith(key)
                .compact();
    }

    /**
     * 验证JWT令牌是否有效
     *
     * @param token  JWT令牌
     * @param userId 用户ID
     * @return 是否有效
     */
    public Boolean validateToken(String token, String userId) {
        try {
            final String tokenUserId = getUserIdFromToken(token);
            return (tokenUserId.equals(userId) && !isTokenExpired(token));
        } catch (Exception e) {
            LOGGER.error("JWT验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断JWT令牌是否可以刷新
     *
     * @param token JWT令牌
     * @return 是否可以刷新
     */
    public Boolean canTokenBeRefreshed(String token) {
        try {
            // 只要令牌存在且未被篡改，就可以刷新
            getUserIdFromToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            // 过期的令牌也可以刷新
            return true;
        } catch (Exception e) {
            // 其他异常表示令牌无效，不能刷新
            return false;
        }
    }

    /**
     * 刷新JWT令牌
     *
     * @param token 原JWT令牌
     * @return 新JWT令牌
     */
    public String refreshToken(String token) {
        try {
            // 从原令牌中获取所有声明
            Claims claims;
            try {
                claims = getAllClaimsFromToken(token);
            } catch (ExpiredJwtException e) {
                // 如果令牌已过期，从异常中获取声明
                claims = e.getClaims();
            }

            // 重新生成令牌
            String userId = claims.getSubject();
            claims.remove("sub");
            claims.remove("iat");
            claims.remove("exp");
            claims.remove("iss");

            return doGenerateToken(claims, userId);
        } catch (Exception e) {
            LOGGER.error("刷新JWT失败: {}", e.getMessage());
            return null;
        }
    }
} 