package com.kfyty.mybatis.auto.mapper.struct;

import lombok.Data;

import java.util.List;

/**
 * 功能描述: 表结构
 *
 * @author kfyty725@hotmail.com
 * @date 2020/08/19 19:00
 * @since JDK 1.8
 */
@Data
public class TableStruct {
    private String databaseName;
    private String tableName;
    private String tableComment;
    private List<TableFieldStruct> fieldStruct;
}
