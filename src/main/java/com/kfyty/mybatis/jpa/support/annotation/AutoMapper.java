package com.kfyty.mybatis.jpa.support.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能描述: 用于 mapper 接口或方法，即可自动映射方法
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/6 13:37
 * @since JDK 1.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface AutoMapper {
    /**
     * 更新时指定主键属性，可用于接口配置
     * @return 默认值为 id
     */
    String[] primaryKey() default "";

    /**
     * 实体类后缀，可用于接口配置
     * @return 默认值为 Pojo
     */
    String suffix() default "";

    /**
     * 实体类/Mapper 接口命名不规范时需指定表名，可用于接口配置
     * @return 默认值为 ""
     */
    String table() default "";

    /**
     * 查询时添加额外的条件，可用于接口配置
     * @return 默认值为 ""
     */
    String where() default "";

    /**
     * where 条件分隔符，可用于接口配置
     * @return 默认值为 "and"
     */
    String separator() default "and";

    /**
     * 指定需要查询的列，仅用于方法配置
     * @return 默认值为 "*"
     */
    String columns() default "*";

    /**
     * 插入时，遇到 null 是否转换为插入数据库默认值，仅用于方法配置
     * @return 默认值为 true
     */
    boolean useDefault() default true;
}
