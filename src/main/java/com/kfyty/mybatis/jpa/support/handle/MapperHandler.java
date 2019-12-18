package com.kfyty.mybatis.jpa.support.handle;

import com.kfyty.mybatis.jpa.support.configure.MapperMethodConfiguration;
import com.kfyty.mybatis.jpa.support.match.SQLOperateEnum;
import com.kfyty.mybatis.jpa.support.strategy.DeleteMapperLabelStrategy;
import com.kfyty.mybatis.jpa.support.strategy.GenerateMapperLabel;
import com.kfyty.mybatis.jpa.support.strategy.InsertMapperLabelStrategy;
import com.kfyty.mybatis.jpa.support.strategy.SelectMapperLabelStrategy;
import com.kfyty.mybatis.jpa.support.strategy.UpdateMapperLabelStrategy;
import lombok.Getter;

/**
 * 功能描述: Mapper 处理器，得到 Mapper 标签
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/6 18:19
 * @since JDK 1.8
 */
public class MapperHandler {
    @Getter
    private String mapperXmlLabel;
    private SQLOperateEnum operateEnum;
    private GenerateMapperLabel generateMapperLabel;
    private MapperMethodConfiguration mapperMethodConfiguration;

    public MapperHandler(MapperMethodConfiguration mapperMethodConfiguration) {
        this.operateEnum = mapperMethodConfiguration.getOperateEnum();
        this.mapperMethodConfiguration = mapperMethodConfiguration;
    }

    public MapperHandler parse() {
        this.determine();
        this.generateMapperLabel.generateMapperLabel();
        return this;
    }

    public String getMapperXml() {
        return this.generateMapperLabel.getMapperLabel();
    }

    private void determine() {
        if(operateEnum.name().contains("INSERT")) {
            this.mapperXmlLabel = "/insert";
            this.generateMapperLabel = new InsertMapperLabelStrategy(mapperMethodConfiguration);
            return ;
        }
        if(operateEnum.name().contains("UPDATE")) {
            this.mapperXmlLabel = "/update";
            this.generateMapperLabel = new UpdateMapperLabelStrategy(mapperMethodConfiguration);
            return ;
        }
        if(operateEnum.name().contains("DELETE")) {
            this.mapperXmlLabel = "/delete";
            this.generateMapperLabel = new DeleteMapperLabelStrategy(mapperMethodConfiguration);
            return ;
        }
        if(operateEnum.name().contains("SELECT") || operateEnum.name().contains("PAGE") || operateEnum.name().contains("COUNT")) {
            this.mapperXmlLabel = "/select";
            this.generateMapperLabel = new SelectMapperLabelStrategy(mapperMethodConfiguration);
            return ;
        }
        throw new IllegalArgumentException("build sql error: match mapper label failed !");
    }
}
