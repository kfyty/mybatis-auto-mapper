package com.kfyty.mybatis.auto.mapper.handle.strategy;

import com.kfyty.mybatis.auto.mapper.annotation.SelectKey;
import com.kfyty.mybatis.auto.mapper.configure.MapperMethodConfiguration;
import com.kfyty.mybatis.auto.mapper.match.SQLOperateEnum;
import com.kfyty.mybatis.auto.mapper.utils.CommonUtil;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * 功能描述: 生成 <insert/> 标签策略
 *
 * @author kfyty725@hotmail.com
 * @date 2019/12/16 18:19
 * @since JDK 1.8
 */
public class InsertMapperLabelStrategy extends AbstractGenerateMapperLabel {

    public InsertMapperLabelStrategy(MapperMethodConfiguration mapperMethodConfiguration) {
        super(mapperMethodConfiguration);
    }

    @Override
    public String generateMapperLabel() {
        if(operateEnum.equals(SQLOperateEnum.OPERATE_INSERT)) {
            this.operateInsert();
        }
        if(operateEnum.equals(SQLOperateEnum.OPERATE_INSERT_ALL)) {
            this.operateInsertAll();
        }
        return this.xml;
    }

    private void operateInsert() {
        String[] insertStatement = this.buildInsertStatement(queryParameters.get(0));
        this.xml = String.format(this.getMapperXmlTemplate(), parameterType.getName(), this.buildSelectKey(), this.table, insertStatement[0], insertStatement[1]);
    }

    private void operateInsertAll() {
        String[] insertStatement = this.buildInsertAllStatement();
        this.xml = String.format(this.getMapperXmlTemplate(), parameterType.getName(), this.table, insertStatement[0], queryParameters.get(0), insertStatement[1]);
    }

    private String buildSelectKey() {
        if(mapperMethodConfiguration.getSelectKey() == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        SelectKey selectKey = mapperMethodConfiguration.getSelectKey();
        String primaryKey = mapperMethodConfiguration.getPrimaryKey()[0];
        String primaryKeyType = CommonUtil.getField(mapperMethodConfiguration.getParameterType(), primaryKey).getType().getName();
        builder.append("<selectKey keyProperty=\"").append(primaryKey).append("\" resultType=\"").append(primaryKeyType).append("\" order=\"").append(selectKey.order()).append("\">");
        builder.append(selectKey.value());
        builder.append("</selectKey>");
        return builder.toString();
    }

    private String[] buildInsertStatement(String entity) {
        StringBuilder[] builder = new StringBuilder[] {new StringBuilder(), new StringBuilder()};
        Map<String, Field> fieldMap = CommonUtil.getFieldMap(parameterType);
        builder[1].append(mapperMethodConfiguration.getUseDefault() ? "<trim suffixOverrides=\", \">" : "");
        for (String field : fieldMap.keySet()) {
            builder[0].append(CommonUtil.convert2Underline(field, true)).append(", ");
            if(!mapperMethodConfiguration.getUseDefault()) {
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
        builder[1].append(mapperMethodConfiguration.getUseDefault() ? "</trim>" : "");
        return new String[] {
                builder[0].delete(builder[0].length() - 2, builder[0].length()).toString(),
                mapperMethodConfiguration.getUseDefault() ? builder[1].toString() : builder[1].delete(builder[1].length() - 2, builder[1].length()).toString()
        };
    }

    private String[] buildInsertAllStatement() {
        return buildInsertStatement("item");
    }
}
