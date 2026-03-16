package com.shangnantea.config;

import com.shangnantea.websocket.listener.UserOnlineExpiredListener;
import com.shangnantea.websocket.listener.LoginSessionKeyEventListener;
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

    @Autowired
    @NonNull
    private LoginSessionKeyEventListener loginSessionKeyEventListener;
    
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

        // 监听登录会话 key 的 set/del 事件（keyspace events）
        // 注意：需要 Redis 配置 notify-keyspace-events 包含 set/del 事件
        PatternTopic setTopic = new PatternTopic("__keyevent@0__:set");
        PatternTopic delTopic = new PatternTopic("__keyevent@0__:del");
        container.addMessageListener(loginSessionKeyEventListener, setTopic);
        container.addMessageListener(loginSessionKeyEventListener, delTopic);
        // 如果 login:user:* 自己过期，也需要同步（虽然正常会随着JWT TTL清理）
        container.addMessageListener(loginSessionKeyEventListener, expiredTopic);
        
        logger.info("Redis KeyEvent 监听器已注册: expired/set/del");
        return container;
    }
}
