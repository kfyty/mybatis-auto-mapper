package com.kfyty.mybatis.auto.mapper.handle;

import com.kfyty.mybatis.auto.mapper.configure.MapperMethodConfiguration;
import com.kfyty.mybatis.auto.mapper.handle.strategy.AbstractGenerateMapperLabel;
import com.kfyty.mybatis.auto.mapper.match.SQLOperateEnum;
import lombok.NoArgsConstructor;

/**
 * 功能描述: Mapper 处理器，得到 Mapper 标签
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/6 18:19
 * @since JDK 1.8
 */
@NoArgsConstructor
public class MapperHandler {
    private SQLOperateEnum operateEnum;
    private AbstractGenerateMapperLabel generateMapperStrategy;
    private MapperMethodConfiguration mapperMethodConfiguration;

    public MapperHandler(MapperMethodConfiguration mapperMethodConfiguration) {
        this.setMapperMethodConfiguration(mapperMethodConfiguration);
    }

    public MapperHandler setMapperMethodConfiguration(MapperMethodConfiguration mapperMethodConfiguration) {
        this.operateEnum = mapperMethodConfiguration.getOperateEnum();
        this.generateMapperStrategy = this.operateEnum.strategy();
        this.mapperMethodConfiguration = mapperMethodConfiguration;
        return this;
    }

    public MapperHandler doResolve() {
        this.generateMapperStrategy.initMapperMethodConfiguration(mapperMethodConfiguration);
        this.generateMapperStrategy.generateMapperLabel();
        return this;
    }

    public String getMapperNodeType() {
        return this.generateMapperStrategy.getMapperNodeType();
    }

    public String getMapperXml() {
        return this.generateMapperStrategy.getMapperLabel();
    }

    public void resetMapperXml() {
        this.generateMapperStrategy.resetMapperXml();
    }

    public boolean supportMapperNode() {
        return this.generateMapperStrategy.supportMapperNode();
    }
}
