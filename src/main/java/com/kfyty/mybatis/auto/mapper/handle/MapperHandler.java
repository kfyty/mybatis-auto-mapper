package com.kfyty.mybatis.auto.mapper.handle;

import com.kfyty.mybatis.auto.mapper.configure.MapperMethodConfiguration;
import com.kfyty.mybatis.auto.mapper.match.SQLOperateEnum;
import com.kfyty.mybatis.auto.mapper.handle.strategy.DeleteMapperLabelStrategy;
import com.kfyty.mybatis.auto.mapper.handle.strategy.AbstractGenerateMapperLabel;
import com.kfyty.mybatis.auto.mapper.handle.strategy.InsertMapperLabelStrategy;
import com.kfyty.mybatis.auto.mapper.handle.strategy.SelectMapperLabelStrategy;
import com.kfyty.mybatis.auto.mapper.handle.strategy.UpdateMapperLabelStrategy;

/**
 * 功能描述: Mapper 处理器，得到 Mapper 标签
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/6 18:19
 * @since JDK 1.8
 */
public class MapperHandler {
    private SQLOperateEnum operateEnum;
    private AbstractGenerateMapperLabel generateMapperLabel;
    private MapperMethodConfiguration mapperMethodConfiguration;

    public MapperHandler(MapperMethodConfiguration mapperMethodConfiguration) {
        this.operateEnum = mapperMethodConfiguration.getOperateEnum();
        this.mapperMethodConfiguration = mapperMethodConfiguration;
    }

    public MapperHandler parse() {
        this.determineStrategy();
        this.generateMapperLabel.generateMapperLabel();
        return this;
    }

    public String getMapperNodeType() {
        return this.generateMapperLabel.getMapperNodeType();
    }

    public String getMapperXml() {
        return this.generateMapperLabel.getMapperLabel();
    }

    private void determineStrategy() {
        if(operateEnum.name().contains("INSERT")) {
            this.generateMapperLabel = new InsertMapperLabelStrategy(mapperMethodConfiguration);
            return ;
        }
        if(operateEnum.name().contains("UPDATE")) {
            this.generateMapperLabel = new UpdateMapperLabelStrategy(mapperMethodConfiguration);
            return ;
        }
        if(operateEnum.name().contains("DELETE")) {
            this.generateMapperLabel = new DeleteMapperLabelStrategy(mapperMethodConfiguration);
            return ;
        }
        if(operateEnum.name().contains("SELECT") || operateEnum.name().contains("PAGE") || operateEnum.name().contains("COUNT")) {
            this.generateMapperLabel = new SelectMapperLabelStrategy(mapperMethodConfiguration);
            return ;
        }
        throw new IllegalArgumentException("Build sql error: match mapper generate strategy failed !");
    }
}
