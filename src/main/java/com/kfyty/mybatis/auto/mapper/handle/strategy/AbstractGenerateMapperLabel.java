package com.kfyty.mybatis.auto.mapper.handle.strategy;

import com.kfyty.mybatis.auto.mapper.BaseMapper;
import com.kfyty.mybatis.auto.mapper.annotation.Column;
import com.kfyty.mybatis.auto.mapper.configure.MapperMethodConfiguration;
import com.kfyty.mybatis.auto.mapper.match.SQLConditionEnum;
import com.kfyty.mybatis.auto.mapper.match.SQLOperateEnum;
import com.kfyty.core.utils.CommonUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 功能描述: 生成 mapper 标签策略
 *
 * @author kfyty725@hotmail.com
 * @date 2019/12/16 18:19
 * @since JDK 1.8
 */
@Slf4j
@NoArgsConstructor
public abstract class AbstractGenerateMapperLabel {
    protected String xml;
    protected String table;
    protected String where;
    protected SQLOperateEnum operateEnum;
    protected Class<?> returnType;
    protected Class<?> parameterType;
    protected List<String> queryParameters;
    protected MapperMethodConfiguration mapperMethodConfiguration;

    public AbstractGenerateMapperLabel(MapperMethodConfiguration mapperMethodConfiguration) {
        this.initMapperMethodConfiguration(mapperMethodConfiguration);
    }

    public void initMapperMethodConfiguration(MapperMethodConfiguration mapperMethodConfiguration) {
        this.table = mapperMethodConfiguration.getTable();
        this.where = mapperMethodConfiguration.getWhere();
        this.operateEnum = mapperMethodConfiguration.getOperateEnum();
        this.returnType = mapperMethodConfiguration.getReturnType();
        this.parameterType = mapperMethodConfiguration.getParameterType();
        this.queryParameters = mapperMethodConfiguration.getQueryParameters();
        this.mapperMethodConfiguration = mapperMethodConfiguration;
    }

    /**
     * 该策略是否需要支持完整的 mapper 文件解析
     * @return 默认 false
     */
    public boolean supportMapperNode() {
        return false;
    }

    /**
     * 获取 mapper 标签节点类型
     * @return /insert /update /select /delete
     */
    public abstract String getMapperNodeType();

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
        if (CommonUtil.empty(this.xml)) {
            this.xml = this.generateMapperLabel();
        }
        return this.xml;
    }

    /**
     * 重置生成的 mapper 标签
     */
    public void resetMapperXml() {
        this.xml = null;
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
     * 包装主键
     * @return 包装后的方法名称
     */
    protected String wrapPrimaryKeyIfNecessary(String conditionString) {
        if(conditionString == null || !conditionString.contains("Pk")) {
            return conditionString;
        }
        if(!mapperMethodConfiguration.getMapperInterface().equals(BaseMapper.class)) {
            return conditionString;
        }
        String pkCondition = String.join("And", mapperMethodConfiguration.getPrimaryKey());
        return conditionString.replace("Pk", pkCondition);
    }

    /**
     * 包装列
     * @return 包装后的列
     */
    protected Column wrapColumnIfNecessary(final String fieldName, final Field field) {
        if(field.isAnnotationPresent(Column.class)) {
            return field.getAnnotation(Column.class);
        }
        return new Column() {
            @Override
            public String value() {
                return CommonUtil.camelCase2Underline(fieldName);
            }

            @Override
            public JdbcType jdbcType() {
                return JdbcType.OTHER;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return Column.class;
            }
        };
    }

    /**
     * 包装属性名称
     * @return 包装后的属性名称
     */
    protected String wrapFieldNameIfNecessary(String fieldName, Column annotation) {
        if(annotation != null && annotation.jdbcType() != JdbcType.OTHER) {
            fieldName += ", jdbcType=" + annotation.jdbcType().name();
        }
        return fieldName;
    }

    /**
     * 生成条件
     * @return 条件 sql
     */
    protected String buildCondition() {
        return this.buildCondition(this.wrapPrimaryKeyIfNecessary(mapperMethodConfiguration.getMatchName(operateEnum)));
    }

    /**
     * 根据条件字符串生成条件
     * @param conditionString 条件字符串
     * @return 条件 sql
     */
    protected String buildCondition(String conditionString) {
        Objects.requireNonNull(conditionString, "Build SQL condition error: condition is null !");
        StringBuilder builder = new StringBuilder();
        Matcher matcher = Pattern.compile("OrderBy|And|Or").matcher(conditionString);
        List<String> conditions = CommonUtil.split(conditionString, "OrderBy|And|Or");
        for (int i = 0; i < conditions.size(); i++) {
            if(conditions.get(i).contains("Asc") || conditions.get(i).contains("Desc")) {
                if(matcher.find()) {
                    throw new IllegalArgumentException("Build SQL error: order by must be last statement !");
                }
                return builder.append(this.where).append(this.buildSortCondition(conditions.get(i))).toString();
            }
            SQLConditionEnum conditionEnum = SQLConditionEnum.matchSQLCondition(conditions.get(i));
            String column = Arrays.stream(conditions.get(i).split(conditionEnum.condition())).filter(e -> !e.isEmpty()).findFirst().get();
            builder.append(CommonUtil.camelCase2Underline(column));
            this.buildBranchCondition(i, queryParameters, conditionEnum, builder);
            if(matcher.find()) {
                if(!"OrderBy".equalsIgnoreCase(matcher.group())) {
                    builder.append(SQLConditionEnum.matchSQLCondition(matcher.group()).separate());
                }
            }
        }
        return builder.append(this.where).toString();
    }

    /**
     * 生成排序条件
     */
    protected String buildSortCondition(String sortCondition) {
        Objects.requireNonNull(sortCondition, "Build SQL sort error: sort condition is null !");
        StringBuilder builder = new StringBuilder();
        sortCondition = sortCondition.replaceFirst("OrderBy", "");
        List<String> conditions = CommonUtil.split(sortCondition, "Asc|Desc");
        builder.append(" order by ");
        for (int i = 0; i < conditions.size(); i++) {
            sortCondition = sortCondition.replaceFirst(conditions.get(i), "");
            SQLConditionEnum conditionEnum = sortCondition.startsWith(SQLConditionEnum.CONDITION_OrderByAsc.condition()) ? SQLConditionEnum.CONDITION_OrderByAsc : SQLConditionEnum.CONDITION_OrderByDesc;
            builder.append(String.format(conditionEnum.template(), CommonUtil.camelCase2Underline(conditions.get(i))));
            if(i != conditions.size() - 1) {
                builder.append(", ");
            }
            sortCondition = sortCondition.replaceFirst(conditionEnum.condition(), "");
        }
        return builder.toString();
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
        return String.format(conditionEnum.template(), builder);
    }
}
