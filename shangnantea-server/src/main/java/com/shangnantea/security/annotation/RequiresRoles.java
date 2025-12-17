package com.shangnantea.security.annotation;

import java.lang.annotation.*;

/**
 * 需要特定角色注解，标记在方法或类上表示需要特定角色才能访问
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRoles {
    /**
     * 角色数组，满足其中任意一个角色即可访问
     */
    int[] value() default {};
} 