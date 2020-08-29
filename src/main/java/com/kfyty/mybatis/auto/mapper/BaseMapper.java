package com.kfyty.mybatis.auto.mapper;

import com.kfyty.mybatis.auto.mapper.annotation.AutoMapper;
import com.kfyty.mybatis.auto.mapper.struct.TableFieldStruct;
import com.kfyty.mybatis.auto.mapper.struct.TableStruct;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface BaseMapper<PrimaryKey, T> {
    /**
     * 插入单条数据
     * @param entity 实体数据
     * @return 受影响的行数
     */
    @AutoMapper
    int insert(@Param("entity") T entity);

    /**
     * 批量插入数据
     * @param entities 实体数据
     * @return 受影响的行数
     */
    @AutoMapper
    int insertAll(@Param("entities") List<T> entities);

    /**
     * 更新单条数据，只更新值不为空的字段
     * @param entity 实体数据
     * @return 受影响的行数
     */
    @AutoMapper
    int update(@Param("entity") T entity);

    /**
     * 更新单条数据，允许更新值为空的字段
     * @param entity 实体数据
     * @return 受影响的行数
     */
    @AutoMapper(allowNull = true)
    int updateDeep(@Param("entity") T entity);

    /**
     * 批量更新数据，只更新值不为空的字段
     * @param entities 实体数据
     * @return 受影响的行数
     */
    @AutoMapper
    int updateAll(@Param("entities") List<T> entities);

    /**
     * 批量更新数据，允许更新值为空的字段
     * @param entities 实体数据
     * @return 受影响的行数
     */
    @AutoMapper(allowNull = true)
    int updateAllDeep(@Param("entities") List<T> entities);

    /**
     * 查询所有的数据
     * @return 所有数据的集合
     */
    @AutoMapper
    List<T> findAll();

    /**
     * 查询记录总条数
     * @return 记录总条数
     */
    @AutoMapper
    int countAll();

    /**
     * 分页查询数据
     * 需要 mybatis-page-helper 分页插件支持，已自动内置，亦可外部手动配置
     * @param pageNum 起始页码
     * @param pageSize 每页大小
     * @return 分页数据
     */
    @AutoMapper
    List<T> pageAll(@Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

    /**
     * 删除所有数据
     * @return 受影响的行数
     */
    @AutoMapper
    int deleteAll();

    /**
     * 根据主键查询数据，支持复合主键
     * @param pk 主键数据
     * @return 实体数据
     */
    @AutoMapper
    T findByPk(@Param("pk") PrimaryKey ... pk);

    /**
     * 根据主键查询数据，支持复合主键
     * @param pk 主键数据
     * @return 直接以 Map 形式返回实体数据
     */
    @AutoMapper(parseColumn = false)
    Map<String, Object> findMapByPk(@Param("pk") PrimaryKey ... pk);

    /**
     * 根据主键删除数据，支持复合主键
     * @param pk 主键数据
     * @return 受影响的行数
     */
    @AutoMapper
    int deleteByPk(@Param("pk") PrimaryKey ... pk);

    /**
     * 查询当前表结构
     * @param database 表所在的数据库名称
     * @return 表结构
     */
    @AutoMapper
    TableStruct findTableStruct(@Param("database") String database);

    /**
     * 查询当前表的字段结构
     * @param database 表所在的数据库名称
     * @return 表字段结构
     */
    @AutoMapper
    List<TableFieldStruct> findTableFieldStruct(@Param("database") String database);
}
