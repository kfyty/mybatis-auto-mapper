package com.kfyty.mybatis.auto.mapper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能描述: 用于实体类属性，用来标记数据表中不存在的字段
 *
 * @author kfyty725@hotmail.com
 * @date 2020/04/18 11:30
 * @since JDK 1.8
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Transient {
}
