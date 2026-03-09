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

    /**
     * 临时脚本：生成多组使用不同盐的 BCrypt 密文（明文: user1234）。
     * 使用方式：
     * 1. 在 IDE 里右键运行本类的 main 方法，或用 mvn 编译后直接运行。
     * 2. 控制台会输出4行不同的 hash，把结果复制走后即可删除该 main 方法。
     */
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String raw = "user1234";

        for (int i = 1; i <= 4; i++) {
            String hash = encoder.encode(raw);
            System.out.println("hash" + i + ": " + hash);
        }
    }
} 