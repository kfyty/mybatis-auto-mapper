package com.kfyty.mybatis.jpa.support.match;

import org.apache.ibatis.parsing.XPathParser;

import java.io.InputStreamReader;
import java.util.Objects;

/**
 * 功能描述: SQL 操作枚举
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/7 10:27
 * @since JDK 1.8
 */
public enum SQLOperateEnum {
    OPERATE_INSERT_ALL("insertAll", ""),
    OPERATE_UPDATE_ALL("updateAll", ""),
    OPERATE_INSERT("insert", ""),
    OPERATE_UPDATE("update", ""),
    OPERATE_SELECT_BY("findBy", ""),
    OPERATE_SELECT_ALL("findAll", ""),
    OPERATE_DELETE_BY("deleteBy", ""),
    OPERATE_DELETE_ALL("deleteAll", "");

    static {
        XPathParser xPathParser = new XPathParser(new InputStreamReader(SQLOperateEnum.class.getResourceAsStream("/mapper-template.xml")));
        xPathParser.evalNode("/mapper").getChildren().forEach(e -> SQLOperateEnum.matchSQLOperate(e.getStringAttribute("id")).template = e.toString());
    }

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
