package com.shangnantea.config;

import com.shangnantea.service.UserService;
import com.shangnantea.service.impl.UserServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 用户服务显式配置
 * 说明：在某些环境下自动扫描可能存在异常，这里通过手动声明 Bean
 * 确保始终有一个 UserService 可供注入。
 */
@Configuration
public class UserServiceConfig {

    @Bean
    public UserService userService() {
        // 交给 Spring 后续完成字段级依赖注入
        return new UserServiceImpl();
    }
}

