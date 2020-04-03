package com.kfyty.mybatis.auto.mapper.exception;

import lombok.NoArgsConstructor;

/**
 * 功能描述: SQL 操作枚举匹配异常
 *
 * @author kfyty725@hotmail.com
 * @date 2020/4/3 13:24
 * @since JDK 1.8
 */
@NoArgsConstructor
public class SQLOperateMatchException extends RuntimeException {
    public SQLOperateMatchException(String message) {
        super(message);
    }

    public SQLOperateMatchException(Throwable throwable) {
        super(throwable);
    }

    public SQLOperateMatchException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
