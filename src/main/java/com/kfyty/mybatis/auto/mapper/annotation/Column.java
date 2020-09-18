package com.kfyty.mybatis.auto.mapper.annotation;

import org.apache.ibatis.type.JdbcType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能描述: 用于实体类属性，用来标记和数据库字段名称不一致的属性
 *
 * @author kfyty725@hotmail.com
 * @date 2020/09/18 18:06
 * @since JDK 1.8
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String value();

    JdbcType jdbcType() default JdbcType.OTHER;
}
