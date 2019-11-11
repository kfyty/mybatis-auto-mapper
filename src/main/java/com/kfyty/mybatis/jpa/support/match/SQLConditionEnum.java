package com.kfyty.mybatis.jpa.support.match;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
    CONDITION_BETWEEN("Between", " between #{%s} and #{%s} ", ""),
    CONDITION_LessThan("LessThan", " &lt; #{%s} ", ""),
    CONDITION_LessEqual("LessEqual", " &lt;= #{%s} ", ""),
    CONDITION_GreaterThan("GreaterThan", " &gt; #{%s} ", ""),
    CONDITION_GreaterEqual("GreaterEqual", " &gt;= #{%s} ", ""),
    CONDITION_NotEqual("NotEqual", " &lt;&gt; #{%s} ", ""),
    CONDITION_EQUAL("Equal", " = #{%s} ", ""),
    CONDITION_NotNull("NotNull", " is not null ", ""),
    CONDITION_IsNull("IsNull", " is null ", ""),
    CONDITION_NotLike("NotLike", " not like '${%s}' ", ""),
    CONDITION_Like("Like", " like '${%s}' ", ""),
    CONDITION_NotIn("NotIn", " not in ( %s ) ", ""),
    CONDITION_In("In", " in ( %s ) ", ""),
    CONDITION_OrderByAsc("Asc", " %s asc ", ""),
    CONDITION_OrderByDesc("Desc", " %s desc ", ""),
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
        Set<SQLConditionEnum> conditionEnums = new HashSet<>();
        for (SQLConditionEnum value : SQLConditionEnum.values()) {
            if(methodName.contains(value.condition())) {
                if(value.equals(SQLConditionEnum.CONDITION_Like) && conditionEnums.contains(SQLConditionEnum.CONDITION_NotLike)) {
                    continue;
                }
                if(value.equals(SQLConditionEnum.CONDITION_In) && conditionEnums.contains(SQLConditionEnum.CONDITION_NotIn)) {
                    continue;
                }
                if(value.equals(SQLConditionEnum.CONDITION_EQUAL) && (
                        conditionEnums.contains(SQLConditionEnum.CONDITION_NotEqual) ||
                        conditionEnums.contains(SQLConditionEnum.CONDITION_LessEqual) ||
                        conditionEnums.contains(SQLConditionEnum.CONDITION_GreaterEqual))) {
                    continue;
                }
                conditionEnums.add(value);
            }
        }
        if(conditionEnums.size() > 1) {
            throw new IllegalArgumentException("sql condition match error: more than one matched !");
        }
        return conditionEnums.isEmpty() ? CONDITION_DEFAULT : conditionEnums.iterator().next();
    }
}
