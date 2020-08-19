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
    private AbstractGenerateMapperLabel generateMapperLabel;
    private MapperMethodConfiguration mapperMethodConfiguration;

    public MapperHandler(MapperMethodConfiguration mapperMethodConfiguration) {
        this.setMapperMethodConfiguration(mapperMethodConfiguration);
    }

    public MapperHandler setMapperMethodConfiguration(MapperMethodConfiguration mapperMethodConfiguration) {
        this.operateEnum = mapperMethodConfiguration.getOperateEnum();
        this.mapperMethodConfiguration = mapperMethodConfiguration;
        return this;
    }

    public MapperHandler parse() {
        this.generateMapperLabel = this.operateEnum.strategy();
        this.generateMapperLabel.initMapperMethodConfiguration(mapperMethodConfiguration);
        this.generateMapperLabel.generateMapperLabel();
        return this;
    }

    public String getMapperNodeType() {
        return this.generateMapperLabel.getMapperNodeType();
    }

    public String getMapperXml() {
        return this.generateMapperLabel.getMapperLabel();
    }
}
