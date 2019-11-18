package com.kfyty.mybatis.jpa.support.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能描述: 当配置 supportMethodsArguments 为 false 时使用此注解，将最后两参数作为分页参数进行分页
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/6 13:37
 * @since JDK 1.8
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Pageable {
}
