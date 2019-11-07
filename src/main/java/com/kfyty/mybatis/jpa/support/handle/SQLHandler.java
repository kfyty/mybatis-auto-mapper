package com.kfyty.mybatis.jpa.support.handle;

import com.kfyty.mybatis.jpa.support.match.SQLConditionEnum;
import com.kfyty.mybatis.jpa.support.match.SQLOperateEnum;
import com.kfyty.mybatis.jpa.support.utils.CommonUtil;
import org.apache.ibatis.mapping.SqlCommandType;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 功能描述: SQL 处理器，得到 SQL 语句
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/6 18:19
 * @since JDK 1.8
 */
public class SQLHandler {
    private String sql;
    private String table;
    private SQLOperateEnum operateEnum;
    private MethodHandler methodHandler;

    public SQLHandler(MethodHandler methodHandler) {
        this.methodHandler = methodHandler;
    }

    public SQLHandler parse() {
        try {
            this.table = CommonUtil.convert2Underline(this.methodHandler.getReturnType().getSimpleName().replace(methodHandler.getSuffix(), ""), true);
            this.operateEnum = SQLOperateEnum.matchSQLOperate(methodHandler.getMethod().getName());
            this.getClass().getDeclaredMethod(CommonUtil.convert2Hump(operateEnum.name(), false)).invoke(this);
            return this;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getSQL() {
        if(sql == null) {
            this.parse();
        }
        return this.sql;
    }

    public SqlCommandType getSQLCommandType() {
        if(operateEnum.name().contains("INSERT")) {
            return SqlCommandType.INSERT;
        }
        if(operateEnum.name().contains("UPDATE")) {
            return SqlCommandType.UPDATE;
        }
        if(operateEnum.name().contains("SELECT")) {
            return SqlCommandType.SELECT;
        }
        if(operateEnum.name().contains("DELETE")) {
            return SqlCommandType.DELETE;
        }
        return SqlCommandType.UNKNOWN;
    }

    private void operateSelectAll() {
        this.sql = String.format(this.operateEnum.template(), this.table);
    }

    private void operateSelectBy() {
        this.sql = String.format(operateEnum.template(), this.table, this.buildCondition());
    }

    private void operateDeleteAll() {
        this.sql = String.format(this.operateEnum.template(), this.table);
    }

    private void operateDeleteBy() {
        this.sql = String.format(operateEnum.template(), this.table, this.buildCondition());
    }

    private String buildCondition() {
        StringBuilder builder = new StringBuilder();
        String methodName = methodHandler.getMethod().getName().replace(operateEnum.operate(), "");
        Matcher matcher = Pattern.compile("OrderBy|And|Or").matcher(methodName);
        List<String> queryParameters = this.methodHandler.getQueryParameters();
        List<String> conditions = Arrays.stream(methodName.split("OrderBy|And|Or")).filter(e -> !e.isEmpty()).collect(Collectors.toList());
        for (int i = 0; i < conditions.size(); i++) {
            SQLConditionEnum conditionEnum = SQLConditionEnum.matchSQLCondition(conditions.get(i));
            String column = Arrays.stream(conditions.get(i).split(conditionEnum.condition())).filter(e -> !e.isEmpty()).findFirst().get();
            this.buildBranchCondition(i, column, queryParameters, conditionEnum, builder);
            if(matcher.find()) {
                String group = matcher.group();
                if(!group.equalsIgnoreCase("OrderBy")) {
                    builder.append(SQLConditionEnum.matchSQLCondition(group).separate());
                }
            }
        }
        return builder.toString();
    }

    private void buildBranchCondition(int index, String column, List<String> queryParameters, SQLConditionEnum conditionEnum, StringBuilder builder) {
        if(conditionEnum.equals(SQLConditionEnum.CONDITION_BETWEEN)) {
            builder.append(CommonUtil.convert2Underline(column, true)).append(buildConditionBetween(index, queryParameters, conditionEnum));
            return;
        }
        if(conditionEnum.equals(SQLConditionEnum.CONDITION_IsNull)) {
            builder.append(CommonUtil.convert2Underline(column, true)).append(buildConditionIsNull(index, queryParameters, conditionEnum));
            return;
        }
        if(conditionEnum.equals(SQLConditionEnum.CONDITION_NotNull)) {
            builder.append(CommonUtil.convert2Underline(column, true)).append(buildConditionNotNull(index, queryParameters, conditionEnum));
            return;
        }
        if(conditionEnum.equals(SQLConditionEnum.CONDITION_OrderByAsc) || conditionEnum.equals(SQLConditionEnum.CONDITION_OrderByDesc)) {
            builder.append(buildConditionOrderBy(index, column, queryParameters, conditionEnum));
            return;
        }
        builder.append(CommonUtil.convert2Underline(column, true)).append(String.format(conditionEnum.template(), queryParameters.get(index)));
    }

    private String buildConditionBetween(int index, List<String> queryParameters, SQLConditionEnum conditionEnum) {
        String sqlPart =  String.format(conditionEnum.template(), queryParameters.get(index), queryParameters.get(index + 1));
        queryParameters.remove(index);
        return sqlPart;
    }

    private String buildConditionIsNull(int index, List<String> queryParameters, SQLConditionEnum conditionEnum) {
        queryParameters.add(index, null);
        return conditionEnum.template();
    }

    private String buildConditionNotNull(int index, List<String> queryParameters, SQLConditionEnum conditionEnum) {
        queryParameters.add(index, null);
        return conditionEnum.template();
    }

    private String buildConditionOrderBy(int index, String column, List<String> queryParameters, SQLConditionEnum conditionEnum) {
        queryParameters.add(index, null);
        return String.format(conditionEnum.template(), CommonUtil.convert2Underline(column, true));
    }
}
