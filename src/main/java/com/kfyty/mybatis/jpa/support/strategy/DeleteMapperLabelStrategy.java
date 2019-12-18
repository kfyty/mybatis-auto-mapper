package com.kfyty.mybatis.jpa.support.strategy;

import com.kfyty.mybatis.jpa.support.configure.MapperMethodConfiguration;
import com.kfyty.mybatis.jpa.support.match.SQLOperateEnum;

/**
 * 功能描述: 生成 <delete/> 标签策略
 *
 * @author kfyty725@hotmail.com
 * @date 2019/12/16 18:19
 * @since JDK 1.8
 */
public class DeleteMapperLabelStrategy extends GenerateMapperLabel {

    public DeleteMapperLabelStrategy(MapperMethodConfiguration mapperMethodConfiguration) {
        super(mapperMethodConfiguration);
    }

    @Override
    public String generateMapperLabel() {
        if(operateEnum.equals(SQLOperateEnum.OPERATE_DELETE_BY)) {
            this.operateDeleteBy();
        }
        if(operateEnum.equals(SQLOperateEnum.OPERATE_DELETE_ALL)) {
            this.operateDeleteAll();
        }
        return this.xml;
    }

    private void operateDeleteBy() {
        this.xml = String.format(this.getMapperXmlTemplate(), this.table, this.buildCondition());
    }

    private void operateDeleteAll() {
        this.xml = String.format(this.getMapperXmlTemplate(), this.table);
    }
}
