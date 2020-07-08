package com.kfyty.mybatis.auto.mapper;

import com.kfyty.mybatis.auto.mapper.annotation.AutoMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
    T findById(@Param("id") PrimaryKey id);
}
