package com.shangnantea.security.annotation;

import java.lang.annotation.*;

/**
 * 需要登录注解，标记在方法或类上表示需要登录才能访问
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresLogin {
} 