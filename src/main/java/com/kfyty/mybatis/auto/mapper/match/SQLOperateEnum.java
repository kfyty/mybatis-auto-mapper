package com.kfyty.mybatis.auto.mapper.match;

import com.kfyty.mybatis.auto.mapper.exception.SQLOperateMatchException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.ibatis.parsing.XPathParser;

import java.io.InputStreamReader;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 功能描述: SQL 操作枚举
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/7 10:27
 * @since JDK 1.8
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SQLOperateEnum {
    OPERATE_UPDATE_BY("updateBy", "", Pattern.compile("update(.*?)By")),
    OPERATE_SELECT_BY("findBy", "", Pattern.compile("find(.*?)By")),
    OPERATE_SELECT_ALL("findAll", "", null),
    OPERATE_PAGE_BY("pageBy", "", Pattern.compile("page(.*?)By")),
    OPERATE_PAGE_ALL("pageAll", "", null),
    OPERATE_DELETE_BY("deleteBy", "", Pattern.compile("delete(.*?)By")),
    OPERATE_DELETE_ALL("deleteAll", "", null),
    OPERATE_COUNT_BY("countBy", "", Pattern.compile("count(.*?)By")),
    OPERATE_COUNT_All("countAll", "", null),
    OPERATE_INSERT_ALL("insertAll", "", null),
    OPERATE_UPDATE_ALL("updateAll", "", null),
    OPERATE_INSERT("insert", "", null),
    OPERATE_UPDATE("update", "", null);

    static {
        XPathParser xPathParser = new XPathParser(new InputStreamReader(SQLOperateEnum.class.getResourceAsStream("/mapper-template.xml")));
        xPathParser.evalNode("/mapper").getChildren().forEach(e -> SQLOperateEnum.matchSQLOperate(e.getStringAttribute("id")).template = e.toString());
    }

    private String operate;
    private String template;
    private Pattern pattern;

    public String operate() {
        return this.operate;
    }

    public String template() {
        return this.template;
    }

    public Pattern pattern() {
        return this.pattern;
    }

    public static SQLOperateEnum matchSQLOperate(String methodName) {
        Objects.requireNonNull(methodName, "sql operate match error: method name is null !");
        return matchSQLOperateByEqualsChain(methodName);
    }

    private static SQLOperateEnum matchSQLOperateByEqualsChain(String methodName) {
        for (SQLOperateEnum value : SQLOperateEnum.values()) {
            if(value.operate().equals(methodName)) {
                return value;
            }
        }
        return matchSQLOperateByStartChain(methodName);
    }

    private static SQLOperateEnum matchSQLOperateByStartChain(String methodName) {
        for (SQLOperateEnum value : SQLOperateEnum.values()) {
            if(methodName.startsWith(value.operate())) {
                return value;
            }
        }
        return matchSQLOperateByPatternChain(methodName);
    }

    private static SQLOperateEnum matchSQLOperateByPatternChain(String methodName) {
        for (SQLOperateEnum value : SQLOperateEnum.values()) {
            if(value.pattern == null) {
                continue;
            }
            Matcher matcher = value.pattern.matcher(methodName);
            if(matcher.find()) {
                return value;
            }
        }
        throw new SQLOperateMatchException("SQL operate match failed: " + methodName);
    }
}
