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
    /**
     * 更新时指定主键属性
     * @return
     */
    String[] primaryKey() default "id";

    /**
     * 实体类后缀
     * @return
     */
    String suffix() default "";

    /**
     * 删除操作时需指定表名，否则尝试根据 Mapper 接口类名解析表名
     * @return
     */
    String table() default "";

    /**
     * 指定需要查询的列
     * @return
     */
    String columns() default "*";

    /**
     * 插入时，遇到 null 是否转换为插入数据库默认值
     * @return
     */
    boolean useDefault() default true;
}
