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

2、在 mapper 接口方法上添加 @AutoMapper 注解。
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
    * 等价于：select id, name from entity where name like '${name}%' order by id asc
    */
    @MapKey("id")
    @AutoMapper(extend = false)
    Map<String, Entity> findIdAndNameByNameRightLikeOrderByIdAsc(@Param("name") String name);
}
```

3、打开 debug 信息
在 application.yml 中添加如下配置：
```yml
logging:
  level:
    com.kfyty.mybatis.auto.mapper.handle: debug
```

更多编写实例请参考 MybatisAutoMapperTest.java 文件。
