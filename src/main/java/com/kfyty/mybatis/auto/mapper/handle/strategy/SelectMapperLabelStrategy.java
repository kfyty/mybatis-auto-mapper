package com.kfyty.mybatis.auto.mapper.handle.strategy;

import com.kfyty.mybatis.auto.mapper.utils.CommonUtil;
import lombok.NoArgsConstructor;

/**
 * 功能描述: 生成 <select/> 标签策略
 *
 * @author kfyty725@hotmail.com
 * @date 2019/12/16 18:19
 * @since JDK 1.8
 */
@NoArgsConstructor
public class SelectMapperLabelStrategy extends AbstractGenerateMapperLabel {

    @Override
    public String getMapperNodeType() {
        return "/select";
    }

    @Override
    public String generateMapperLabel() {
        switch (operateEnum) {
            case OPERATE_PAGE_BY:
            case OPERATE_SELECT_BY:
                this.operateSelectBy();
                break;
            case OPERATE_PAGE_ALL:
            case OPERATE_SELECT_ALL:
                this.operateSelectAll();
                break;
            case OPERATE_COUNT_BY:
                this.operateCountBy();
                break;
            case OPERATE_COUNT_All:
                this.operateCountAll();
                break;
        }
        return this.xml;
    }

    private void operateSelectBy() {
        this.xml = String.format(this.getMapperXmlTemplate(), returnType.getName(), mapperMethodConfiguration.getColumns(), this.table, this.buildCondition());
    }

    private void operateSelectAll() {
        String where = CommonUtil.empty(this.where) ? "" : " where " + this.where;
        String sortCondition = mapperMethodConfiguration.getMatchName(operateEnum);
        sortCondition = CommonUtil.empty(sortCondition) || !sortCondition.startsWith("OrderBy") ? "" : this.buildSortCondition(sortCondition);
        this.xml = String.format(this.getMapperXmlTemplate(), returnType.getName(), mapperMethodConfiguration.getColumns(), this.table + where + sortCondition);
    }

    private void operateCountBy() {
        String columns = mapperMethodConfiguration.getColumns().toLowerCase();
        columns = columns.equals("*") || !columns.contains("count") ? "count(*)" : columns;
        this.xml = String.format(this.getMapperXmlTemplate(), returnType.getName(), columns, this.table, this.buildCondition());
    }

    private void operateCountAll() {
        String where = CommonUtil.empty(this.where) ? "" : " where " + this.where;
        String columns = mapperMethodConfiguration.getColumns().toLowerCase();
        columns = columns.equals("*") || !columns.contains("count") ? "count(*)" : columns;
        this.xml = String.format(this.getMapperXmlTemplate(), returnType.getName(), columns, this.table + where);
    }
}
