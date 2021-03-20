package com.kfyty.mybatis.auto.mapper.match;

import java.util.Set;

/**
 * 描述: SQL 条件接口
 *
 * @author kfyty725
 * @date 2021/3/19 17:15
 * @email kfyty725@hotmail.com
 */
public interface SQLCondition {
    /**
     * SQL 条件名称，eg: Equal
     * @return SQL 条件名称
     */
    String condition();

    /**
     * 条件参数模板
     * 解析出条件名称后，用于拼接参数 SQL，eg: = %s
     * @return 条件参数模板
     */
    String template();

    /**
     * SQL 分隔符，只有 And/Or 条件有效
     * @return and/or
     */
    String separate();

    /**
     * SQL 互斥条件
     * 已匹配的条件集合中是否存在与当前条件互斥的条件
     * @param conditions 已匹配的条件集合
     * @return 已匹配的条件集合中，存在与当前条件互斥的条件时返回 true
     */
    default boolean mutexCondition(Set<SQLCondition> conditions) {
        return false;
    }
}
