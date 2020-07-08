package com.kfyty.mybatis.auto.mapper.annotation;

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
     * 继承自 BaseMapper 的方法需在子接口上声明实体类型
     * @return 默认值为 Object.class
     */
    Class<?> entity() default Object.class;

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
     * 符合 find*By** 风格命名时，是否从方法解析需查询的列，仅用于方法配置
     * @return 默认值为 true
     */
    boolean parseColumn() default true;

    /**
     * 插入/更新对象时，遇到 null 是否转换为插入数据库默认值，仅用于方法配置
     * @return 默认值为 false
     */
    boolean useDefault() default false;

    /**
     * 更新对象时，是否允许更新为 null 值，仅用于方法配置
     * @return 默认值为 false
     */
    boolean allowNull() default false;

    /**
     * 是否继承类注解 where 配置，仅用于方法配置
     * @return 默认值为 true
     */
    boolean extend() default true;
}
