package com.kfyty.mybatis.auto.mapper.handle.strategy;

import com.kfyty.mybatis.auto.mapper.struct.DataBaseStructSQL;
import com.kfyty.support.utils.CommonUtil;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
public class TableStructMapperLabelStrategy extends AbstractGenerateMapperLabel {
    private static final String TABLE_STRUCT_TEMPLATE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
            "<mapper namespace=\"%s\">\n" +
            "    <resultMap id=\"%s\" type=\"com.kfyty.mybatis.auto.mapper.struct.TableStruct\">\n" +
            "        <id property=\"tableName\" column=\"TABLE_NAME\" />\n" +
            "        <result property=\"databaseName\" column=\"DATABASE_NAME\" />\n" +
            "        <result property=\"tableComment\" column=\"TABLE_COMMENT\" />\n" +
            "        <collection property=\"fieldStruct\" select=\"%s.findTableFieldStruct\" column=\"DATABASE_NAME\" javaType=\"java.util.List\" ofType=\"com.kfyty.mybatis.auto.mapper.struct.TableFieldStruct\" />\n" +
            "    </resultMap> \n" +
            "    %s \n" +
            "</mapper>\n";

    @Override
    public boolean supportMapperNode() {
        return true;
    }

    @Override
    public String getMapperNodeType() {
        return "/mapper";
    }

    @Override
    public String generateMapperLabel() {
        String sql = DataBaseStructSQL.TABLE_STRUCT_SQL.get(mapperMethodConfiguration.getDatabase());
        if(CommonUtil.empty(sql)) {
            throw new RuntimeException("Load table struct not support of database: " + mapperMethodConfiguration.getDatabase() + ", please use DataBaseStructSQL.TABLE_STRUCT_SQL.put(database, SQL) and try again !");
        }
        String namespace = mapperMethodConfiguration.getChildInterface().getName();
        String resultMapId = System.nanoTime() + UUID.randomUUID().toString().replace("-", "");
        this.xml = String.format(this.getMapperXmlTemplate(), resultMapId, sql.replace("#{table}", "'" + table + "'"));
        this.xml = String.format(TABLE_STRUCT_TEMPLATE, namespace, resultMapId, namespace, xml);
        return this.xml;
    }
}
