package com.kfyty.mybatis.auto.mapper;

import com.kfyty.mybatis.auto.mapper.annotation.AutoMapper;
import com.kfyty.mybatis.auto.mapper.struct.TableFieldStruct;
import com.kfyty.mybatis.auto.mapper.struct.TableStruct;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface BaseMapper<PrimaryKey, T> {
    @AutoMapper
    int insert(@Param("entity") T entity);

    @AutoMapper
    int insertAll(@Param("entities") List<T> entities);

    @AutoMapper
    int update(@Param("entity") T entity);

    @AutoMapper(allowNull = true)
    int updateDeep(@Param("entity") T entity);

    @AutoMapper
    int updateAll(@Param("entities") List<T> entities);

    @AutoMapper(allowNull = true)
    int updateAllDeep(@Param("entities") List<T> entities);

    @AutoMapper
    List<T> findAll();

    @AutoMapper
    int countAll();

    @AutoMapper
    List<T> pageAll(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    @AutoMapper
    int deleteAll();

    @AutoMapper
    T findByPk(@Param("pk") PrimaryKey pk);

    @AutoMapper(parseColumn = false)
    Map<String, Object> findMapByPk(@Param("pk") PrimaryKey pk);

    @AutoMapper
    int deleteByPk(@Param("pk") PrimaryKey pk);

    @AutoMapper
    TableStruct findTableStruct(@Param("database") String database);

    @AutoMapper
    List<TableFieldStruct> findTableFieldStruct(@Param("database") String database);
}
