package com.kfyty.mybatis.auto.mapper.exception;

import lombok.NoArgsConstructor;

/**
 * 功能描述: SQL 条件枚举匹配异常
 *
 * @author kfyty725@hotmail.com
 * @date 2020/4/3 13:24
 * @since JDK 1.8
 */
@NoArgsConstructor
public class SQLConditionMatchException extends RuntimeException {
    public SQLConditionMatchException(String message) {
        super(message);
    }

    public SQLConditionMatchException(Throwable throwable) {
        super(throwable);
    }

    public SQLConditionMatchException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
