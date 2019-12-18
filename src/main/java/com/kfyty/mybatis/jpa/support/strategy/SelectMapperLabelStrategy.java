package com.kfyty.mybatis.jpa.support.strategy;

import com.kfyty.mybatis.jpa.support.configure.MapperMethodConfiguration;

/**
 * 功能描述: 生成 <select/> 标签策略
 *
 * @author kfyty725@hotmail.com
 * @date 2019/12/16 18:19
 * @since JDK 1.8
 */
public class SelectMapperLabelStrategy extends GenerateMapperLabel {

    public SelectMapperLabelStrategy(MapperMethodConfiguration mapperMethodConfiguration) {
        super(mapperMethodConfiguration);
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
        this.xml = String.format(this.getMapperXmlTemplate(), returnType.getName(), mapperMethodConfiguration.getColumns(), this.table);
    }

    private void operateCountBy() {
        String columns = mapperMethodConfiguration.getColumns().equals("*") ? "count(*)" : mapperMethodConfiguration.getColumns();
        this.xml = String.format(this.getMapperXmlTemplate(), returnType.getName(), columns, this.table, this.buildCondition());
    }

    private void operateCountAll() {
        String columns = mapperMethodConfiguration.getColumns().equals("*") ? "count(*)" : mapperMethodConfiguration.getColumns();
        this.xml = String.format(this.getMapperXmlTemplate(), returnType.getName(), columns, this.table);
    }
}
