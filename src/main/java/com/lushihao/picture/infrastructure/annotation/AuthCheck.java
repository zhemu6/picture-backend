package com.lushihao.picture.infrastructure.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解 用于权限校验
 * @author: lushihao
 * @version: 1.0
 * create:   2025-07-27   19:48
 */
// 注解作用在方法上
@Target(ElementType.METHOD)
// 注解在运行时有效
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 必须有某个角色的权限
     */
    String mustRole() default "";

}
