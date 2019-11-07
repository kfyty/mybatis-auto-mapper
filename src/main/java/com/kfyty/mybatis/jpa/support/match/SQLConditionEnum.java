package com.kfyty.mybatis.jpa.support.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 功能描述: SQL 条件枚举
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/7 10:27
 * @since JDK 1.8
 */
public enum SQLConditionEnum {
    CONDITION_OR("Or", "", " or "),
    CONDITION_AND("And", "", " and "),
    CONDITION_EQUAL("Equals", " = #{%s} ", ""),
    CONDITION_NotEqual("NotEqual", " <> #{%s} ", ""),
    CONDITION_BETWEEN("Between", " between #{%s} and #{%s} ", ""),
    CONDITION_LessThan("LessThan", " < #{%s} ", ""),
    CONDITION_LessEqual("LessEqual", " <= #{%s} ", ""),
    CONDITION_GreaterThan("GreaterThan", " > #{%s} ", ""),
    CONDITION_GreaterEqual("GreaterEqual", " >= #{%s} ", ""),
    CONDITION_IsNull("IsNull", " is null ", ""),
    CONDITION_NotNull("NotNull", " is not null ", ""),
    CONDITION_Like("Like", " like ${%s} ", ""),
    CONDITION_NotLike("NotLike", " not like ${%s} ", ""),
    CONDITION_OrderByAsc("Asc", " order by %s asc ", ""),
    CONDITION_OrderByDesc("Desc", " order by %s desc ", ""),
    CONDITION_In("In", " in ( %s ) ", ""),
    CONDITION_NotIn("NotIn", " not in ( %s ) ", ""),
    CONDITION_DEFAULT("Default", " = #{%s} ", "");

    private String condition;
    private String template;
    private String separate;

    private SQLConditionEnum(String condition, String template, String separate) {
        this.condition = condition;
        this.template = template;
        this.separate = separate;
    }

    public String condition() {
        return this.condition;
    }

    public String template() {
        return this.template;
    }

    public String separate() {
        return this.separate;
    }

    public static SQLConditionEnum matchSQLCondition(String methodName) {
        Objects.requireNonNull(methodName, "sql condition match error: method name is null !");
        List<SQLConditionEnum> conditionEnums = new ArrayList<>();
        for (SQLConditionEnum value : SQLConditionEnum.values()) {
            if(methodName.contains(value.condition())) {
                conditionEnums.add(value);
            }
        }
        if(conditionEnums.size() > 1) {
            throw new IllegalArgumentException("sql condition match error: more than one matched !");
        }
        return conditionEnums.isEmpty() ? CONDITION_DEFAULT : conditionEnums.get(0);
    }
}
