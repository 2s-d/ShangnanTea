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
        logger.info("========== [阶段1] 开始执行Redis配置检查 ==========");
        
        if (redisTemplate == null) {
            logger.error("========== [阶段1] 失败: StringRedisTemplate未注入，跳过Redis过期事件配置 ==========");
            return;
        }
        
        logger.info("[阶段1-步骤1] StringRedisTemplate已注入，开始检查Redis配置");
        
        try {
            logger.info("[阶段1-步骤2] 开始获取当前Redis配置: notify-keyspace-events");
            
            // 使用Redis命令执行配置检查和设置
            String currentValue = redisTemplate.execute((RedisCallback<String>) connection -> {
                try {
                    logger.debug("[阶段1-步骤2-内部] 调用connection.getConfig('notify-keyspace-events')");
                    // 尝试获取当前配置
                    Properties config = connection.getConfig("notify-keyspace-events");
                    if (config != null && config.containsKey("notify-keyspace-events")) {
                        String value = config.getProperty("notify-keyspace-events");
                        logger.debug("[阶段1-步骤2-内部] 获取到配置值: {}", value);
                        return value;
                    }
                    logger.debug("[阶段1-步骤2-内部] 配置不存在或为空");
                    return "";
                } catch (Exception e) {
                    logger.error("[阶段1-步骤2-内部] 获取Redis配置失败: {}", e.getMessage(), e);
                    return "";
                }
            });
            
            logger.info("[阶段1-步骤2] 获取Redis配置完成: currentValue='{}'", currentValue);
            
            // 检查是否已包含 'E' 和 'x'
            boolean hasE = currentValue != null && currentValue.contains("E");
            boolean hasX = currentValue != null && currentValue.contains("x");
            
            logger.info("[阶段1-步骤3] 检查配置内容: hasE={}, hasX={}, currentValue='{}'", hasE, hasX, currentValue);
            
            if (hasE && hasX) {
                logger.info("========== [阶段1] 成功: Redis过期事件已启用: notify-keyspace-events={} ==========", currentValue);
            } else {
                logger.info("[阶段1-步骤4] 配置不完整，尝试自动设置: notify-keyspace-events=Ex");
                // 尝试设置配置
                try {
                    redisTemplate.execute((RedisCallback<Object>) connection -> {
                        logger.debug("[阶段1-步骤4-内部] 调用connection.setConfig('notify-keyspace-events', 'Ex')");
                        connection.setConfig("notify-keyspace-events", "Ex");
                        return null;
                    });
                    logger.info("========== [阶段1] 成功: 已自动配置Redis过期事件: notify-keyspace-events=Ex (原配置: {}) ==========", currentValue);
                } catch (Exception e) {
                    logger.error("========== [阶段1] 失败: 无法自动配置Redis过期事件 ==========");
                    logger.error("请手动配置: CONFIG SET notify-keyspace-events Ex");
                    logger.error("当前配置: {}", currentValue);
                    logger.error("错误信息: {}", e.getMessage(), e);
                    logger.warn("详细配置说明请参考: docs/redis-config.md");
                }
            }
        } catch (Exception e) {
            logger.error("========== [阶段1] 异常: 检查Redis过期事件配置失败 ==========");
            logger.error("错误信息: {}", e.getMessage(), e);
            logger.warn("请手动配置: CONFIG SET notify-keyspace-events Ex");
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
        logger.info("========== [阶段2] 开始注册Redis过期事件监听器 ==========");
        
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        logger.info("[阶段2-步骤1] 已设置Redis连接工厂");
        
        // 监听 __keyevent@0__:expired 频道（key过期事件）
        // 注意：Redis需要配置 notify-keyspace-events Ex 才能接收过期事件
        PatternTopic expiredTopic = new PatternTopic("__keyevent@0__:expired");
        logger.info("[阶段2-步骤2] 创建监听主题: {}", expiredTopic.getTopic());
        
        container.addMessageListener(userOnlineExpiredListener, expiredTopic);
        logger.info("[阶段2-步骤3] 已添加消息监听器: listener={}, topic={}", 
            userOnlineExpiredListener.getClass().getSimpleName(), expiredTopic.getTopic());
        
        logger.info("========== [阶段2] 成功: Redis过期事件监听器已注册: topic=__keyevent@0__:expired ==========");
        return container;
    }
}
