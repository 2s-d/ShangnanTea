package com.shangnantea.config;

import com.shangnantea.websocket.listener.UserOnlineExpiredListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.NonNull;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisCallback;

import java.util.Properties;

/**
 * Redis配置类
 * 配置Redis过期事件监听器，用于监听用户在线状态key的过期事件
 */
@Configuration
public class RedisConfig implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);
    
    @Autowired
    @NonNull
    private RedisConnectionFactory redisConnectionFactory;
    
    @Autowired
    @NonNull
    private UserOnlineExpiredListener userOnlineExpiredListener;
    
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
        
        try {
            // 使用Redis命令执行配置检查和设置
            String currentValue = redisTemplate.execute((RedisCallback<String>) connection -> {
                try {
                    // 尝试获取当前配置
                    Properties config = connection.getConfig("notify-keyspace-events");
                    if (config != null && config.containsKey("notify-keyspace-events")) {
                        return config.getProperty("notify-keyspace-events");
                    }
                    return "";
                } catch (Exception e) {
                    logger.debug("获取Redis配置失败: {}", e.getMessage());
                    return "";
                }
            });
            
            // 检查是否已包含 'E' 和 'x'
            boolean hasE = currentValue != null && currentValue.contains("E");
            boolean hasX = currentValue != null && currentValue.contains("x");
            
            if (hasE && hasX) {
                logger.info("Redis过期事件已启用: notify-keyspace-events={}", currentValue);
            } else {
                // 尝试设置配置
                try {
                    redisTemplate.execute((RedisCallback<Object>) connection -> {
                        connection.setConfig("notify-keyspace-events", "Ex");
                        return null;
                    });
                    logger.info("已自动配置Redis过期事件: notify-keyspace-events=Ex (原配置: {})", currentValue);
                } catch (Exception e) {
                    logger.warn("无法自动配置Redis过期事件，请手动配置: CONFIG SET notify-keyspace-events Ex, 当前配置: {}, 错误: {}", 
                        currentValue, e.getMessage());
                    logger.warn("详细配置说明请参考: docs/redis-config.md");
                }
            }
        } catch (Exception e) {
            logger.warn("检查Redis过期事件配置失败，请手动配置: CONFIG SET notify-keyspace-events Ex, 错误: {}", e.getMessage());
            logger.warn("详细配置说明请参考: docs/redis-config.md");
        }
    }
    
    /**
     * 配置Redis消息监听容器
     * 监听 __keyevent@0__:expired 频道，用于接收key过期事件
     * 注意：需要在Redis配置中启用 notify-keyspace-events Ex
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        
        // 监听 __keyevent@0__:expired 频道（key过期事件）
        // 注意：Redis需要配置 notify-keyspace-events Ex 才能接收过期事件
        PatternTopic expiredTopic = new PatternTopic("__keyevent@0__:expired");
        container.addMessageListener(userOnlineExpiredListener, expiredTopic);
        
        logger.info("Redis过期事件监听器已注册: topic=__keyevent@0__:expired");
        return container;
    }
}
