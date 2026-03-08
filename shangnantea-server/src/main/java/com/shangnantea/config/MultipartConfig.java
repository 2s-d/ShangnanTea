package com.shangnantea.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
 * 文件上传配置类
 * 显式配置文件上传大小限制，确保配置生效
 */
@Configuration
public class MultipartConfig {
    
    /**
     * 配置MultipartConfigElement
     * 设置文件上传大小限制：单个文件20MB，总请求50MB
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // 单个文件最大大小：20MB
        factory.setMaxFileSize(DataSize.ofMegabytes(20));
        
        // 整个请求最大大小：50MB（支持多文件上传）
        factory.setMaxRequestSize(DataSize.ofMegabytes(50));
        
        // 文件写入磁盘的阈值：2KB（小于此值保存在内存中）
        factory.setFileSizeThreshold(DataSize.ofKilobytes(2));
        
        // 临时文件存储位置
        factory.setLocation(System.getProperty("java.io.tmpdir"));
        
        return factory.createMultipartConfig();
    }
    
    /**
     * 配置MultipartResolver
     * 使用标准Servlet实现，确保与Tomcat兼容
     */
    @Bean(name = "multipartResolver")
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
}
