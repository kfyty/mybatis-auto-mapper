package com.kfyty.mybatis.auto.mapper.annotation;

import com.kfyty.mybatis.auto.mapper.enums.SelectKeyOrder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能描述: 用于 mapper 接口或方法，插入数据时生成 <selectKey/> 标签
 * 方法注解优先级高于类注解
 * insertAll 方法无效
 *
 * @author kfyty725@hotmail.com
 * @date 2019/12/20 19:37
 * @since JDK 1.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface SelectKey {
    /**
     * 查询主键值 SQL 语句
     * @return 默认值为 MySQL 查询自增主键
     */
    String value() default "select last_insert_id()";

    /**
     * <selectKey/> 标签执行顺序
     * @return 默认值为 SelectKeyOrder.AFTER
     */
    SelectKeyOrder order() default SelectKeyOrder.AFTER;
}
