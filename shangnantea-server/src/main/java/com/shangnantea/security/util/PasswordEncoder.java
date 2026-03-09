package com.shangnantea.security.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码编码工具类（BCrypt）。
 *
 * 说明：
 * - 该实现会直接改变数据库中 password 字段的存储格式。
 * - 若数据库中仍是旧的 SHA-256/Base64 密文，则升级后将无法验证通过。
 */
@Component
public class PasswordEncoder {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    /**
     * 加密密码
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public String encode(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }

        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }

        return encoder.matches(rawPassword, encodedPassword);
    }
} 