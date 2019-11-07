package com.kfyty.mybatis.jpa.support.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能描述: 用于 mapper 接口的方法，使其拥有 jpa 功能
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/6 13:37
 * @since JDK 1.8
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JpaQuery {
    String suffix() default "";

    String mapKey() default "";
}
