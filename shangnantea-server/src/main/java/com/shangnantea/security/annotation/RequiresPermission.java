package com.shangnantea.security.annotation;

import java.lang.annotation.*;

/**
 * 权限注解，用于标记需要特定权限才能访问的接口
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {
    /**
     * 权限码，满足其中任意一个权限即可访问
     */
    String[] value() default {};
    
    /**
     * 权限校验逻辑，默认为OR（满足任意一个）
     */
    Logical logical() default Logical.OR;
    
    /**
     * 权限校验逻辑
     */
    enum Logical {
        /**
         * 满足任意一个权限即可
         */
        OR,
        
        /**
         * 必须满足所有权限
         */
        AND
    }
} 