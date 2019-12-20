package com.kfyty.mybatis.auto.mapper.handle.strategy;

import com.kfyty.mybatis.auto.mapper.configure.MapperMethodConfiguration;
import com.kfyty.mybatis.auto.mapper.match.SQLOperateEnum;
import com.kfyty.mybatis.auto.mapper.utils.CommonUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * 功能描述: 生成 <update/> 标签策略
 *
 * @author kfyty725@hotmail.com
 * @date 2019/12/16 18:19
 * @since JDK 1.8
 */
public class UpdateMapperLabelStrategy extends AbstractGenerateMapperLabel {

    public UpdateMapperLabelStrategy(MapperMethodConfiguration mapperMethodConfiguration) {
        super(mapperMethodConfiguration);
    }

    @Override
    public String generateMapperLabel() {
        if(operateEnum.equals(SQLOperateEnum.OPERATE_UPDATE)) {
            this.operateUpdate();
        }
        if(operateEnum.equals(SQLOperateEnum.OPERATE_UPDATE_ALL)) {
            this.operateUpdateAll();
        }
        if(operateEnum.equals(SQLOperateEnum.OPERATE_UPDATE_BY)) {
            this.operateUpdateBy();
        }
        return this.xml;
    }

    private void operateUpdate() {
        String entity = queryParameters.get(0);
        this.xml = String.format(this.getMapperXmlTemplate(), parameterType.getName(), this.table, this.buildUpdateStatement(entity), this.buildUpdatePrimaryKeyCondition(entity) + this.where);
    }

    private void operateUpdateAll() {
        this.xml = String.format(this.getMapperXmlTemplate(), parameterType.getName(), queryParameters.get(0), this.table, this.buildUpdateAllStatement(), this.buildUpdatePrimaryKeyCondition("item") + this.where);
    }

    private void operateUpdateBy() {
        String[] condition = this.buildUpdateCondition();
        this.xml = String.format(this.getMapperXmlTemplate(), this.table, condition[0], condition[1]);
    }

    private String buildUpdatePrimaryKeyCondition(String entity) {
        StringBuilder builder = new StringBuilder();
        String[] primaryKeys = mapperMethodConfiguration.getPrimaryKey();
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
        Map<String, Field> fieldMap = CommonUtil.getFieldMap(parameterType);
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
        List<String> updateConditions = CommonUtil.split(mapperMethodConfiguration.getMatchName(operateEnum), "Set");
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
}
