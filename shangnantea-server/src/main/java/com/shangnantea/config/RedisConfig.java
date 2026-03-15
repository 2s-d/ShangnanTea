package com.shangnantea.config;

import com.shangnantea.websocket.listener.UserOnlineExpiredListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.NonNull;

/**
 * Redis配置类
 * 配置Redis过期事件监听器，用于监听用户在线状态key的过期事件
 */
@Configuration
public class RedisConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);
    
    @Autowired
    @NonNull
    private RedisConnectionFactory redisConnectionFactory;
    
    @Autowired
    @NonNull
    private UserOnlineExpiredListener userOnlineExpiredListener;
    
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
