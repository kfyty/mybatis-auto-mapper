package com.kfyty.mybatis.auto.mapper.struct;

import java.util.HashMap;
import java.util.Map;

public interface DataBaseStructSQL {
    Map<String, String> TABLE_STRUCT_SQL = new HashMap<String, String>() {{
        put("mysql", "select table_schema database_name, table_name, table_comment from information_schema.tables where table_schema = #{database} and table_name = #{table}");

        put("oracle", "select OWNER DATABASE_NAME, TABLE_NAME, COMMENTS TABLE_COMMENT from all_tab_comments where OWNER = #{database} AND TABLE_NAME = upper(#{table})");

        put("microsoft sql server",
                "SELECT DISTINCT\n" +
                "   i.table_schema database_name,\n" +
                "   t.name table_name,\n" +
                "   e.value table_comment \n" +
                "FROM\n" +
                "   information_schema.columns i\n" +
                "   join sys.tables t ON i.table_name = t.name\n" +
                "   left join sys.extended_properties e ON e.major_id = t.object_id AND e.minor_id = 0 \n" +
                "WHERE\n" +
                "   i.table_schema = #{database} \n" +
                "   and t.name = #{table}");
    }};

    Map<String, String> TABLE_FIELD_STRUCT_SQL = new HashMap<String, String>() {{
        put("mysql",
                "SELECT\n" +
                "   table_name,\n" +
                "   column_name field_name,\n" +
                "   data_type field_type,\n" +
                "   IF (column_key = 'PRI', 'true', 'false') primary_key,\n" +
                "   IF (is_nullable = 'YES', 'true', 'false') nullable,\n" +
                "   column_comment field_comment \n" +
                "FROM\n" +
                "   information_schema.COLUMNS \n" +
                "WHERE\n" +
                "   table_schema = #{database} \n" +
                "   and table_name = #{table}");

        put("oracle",
                "SELECT\n" +
                "   t.table_name,\n" +
                "   t.column_name field_name,\n" +
                "   t.data_type field_type,\n" +
                "   (SELECT decode(count(a.constraint_name), 0, 'false', 1, 'true') FROM user_cons_columns a JOIN user_constraints b ON a.constraint_name = b.constraint_name WHERE b.constraint_type = 'P' AND a.table_name = t.table_name and a.column_name = t.column_name) primary_key,\n" +
                "   decode(nullable, 'N', 'false', 'Y', 'true') nullable,\n" +
                "   c.comments field_comment \n" +
                "FROM\n" +
                "   user_tab_columns t\n" +
                "   JOIN user_col_comments c ON t.table_name = c.table_name and t.column_name = c.column_name \n" +
                "WHERE\n" +
                "   t.table_name = upper(#{table})");

        put("microsoft sql server",
                "SELECT DISTINCT\n" +
                "   t.name table_name,\n" +
                "   c.name field_name,\n" +
                "   ty.name field_type,\n" +
                "   (SELECT CASE WHEN sik.id IS NOT NULL THEN 'true' ELSE 'false' END FROM SYSCOLUMNS sc LEFT JOIN SYSOBJECTS so1 ON so1.parent_obj = sc.id AND so1.xtype = 'PK' LEFT JOIN SYSINDEXKEYS sik ON sc.colid = sik.colid AND sik.id = sc.id LEFT JOIN SYSOBJECTS so2 ON so2.id= sc.id WHERE so2.name = t.name AND sc.name = c.name) primary_key,\n" +
                "   CASE c.is_nullable WHEN 0 THEN 'false' ELSE 'true' END nullable,\n" +
                "   e.value field_comment \n" +
                "FROM\n" +
                "   information_schema.columns i\n" +
                "   join sys.tables t ON t.name = i.table_name\n" +
                "   join sys.columns c ON c.object_id = t.object_id\n" +
                "   join sys.types ty ON ty.user_type_id = c.user_type_id\n" +
                "   left join sys.extended_properties e ON e.major_id = c.object_id and e.minor_id = c.column_id \n" +
                "WHERE\n" +
                "   i.table_schema = #{database}\n" +
                "   and i.table_name = #{table}");
    }};
}
