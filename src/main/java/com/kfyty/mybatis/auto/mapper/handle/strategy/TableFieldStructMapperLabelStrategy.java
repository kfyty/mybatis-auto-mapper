package com.kfyty.mybatis.auto.mapper.handle.strategy;

import com.kfyty.mybatis.auto.mapper.struct.DataBaseStructSQL;
import com.kfyty.core.utils.CommonUtil;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TableFieldStructMapperLabelStrategy extends AbstractGenerateMapperLabel {
    @Override
    public String getMapperNodeType() {
        return "/select";
    }

    @Override
    public String generateMapperLabel() {
        String sql = DataBaseStructSQL.TABLE_FIELD_STRUCT_SQL.get(mapperMethodConfiguration.getDatabase());
        if(CommonUtil.empty(sql)) {
            throw new RuntimeException("Load table field struct not support of database: " + mapperMethodConfiguration.getDatabase() + ", please use DataBaseStructSQL.TABLE_FIELD_STRUCT_SQL.put(database, SQL) and try again !");
        }
        this.xml = String.format(this.getMapperXmlTemplate(), sql.replace("#{table}", "'" + table + "'"));
        return this.xml;
    }
}
