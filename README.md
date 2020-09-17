# mybatis-auto-mapper
    一个为 mybatis 编写的非运行时扩展包，提供根据 mapper 接口方法名自动映射查询的功能！

## 简介
本项目是为 mybatis 编写的一个运行在 springboot 环境下的扩展包。实现了根据 mapper 接口方法名自动映射单表的增删改查的功能。

## 示例

1、在项目中添加如下依赖。
```xml
<dependency>
    <groupId>com.kfyty</groupId>
    <artifactId>mybatis-auto-mapper</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

2、在 mapper 接口方法上添加 @AutoMapper 注解（类注解用于类级别的配置，可省略）。
```java
@AutoMapper(where = "delete = true")
public interface EntityMapper {
    /**
     * 等价于：insert into entity(field1 ... fieldN) values (#{entity.field1} ... #{entity.fieldN})
     * SelectKey 注解用于返回主键，默认提供 MySQL 自增主键实现，批量插入不支持返回主键。
     */
    @SelectKey
    @AutoMapper(useDefault = true, allowNull = true)
    int insertEntity(@Param("entity") Entity entity);
    
    /**
     * 等价于：select * from entity where id = #{id} and delete = true
     */
    @AutoMapper
    Entity findById(@Param("id") Integer id);
    
    /**
     * 等价于：select count(*) from entity where id between #{id1} and #{id2} and delete = true
     */
    @AutoMapper
    int countByIdBetween(@Param("id1") Integer id1, @Param("id2") Integer id2);

    /**
     * 等价于：update entity set status = #{status} where id = #{id} and delete = true
     */
    @AutoMapper
    int updateByIdSetStatus(@Param("id") Integer id, @Param("status") Integer status);
    
    /**
     * 等价于：select id, name from entity where name like '${name}%' order by id asc
     */
    @MapKey("id")
    @AutoMapper(extend = false)
    Map<String, Entity> findIdAndNameByNameRightLikeOrderByIdAsc(@Param("name") String name);
}
```

3、提供预设接口，以提供基础的增删改查功能，以及查询当前实体所对应的表结构功能：
```java
/**
 * 预设接口，提供基础功能
 * 继承此接口的 mapper，需添加 @AutoMapper(entity = Entity.class)
 * @param <PrimaryKey> 实体主键类型
 * @param <T> 实体泛型
 */
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
```

4、打开 debug 信息
在 application.yml 中添加如下配置：
```yml
logging:
  level:
    com.kfyty.mybatis.auto.mapper.handle: debug
```

更多编写实例请参考 MybatisAutoMapperTest.java 文件。
