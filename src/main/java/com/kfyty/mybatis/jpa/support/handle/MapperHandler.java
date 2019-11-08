package com.kfyty.mybatis.jpa.support.handle;

import com.kfyty.mybatis.jpa.support.annotation.JpaQuery;
import com.kfyty.mybatis.jpa.support.match.SQLConditionEnum;
import com.kfyty.mybatis.jpa.support.match.SQLOperateEnum;
import com.kfyty.mybatis.jpa.support.utils.CommonUtil;

import java.util.Arrays;
import java.util.List;
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
        this.table = CommonUtil.convert2Underline(this.methodHandler.getReturnType().getSimpleName().replace(methodHandler.getSuffix(), ""), true);
        if(table.equalsIgnoreCase("void")) {
            this.table = methodHandler.getMethod().getAnnotation(JpaQuery.class).table();
            if(CommonUtil.empty(table)) {
                this.table = CommonUtil.convert2Underline(methodHandler.getMethod().getDeclaringClass().getSimpleName().replaceAll("Mapper|Dao", ""), true);
                if(CommonUtil.empty(table)) {
                    throw new IllegalArgumentException("build sql error: parse table name failed !");
                }
            }
        }
        return this.table;
    }

    public String getMapperXml() {
        if(xml == null) {
            this.parse();
        }
        return this.xml;
    }

    public String getMapperLabel() {
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
        return null;
    }

    private void operateSelectBy() {
        this.xml = String.format(this.getMapperXmlTemplate(), methodHandler.getReturnType().getName(), this.getTable(), this.buildCondition());
    }

    private void operateSelectAll() {
        this.xml = String.format(this.getMapperXmlTemplate(), methodHandler.getReturnType().getName(), this.getTable());
    }

    private void operateDeleteBy() {
        this.xml = String.format(this.getMapperXmlTemplate(), this.getTable(), this.buildCondition());
    }

    private void operateDeleteAll() {
        this.xml = String.format(this.getMapperXmlTemplate(), this.getTable());
    }

    private String getMapperXmlTemplate() {
        return operateEnum.template().replace(operateEnum.operate(), methodHandler.getMethod().getName());
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
                builder.append(this.buildConditionOrderBy(conditions.get(i)));
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
            builder.append(buildConditionBetween(index, queryParameters, conditionEnum));
            return;
        }
        if(conditionEnum.equals(SQLConditionEnum.CONDITION_IsNull)) {
            builder.append(buildConditionIsNull(index, queryParameters, conditionEnum));
            return;
        }
        if(conditionEnum.equals(SQLConditionEnum.CONDITION_NotNull)) {
            builder.append(buildConditionNotNull(index, queryParameters, conditionEnum));
            return;
        }
        builder.append(String.format(conditionEnum.template(), queryParameters.get(index)));
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

    private String buildConditionOrderBy(String column) {
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
