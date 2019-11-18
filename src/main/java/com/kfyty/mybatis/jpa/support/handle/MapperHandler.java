package com.kfyty.mybatis.jpa.support.handle;

import com.kfyty.mybatis.jpa.support.annotation.JpaQuery;
import com.kfyty.mybatis.jpa.support.match.SQLConditionEnum;
import com.kfyty.mybatis.jpa.support.match.SQLOperateEnum;
import com.kfyty.mybatis.jpa.support.utils.CommonUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 功能描述: Mapper 处理器，得到 Mapper 标签
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/6 18:19
 * @since JDK 1.8
 */
public class MapperHandler {
    private String xml;
    private String table;
    private SQLOperateEnum operateEnum;
    private MethodHandler methodHandler;

    public MapperHandler(MethodHandler methodHandler) {
        this.methodHandler = methodHandler;
    }

    public MapperHandler parse() {
        try {
            this.operateEnum = SQLOperateEnum.matchSQLOperate(methodHandler.getMethod().getName());
            this.getClass().getDeclaredMethod(CommonUtil.convert2Hump(operateEnum.name(), false)).invoke(this);
            return this;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getTable() {
        if(table != null) {
            return this.table;
        }
        this.table = methodHandler.getMethod().getAnnotation(JpaQuery.class).table();
        if(!CommonUtil.empty(this.table)) {
            return this.table;
        }
        this.table = CommonUtil.convert2Underline(this.methodHandler.getReturnType().getSimpleName().replace(methodHandler.getSuffix(), ""), true);
        if(!this.tableNameInvalid()) {
            return this.table;
        }
        if(operateEnum.equals(SQLOperateEnum.OPERATE_INSERT) || operateEnum.equals(SQLOperateEnum.OPERATE_INSERT_ALL) ||
                operateEnum.equals(SQLOperateEnum.OPERATE_UPDATE) || operateEnum.equals(SQLOperateEnum.OPERATE_UPDATE_ALL)) {
            this.table = CommonUtil.convert2Underline(methodHandler.getParameterType().getSimpleName().replace(methodHandler.getSuffix(), ""), true);
            return this.table;
        }
        this.table = CommonUtil.convert2Underline(methodHandler.getMethod().getDeclaringClass().getSimpleName().replaceAll("Mapper|Dao", ""), true);
        return this.table;
    }

    public String getMapperXml() {
        return this.xml != null ? this.xml : this.parse().getMapperXml();
    }

    private String getMapperXmlTemplate() {
        return operateEnum.template().replace("id=\"" + operateEnum.operate() + "\"", "id=\"" + methodHandler.getMethod().getName() + "\"");
    }

    public String getMapperXmlLabel() {
        if(operateEnum.name().contains("INSERT")) {
            return "/insert";
        }
        if(operateEnum.name().contains("UPDATE")) {
            return "/update";
        }
        if(operateEnum.name().contains("DELETE")) {
            return "/delete";
        }
        if(operateEnum.name().contains("SELECT") || operateEnum.name().contains("PAGE") || operateEnum.name().contains("COUNT")) {
            return "/select";
        }
        throw new IllegalArgumentException("build sql error: match mapper label failed !");
    }

    private boolean tableNameInvalid() {
        return !methodHandler.getColumns().equals("*")               ||
                methodHandler.getReturnType().equals(Void.class)     ||
                methodHandler.getReturnType().equals(void.class)     ||
                methodHandler.getReturnType().equals(Integer.class)  ||
                methodHandler.getReturnType().equals(int.class)      ||
                methodHandler.getReturnType().equals(HashMap.class);
    }

    private void operateInsert() {
        String[] insertStatement = this.buildInsertStatement(methodHandler.getQueryParameters().get(0));
        this.xml = String.format(this.getMapperXmlTemplate(), methodHandler.getParameterType().getName(), this.getTable(), insertStatement[0], insertStatement[1]);
    }

    private void operateInsertAll() {
        String[] insertStatement = this.buildInsertAllStatement();
        this.xml = String.format(this.getMapperXmlTemplate(), methodHandler.getParameterType().getName(), this.getTable(), insertStatement[0], methodHandler.getQueryParameters().get(0), insertStatement[1]);
    }

    private void operateUpdate() {
        String entity = methodHandler.getQueryParameters().get(0);
        this.xml = String.format(this.getMapperXmlTemplate(), methodHandler.getParameterType().getName(), this.getTable(), this.buildUpdateStatement(entity), this.buildUpdatePrimaryKeyCondition(entity));
    }

    private void operateUpdateAll() {
        this.xml = String.format(this.getMapperXmlTemplate(), methodHandler.getParameterType().getName(), methodHandler.getQueryParameters().get(0), this.getTable(), this.buildUpdateAllStatement(), this.buildUpdatePrimaryKeyCondition("item"));
    }

    private void operateUpdateBy() {
        String[] condition = this.buildUpdateCondition();
        this.xml = String.format(this.getMapperXmlTemplate(), this.getTable(), condition[0], condition[1]);
    }

    private void operateSelectBy() {
        this.xml = String.format(this.getMapperXmlTemplate(), methodHandler.getReturnType().getName(), methodHandler.getColumns(), this.getTable(), this.buildCondition());
    }

    private void operateSelectAll() {
        this.xml = String.format(this.getMapperXmlTemplate(), methodHandler.getReturnType().getName(), methodHandler.getColumns(), this.getTable());
    }

    private void operatePageBy() {
        this.operateSelectBy();
    }

    private void operatePageAll() {
        this.operateSelectAll();
    }

    private void operateDeleteBy() {
        this.xml = String.format(this.getMapperXmlTemplate(), this.getTable(), this.buildCondition());
    }

    private void operateDeleteAll() {
        this.xml = String.format(this.getMapperXmlTemplate(), this.getTable());
    }

    private void operateCountBy() {
        String columns = methodHandler.getColumns().equals("*") ? "count(*)" : methodHandler.getColumns();
        this.xml = String.format(this.getMapperXmlTemplate(), methodHandler.getReturnType().getName(), columns, this.getTable(), this.buildCondition());
    }

    private void operateCountAll() {
        String columns = methodHandler.getColumns().equals("*") ? "count(*)" : methodHandler.getColumns();
        this.xml = String.format(this.getMapperXmlTemplate(), methodHandler.getReturnType().getName(), columns, this.getTable());
    }

    private String[] buildInsertStatement(String entity) {
        StringBuilder[] builder = new StringBuilder[] {new StringBuilder(), new StringBuilder()};
        Map<String, Field> fieldMap = CommonUtil.getFieldMap(methodHandler.getParameterType());
        builder[1].append(methodHandler.useDefault() ? "<trim suffixOverrides=\", \">" : "");
        for (String field : fieldMap.keySet()) {
            builder[0].append(CommonUtil.convert2Underline(field, true)).append(", ");
            if(!methodHandler.useDefault()) {
                builder[1].append("#{").append(entity).append(".").append(field).append("}, ");
                continue;
            }
            builder[1].append("<if test=\"").append(entity).append(".").append(field).append(" == null\">");
            builder[1].append("default, ");
            builder[1].append("</if>");
            builder[1].append("<if test=\"").append(entity).append(".").append(field).append(" != null\">");
            builder[1].append("#{").append(entity).append(".").append(field).append("}, ");
            builder[1].append("</if>");
        }
        builder[1].append(methodHandler.useDefault() ? "</trim>" : "");
        return new String[] {
                builder[0].delete(builder[0].length() - 2, builder[0].length()).toString(),
                methodHandler.useDefault() ? builder[1].toString() : builder[1].delete(builder[1].length() - 2, builder[1].length()).toString()
        };
    }

    private String[] buildInsertAllStatement() {
        return buildInsertStatement("item");
    }

    private String buildUpdatePrimaryKeyCondition(String entity) {
        StringBuilder builder = new StringBuilder();
        String[] primaryKeys = methodHandler.getMethod().getAnnotation(JpaQuery.class).primaryKey();
        for (int i = 0; i < primaryKeys.length; i++) {
            builder.append(primaryKeys[i]).append(" = #{").append(entity).append(".").append(CommonUtil.convert2Hump(primaryKeys[i], false)).append("}");
            if(i != primaryKeys.length - 1) {
                builder.append(" and ");
            }
        }
        return builder.toString();
    }

    private String buildUpdateStatement(String entity) {
        StringBuilder builder = new StringBuilder();
        Map<String, Field> fieldMap = CommonUtil.getFieldMap(methodHandler.getParameterType());
        for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
            builder.append("<if test=\"");
            builder.append(entity).append(".").append(entry.getKey()).append(" != null ");
            if(String.class.isAssignableFrom(entry.getValue().getType())) {
                builder.append(" and ").append(entity).append(".").append(entry.getKey()).append(" != '' ");
            }
            builder.append("\">");
            builder.append(CommonUtil.convert2Underline(entry.getKey(), true)).append(" = ").append("#{").append(entity).append(".").append(entry.getKey()).append("}, ");
            builder.append("</if>");
        }
        return builder.toString();
    }

    private String buildUpdateAllStatement() {
        return buildUpdateStatement("item");
    }

    private String[] buildUpdateCondition() {
        StringBuilder builder = new StringBuilder();
        List<String> queryParameters = methodHandler.getQueryParameters();
        List<String> updateConditions = CommonUtil.split(methodHandler.getMethod().getName().replaceFirst(operateEnum.operate(), ""), "Set");
        if(updateConditions.size() != 2) {
            throw new IllegalArgumentException("build sql error: cannot parse condition and set statement !");
        }
        List<String> conditions = CommonUtil.split(updateConditions.get(1), "And");
        int index = queryParameters.size() - conditions.size();
        for(int i = 0; i < conditions.size(); i++) {
            builder.append(CommonUtil.convert2Underline(conditions.get(i), true)).append(" = #{").append(queryParameters.get(i + index)).append("}");
            if(i != conditions.size() - 1) {
                builder.append(", ");
            }
        }
        return new String[] {
                builder.toString(),
                this.buildCondition(updateConditions.get(0))
        };
    }

    private String buildCondition() {
        return this.buildCondition(methodHandler.getMethod().getName().replaceFirst(operateEnum.operate(), ""));
    }

    private String buildCondition(String conditionString) {
        StringBuilder builder = new StringBuilder();
        Matcher matcher = Pattern.compile("OrderBy|And|Or").matcher(conditionString);
        List<String> queryParameters = this.methodHandler.getQueryParameters();
        List<String> conditions = CommonUtil.split(conditionString, "OrderBy|And|Or");
        for (int i = 0; i < conditions.size(); i++) {
            if(conditions.get(i).contains("Asc") || conditions.get(i).contains("Desc")) {
                if(matcher.find()) {
                    throw new IllegalArgumentException("build sql error: order by must be last statement !");
                }
                builder.append(this.buildConditionOfOrderBy(conditions.get(i)));
                break;
            }
            SQLConditionEnum conditionEnum = SQLConditionEnum.matchSQLCondition(conditions.get(i));
            String column = Arrays.stream(conditions.get(i).split(conditionEnum.condition())).filter(e -> !e.isEmpty()).findFirst().get();
            builder.append(CommonUtil.convert2Underline(column, true));
            this.buildBranchCondition(i, queryParameters, conditionEnum, builder);
            if(matcher.find()) {
                if(!matcher.group().equalsIgnoreCase("OrderBy")) {
                    builder.append(SQLConditionEnum.matchSQLCondition(matcher.group()).separate());
                }
            }
        }
        return builder.toString();
    }

    private void buildBranchCondition(int index, List<String> queryParameters, SQLConditionEnum conditionEnum, StringBuilder builder) {
        if(conditionEnum.equals(SQLConditionEnum.CONDITION_BETWEEN)) {
            builder.append(buildConditionOfBetween(index, queryParameters, conditionEnum));
            return;
        }
        if(conditionEnum.equals(SQLConditionEnum.CONDITION_IsNull) || conditionEnum.equals(SQLConditionEnum.CONDITION_NotNull)) {
            builder.append(buildConditionOfNull(index, queryParameters, conditionEnum));
            return;
        }
        if(conditionEnum.equals(SQLConditionEnum.CONDITION_In) || conditionEnum.equals(SQLConditionEnum.CONDITION_NotIn)) {
            builder.append(buildConditionOfIn(index, queryParameters, conditionEnum));
            return;
        }
        builder.append(String.format(conditionEnum.template(), queryParameters.get(index)));
    }

    private String buildConditionOfBetween(int index, List<String> queryParameters, SQLConditionEnum conditionEnum) {
        String sqlPart =  String.format(conditionEnum.template(), queryParameters.get(index), queryParameters.get(index + 1));
        queryParameters.remove(index);
        return sqlPart;
    }

    private String buildConditionOfNull(int index, List<String> queryParameters, SQLConditionEnum conditionEnum) {
        queryParameters.add(index, null);
        return conditionEnum.template();
    }

    private String buildConditionOfIn(int index, List<String> queryParameters, SQLConditionEnum conditionEnum) {
        StringBuilder builder = new StringBuilder();
        builder.append("<foreach collection=\"").append(queryParameters.get(index)).append("\" item=\"item\" separator=\", \">");
        builder.append("#{item}");
        builder.append("</foreach>");
        return String.format(conditionEnum.template(), builder.toString());
    }

    private String buildConditionOfOrderBy(String column) {
        StringBuilder builder = new StringBuilder();
        List<String> conditions = CommonUtil.split(column, "Asc|Desc");
        builder.append(" order by ");
        for (int i = 0; i < conditions.size(); i++) {
            column = column.replaceFirst(conditions.get(i), "");
            SQLConditionEnum conditionEnum = column.startsWith(SQLConditionEnum.CONDITION_OrderByAsc.condition()) ? SQLConditionEnum.CONDITION_OrderByAsc : SQLConditionEnum.CONDITION_OrderByDesc;
            builder.append(String.format(conditionEnum.template(), CommonUtil.convert2Underline(conditions.get(i), true)));
            if(i != conditions.size() - 1) {
                builder.append(", ");
            }
            column = column.replaceFirst(conditionEnum.condition(), "");
        }
        return builder.toString();
    }
}
