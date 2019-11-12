package com.kfyty.mybatis.jpa.support.handle;

import com.kfyty.mybatis.jpa.support.annotation.JpaQuery;
import com.kfyty.mybatis.jpa.support.match.SQLConditionEnum;
import com.kfyty.mybatis.jpa.support.match.SQLOperateEnum;
import com.kfyty.mybatis.jpa.support.utils.CommonUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        if(operateEnum.equals(SQLOperateEnum.OPERATE_SELECT_BY) || operateEnum.equals(SQLOperateEnum.OPERATE_SELECT_ALL) ||
                operateEnum.equals(SQLOperateEnum.OPERATE_DELETE_BY) || operateEnum.equals(SQLOperateEnum.OPERATE_DELETE_ALL)) {
            this.table = CommonUtil.convert2Underline(methodHandler.getMethod().getDeclaringClass().getSimpleName().replaceAll("Mapper|Dao", ""), true);
            return this.table;
        }
        this.table = CommonUtil.convert2Underline(methodHandler.getParameterType().getSimpleName().replace(methodHandler.getSuffix(), ""), true);
        return this.table;
    }

    public String getMapperXml() {
        if(xml == null) {
            this.parse();
        }
        return this.xml;
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
        if(operateEnum.name().contains("SELECT")) {
            return "/select";
        }
        if(operateEnum.name().contains("DELETE")) {
            return "/delete";
        }
        throw new IllegalArgumentException("build sql error: match mapper label failed !");
    }

    private boolean tableNameInvalid() {
        return !methodHandler.getColumns().equals("*")               ||
                methodHandler.getReturnType().equals(Void.class)     ||
                methodHandler.getReturnType().equals(void.class)     ||
                methodHandler.getReturnType().equals(Integer.class)  ||
                methodHandler.getReturnType().equals(int.class);
    }

    private void operateInsert() {
        String[] insertStatement = this.buildInsertStatement();
        this.xml = String.format(this.getMapperXmlTemplate(), methodHandler.getParameterType().getName(), this.getTable(), insertStatement[0], insertStatement[1]);
    }

    private void operateInsertAll() {
        String[] insertStatement = this.buildInsertAllStatement();
        this.xml = String.format(this.getMapperXmlTemplate(), methodHandler.getParameterType().getName(), this.getTable(), insertStatement[0], methodHandler.getQueryParameters().get(0), insertStatement[1]);
    }

    private void operateUpdate() {
        String entity = methodHandler.getQueryParameters().get(0);
        String primaryKey = methodHandler.getMethod().getAnnotation(JpaQuery.class).primaryKey();
        this.xml = String.format(this.getMapperXmlTemplate(), methodHandler.getParameterType().getName(), this.getTable(), this.buildUpdateStatement(), primaryKey, "#{" + entity + "." + primaryKey + "}");
    }

    private void operateUpdateAll() {
        String primaryKey = methodHandler.getMethod().getAnnotation(JpaQuery.class).primaryKey();
        this.xml = String.format(this.getMapperXmlTemplate(), methodHandler.getParameterType().getName(), methodHandler.getQueryParameters().get(0), this.getTable(), this.buildUpdateAllStatement(), primaryKey, "#{item." + primaryKey + "}");
    }

    private void operateSelectBy() {
        this.xml = String.format(this.getMapperXmlTemplate(), methodHandler.getReturnType().getName(), methodHandler.getColumns(), this.getTable(), this.buildCondition());
    }

    private void operateSelectAll() {
        this.xml = String.format(this.getMapperXmlTemplate(), methodHandler.getReturnType().getName(), methodHandler.getColumns(), this.getTable());
    }

    private void operateDeleteBy() {
        this.xml = String.format(this.getMapperXmlTemplate(), this.getTable(), this.buildCondition());
    }

    private void operateDeleteAll() {
        this.xml = String.format(this.getMapperXmlTemplate(), this.getTable());
    }

    private String[] buildInsertStatement() {
        StringBuilder[] builder = new StringBuilder[] {new StringBuilder(), new StringBuilder()};
        String entity = methodHandler.getQueryParameters().get(0);
        Map<String, Field> fieldMap = CommonUtil.getFieldMap(methodHandler.getParameterType());
        for (String field : fieldMap.keySet()) {
            builder[0].append(CommonUtil.convert2Underline(field, true)).append(", ");
            builder[1].append("#{").append(entity).append(".").append(field).append("}, ");
        }
        return new String[] {
                builder[0].delete(builder[0].length() - 2, builder[0].length()).toString(),
                builder[1].delete(builder[1].length() - 2, builder[1].length()).toString()
        };
    }

    private String[] buildInsertAllStatement() {
        StringBuilder[] builder = new StringBuilder[] {new StringBuilder(), new StringBuilder()};
        Map<String, Field> fieldMap = CommonUtil.getFieldMap(methodHandler.getParameterType());
        for (String field : fieldMap.keySet()) {
            builder[0].append(CommonUtil.convert2Underline(field, true)).append(", ");
            builder[1].append("#{item.").append(field).append("}, ");
        }
        return new String[] {
                builder[0].delete(builder[0].length() - 2, builder[0].length()).toString(),
                builder[1].delete(builder[1].length() - 2, builder[1].length()).toString()
        };
    }

    private String buildUpdateStatement() {
        StringBuilder builder = new StringBuilder();
        String entity = methodHandler.getQueryParameters().get(0);
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
        StringBuilder builder = new StringBuilder();
        Map<String, Field> fieldMap = CommonUtil.getFieldMap(methodHandler.getParameterType());
        for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
            builder.append("<if test=\"");
            builder.append("item.").append(entry.getKey()).append(" != null ");
            if(String.class.isAssignableFrom(entry.getValue().getType())) {
                builder.append(" and ").append("item.").append(entry.getKey()).append(" != '' ");
            }
            builder.append("\">");
            builder.append(CommonUtil.convert2Underline(entry.getKey(), true)).append(" = ").append("#{item.").append(entry.getKey()).append("}, ");
            builder.append("</if>");
        }
        return builder.toString();
    }

    private String buildCondition() {
        StringBuilder builder = new StringBuilder();
        String methodName = methodHandler.getMethod().getName().replace(operateEnum.operate(), "");
        Matcher matcher = Pattern.compile("OrderBy|And|Or").matcher(methodName);
        List<String> queryParameters = this.methodHandler.getQueryParameters();
        List<String> conditions = Arrays.stream(methodName.split("OrderBy|And|Or")).filter(e -> !e.isEmpty()).collect(Collectors.toList());
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
        List<String> conditions = Arrays.stream(column.split("Asc|Desc")).filter(e -> !e.isEmpty()).collect(Collectors.toList());
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
