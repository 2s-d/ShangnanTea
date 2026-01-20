package com.shangnantea;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 商南茶文化推广与销售平台启动类
 */
@SpringBootApplication
@MapperScan("com.shangnantea.mapper")
@EnableScheduling  // 启用定时任务
public class ShangnanTeaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShangnanTeaApplication.class, args);
    }
} 