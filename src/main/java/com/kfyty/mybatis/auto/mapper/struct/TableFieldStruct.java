package com.kfyty.mybatis.auto.mapper.struct;

import lombok.Data;

/**
 * 功能描述: 表字段结构
 *
 * @author kfyty725@hotmail.com
 * @date 2020/08/19 19:00
 * @since JDK 1.8
 */
@Data
public class TableFieldStruct {
    private String tableName;
    private String fieldName;
    private String fieldType;
    private String primaryKey;
    private String nullable;
    private String fieldComment;

    public boolean primaryKey() {
        return Boolean.parseBoolean(primaryKey);
    }

    public boolean nullable() {
        return Boolean.parseBoolean(nullable);
    }
}
