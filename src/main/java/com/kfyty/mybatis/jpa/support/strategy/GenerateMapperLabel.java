package com.kfyty.mybatis.jpa.support.strategy;

import com.kfyty.mybatis.jpa.support.configure.MapperMethodConfiguration;
import com.kfyty.mybatis.jpa.support.match.SQLConditionEnum;
import com.kfyty.mybatis.jpa.support.match.SQLOperateEnum;
import com.kfyty.mybatis.jpa.support.utils.CommonUtil;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 功能描述: 生成 mapper 标签策略
 *
 * @author kfyty725@hotmail.com
 * @date 2019/12/16 18:19
 * @since JDK 1.8
 */
public abstract class GenerateMapperLabel {
    protected String xml;
    protected String table;
    protected String where;
    protected SQLOperateEnum operateEnum;
    protected Class<?> returnType;
    protected Class<?> parameterType;
    protected List<String> queryParameters;
    protected MapperMethodConfiguration mapperMethodConfiguration;

    public GenerateMapperLabel(MapperMethodConfiguration mapperMethodConfiguration) {
        this.table = mapperMethodConfiguration.getTable();
        this.where = mapperMethodConfiguration.getWhere();
        this.operateEnum = mapperMethodConfiguration.getOperateEnum();
        this.returnType = mapperMethodConfiguration.getReturnType();
        this.parameterType = mapperMethodConfiguration.getParameterType();
        this.queryParameters = mapperMethodConfiguration.getQueryParameters();
        this.mapperMethodConfiguration = mapperMethodConfiguration;
    }

    /**
     * 生成单独的 mapper 标签
     * @return 单独的 <insert/> <update/> <select/> <delete/> 标签
     */
    public abstract String generateMapperLabel();

    /**
     * 获取生成 mapper 标签
     * @return 单独的 <insert/> <update/> <select/> <delete/> 标签
     */
    public String getMapperLabel() {
        if(CommonUtil.empty(this.xml)) {
            this.generateMapperLabel();
        }
        return this.xml;
    }

    /**
     * 获取生成 mapper 标签的模板
     * @return mapper 模板
     */
    protected String getMapperXmlTemplate() {
        String statement = mapperMethodConfiguration.getMapperMethod().getName();
        return operateEnum.template().replace("id=\"" + operateEnum.operate() + "\"", "id=\"" + statement + "\"");
    }

    /**
     * 生成条件
     * @return 条件 sql
     */
    protected String buildCondition() {
        return this.buildCondition(mapperMethodConfiguration.getMatchName(operateEnum));
    }

    /**
     * 根据条件字符串生成条件
     * @param conditionString 条件字符串
     * @return 条件 sql
     */
    protected String buildCondition(String conditionString) {
        StringBuilder builder = new StringBuilder();
        Matcher matcher = Pattern.compile("OrderBy|And|Or").matcher(conditionString);
        List<String> conditions = CommonUtil.split(conditionString, "OrderBy|And|Or");
        for (int i = 0; i < conditions.size(); i++) {
            if(conditions.get(i).contains("Asc") || conditions.get(i).contains("Desc")) {
                if(matcher.find()) {
                    throw new IllegalArgumentException("build sql error: order by must be last statement !");
                }
                return builder.append(this.where).append(this.buildConditionOfOrderBy(conditions.get(i))).toString();
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
        return builder.append(this.where).toString();
    }

    /**
     * 生成分支条件
     */
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

    /**
     * 生成 between 条件
     */
    private String buildConditionOfBetween(int index, List<String> queryParameters, SQLConditionEnum conditionEnum) {
        String sqlPart =  String.format(conditionEnum.template(), queryParameters.get(index), queryParameters.get(index + 1));
        queryParameters.remove(index);
        return sqlPart;
    }

    /**
     * 生成 null 条件
     */
    private String buildConditionOfNull(int index, List<String> queryParameters, SQLConditionEnum conditionEnum) {
        queryParameters.add(index, null);
        return conditionEnum.template();
    }

    /**
     * 生成 in 条件
     */
    private String buildConditionOfIn(int index, List<String> queryParameters, SQLConditionEnum conditionEnum) {
        StringBuilder builder = new StringBuilder();
        builder.append("<foreach collection=\"").append(queryParameters.get(index)).append("\" item=\"item\" separator=\", \">");
        builder.append("#{item}");
        builder.append("</foreach>");
        return String.format(conditionEnum.template(), builder.toString());
    }

    /**
     * 生成排序条件
     */
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
