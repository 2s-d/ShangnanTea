package com.shangnantea.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Redis配置初始化器
 * 应用启动时自动配置Redis的过期事件监听
 */
@Component
public class RedisConfigInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisConfigInitializer.class);
    
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;
    
    /**
     * 应用启动时自动配置Redis的过期事件监听
     */
    @Override
    public void run(String... args) {
        if (redisTemplate == null) {
            logger.warn("StringRedisTemplate未注入，跳过Redis过期事件配置");
            return;
        }
        
        // 等待一小段时间，确保Redis连接工厂已完全初始化（避免热重载时的连接工厂销毁问题）
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        try {
            // 使用Redis命令执行配置检查和设置
            String currentValue = redisTemplate.execute((RedisCallback<String>) connection -> {
                try {
                    Properties config = connection.serverCommands().getConfig("notify-keyspace-events");
                    if (config != null && config.containsKey("notify-keyspace-events")) {
                        return config.getProperty("notify-keyspace-events");
                    }
                    return "";
                } catch (Exception e) {
                    logger.debug("获取Redis配置失败: {}", e.getMessage());
                    return "";
                }
            });
            
            // 我们需要：
            // - E: keyevent 通知（__keyevent@...__）
            // - x: expired 事件（在线状态/登录会话过期）
            // - g: generic 事件（del）
            // - $: string 事件（set）
            boolean hasE = currentValue != null && currentValue.contains("E");
            boolean hasX = currentValue != null && currentValue.contains("x");
            boolean hasG = currentValue != null && currentValue.contains("g");
            boolean hasDollar = currentValue != null && currentValue.contains("$");
            
            if (hasE && hasX && hasG && hasDollar) {
                logger.info("Redis KeyEvent 已启用: notify-keyspace-events={}", currentValue);
            } else {
                // 尝试设置配置
                try {
                    redisTemplate.execute((RedisCallback<Object>) connection -> {
                        // Exg$ 覆盖：expired + del + set（且使用 keyevent 通知）
                        connection.serverCommands().setConfig("notify-keyspace-events", "Exg$");
                        return null;
                    });
                    logger.info("已自动配置Redis KeyEvent: notify-keyspace-events=Exg$ (原配置: {})", currentValue);
                } catch (Exception e) {
                    logger.warn("无法自动配置Redis KeyEvent，请手动配置: CONFIG SET notify-keyspace-events Exg$, 当前配置: {}, 错误: {}", 
                        currentValue, e.getMessage());
                    logger.warn("详细配置说明请参考: docs/redis-config.md");
                }
            }
        } catch (IllegalStateException e) {
            // 热重载时连接工厂可能被销毁，这是正常情况，只记录警告
            if (e.getMessage() != null && e.getMessage().contains("was destroyed")) {
                logger.debug("Redis连接工厂在热重载时被销毁，跳过配置检查");
            } else {
                logger.warn("检查Redis KeyEvent 配置失败，请手动配置: CONFIG SET notify-keyspace-events Exg$, 错误: {}", e.getMessage());
            }
        } catch (Exception e) {
            logger.warn("检查Redis KeyEvent 配置失败，请手动配置: CONFIG SET notify-keyspace-events Exg$, 错误: {}", e.getMessage());
            logger.warn("详细配置说明请参考: docs/redis-config.md");
        }
    }
}
