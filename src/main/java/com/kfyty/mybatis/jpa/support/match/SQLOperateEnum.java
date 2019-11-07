package com.kfyty.mybatis.jpa.support.match;

import java.util.Objects;

/**
 * 功能描述: SQL 操作枚举
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/7 10:27
 * @since JDK 1.8
 */
public enum SQLOperateEnum {
    OPERATE_INSERT("insert", " insert into %s values ( %s ) "),
    OPERATE_UPDATE("update", " update %s set %s where %s = %s "),
    OPERATE_INSERT_ALL("insertAll", " insert into %s values ( %s ) "),
    OPERATE_UPDATE_ALL("updateAll", " update %s set %s where %s = %s "),
    OPERATE_SELECT_ALL("findAll", " select * from %s "),
    OPERATE_DELETE_ALL("deleteAll", " delete from %s "),
    OPERATE_SELECT_BY("findBy", " select * from %s where ( %s ) "),
    OPERATE_DELETE_BY("deleteBy", " delete from %s where ( %s ) ");

    private String operate;
    private String template;

    private SQLOperateEnum(String operate, String template) {
        this.operate = operate;
        this.template = template;
    }

    public String operate() {
        return this.operate;
    }

    public String template() {
        return this.template;
    }

    public static SQLOperateEnum matchSQLOperate(String methodName) {
        Objects.requireNonNull(methodName, "sql operate match error: method name is null !");
        for (SQLOperateEnum value : SQLOperateEnum.values()) {
            if(value.operate().equals(methodName)) {
                return value;
            }
        }
        for (SQLOperateEnum value : SQLOperateEnum.values()) {
            if(methodName.startsWith(value.operate())) {
                return value;
            }
        }
        throw new IllegalArgumentException("sql operate match failed: " + methodName);
    }
}
