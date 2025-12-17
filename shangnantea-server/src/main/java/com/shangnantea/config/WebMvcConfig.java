package com.shangnantea.config;

import com.shangnantea.security.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private JwtInterceptor jwtInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册JWT拦截器，拦截所有请求
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                // 排除不需要拦截的路径，如登录、注册、静态资源等
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        "/user/login",
                        "/user/register",
                        "/error",
                        "/swagger-resources/**",
                        "/swagger-ui.html",
                        "/v2/api-docs",
                        "/webjars/**",
                        "/static/**"
                );
    }
} 