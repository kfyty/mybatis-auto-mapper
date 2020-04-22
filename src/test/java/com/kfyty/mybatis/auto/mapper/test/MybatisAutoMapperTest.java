package com.kfyty.mybatis.auto.mapper.test;

import com.kfyty.mybatis.auto.mapper.annotation.AutoMapper;
import com.kfyty.mybatis.auto.mapper.annotation.Pageable;
import com.kfyty.mybatis.auto.mapper.annotation.SelectKey;
import com.kfyty.mybatis.auto.mapper.annotation.Transient;
import com.kfyty.mybatis.auto.mapper.configure.MapperMethodConfiguration;
import com.kfyty.mybatis.auto.mapper.handle.MapperHandler;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 功能描述: 方法编写实例及测试
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/13 19:02
 * @since JDK 1.8
 */
@AutoMapper(where = "DELETE = 0")
public class MybatisAutoMapperTest {
    private Integer id;
    private String name;
    private Date createTime;

    @Transient
    private Object transients;

    private Method getMethod(String name, Class<?> ... classes) {
        try {
            return MybatisAutoMapperTest.class.getDeclaredMethod(name, classes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 需要解析的方法需要添加 @AutoMapper 注解
     * 需要获取主键需要添加 @SelectKey 注解，默认提供 MySQL 实现
     * @param obj
     * @return
     */
    @SelectKey
    @AutoMapper
    public int insertMybatisAutoMapperTest(@Param("obj") MybatisAutoMapperTest obj) {
        return 0;
    }

    @AutoMapper(useDefault = true)
    public int insertAllMybatisAutoMapperTest(@Param("objs") List<MybatisAutoMapperTest> objs) {
        return 0;
    }

    @AutoMapper(where = "PARENT_ID = 0", separator = "or", allowNull = true)
    public int updateMybatisAutoMapperTest(@Param("obj") MybatisAutoMapperTest obj) {
        return 0;
    }

    /**
     * primaryKey: 更新时指定主键属性，默认为 id
     * extend: 设为 false 时，类注解 where() 失效
     * @param objs
     * @return
     */
    @AutoMapper(primaryKey = {"id", "code"}, extend = false, useDefault = true)
    public int updateAllMybatisAutoMapperTest(@Param("objs") List<MybatisAutoMapperTest> objs) {
        return 0;
    }

    /**
     * updateBy 中必须包含 Set, 且用 And 分隔 Set 之后的字段
     * @param code
     * @param name
     * @param createTime
     * @param updateTime
     * @return
     */
    @AutoMapper
    public int updateByCodeAndNameSetAgeAndCreateTimeAndUpdateTime(@Param("code") String code, @Param("name") String name, @Param("age") Integer age, @Param("createTime") Date createTime, @Param("updateTime") Date updateTime) {
        return 0;
    }

    @AutoMapper
    public MybatisAutoMapperTest findById(@Param("id") Integer id) {
        return null;
    }

    /**
     * 支持 find*By** 风格
     * @param id
     * @return
     */
    @AutoMapper
    public MybatisAutoMapperTest findNameById(@Param("id") Integer id) {
        return null;
    }

    @AutoMapper
    public MybatisAutoMapperTest countNameAndCreateTimeById(@Param("id") Integer id) {
        return null;
    }

    @AutoMapper
    public MybatisAutoMapperTest findByIdLessThanOrCreateTimeLessEqual(@Param("id") Integer id, @Param("createTime") Date createTime) {
        return null;
    }

    @AutoMapper
    public MybatisAutoMapperTest findByIdEqualAndNameNotNull(@Param("id") Integer id, @Param("name") String name) {
        return null;
    }

    @AutoMapper
    public MybatisAutoMapperTest findByIdEqualAndNameNotNullOrCreateTimeBetween(@Param("id") Integer id, String name, @Param("start") Date start, @Param("end") Date end) {
        return null;
    }

    /**
     * OrderBy 之前没有 And/Or，切必须是最后一项
     * @param sid
     * @param eid
     * @param ids
     * @return
     */
    @AutoMapper
    public MybatisAutoMapperTest findByIdBetweenOrIdInOrderByNameAscCreateTimeDesc(@Param("sid") Integer sid, @Param("eid") Integer eid, @Param("ids")List<Integer> ids) {
        return null;
    }

    /**
     * columns: 自定义查询列
     * @return
     */
    @AutoMapper(columns = "distinct name")
    public List<String> findAllName() {
        return null;
    }

    /**
     * 根据 name 排序
     * @return
     */
    @AutoMapper
    public List<String> findAllOrderByNameAsc() {
        return null;
    }

    /**
     * table: 当返回值/方法参数/Mapper接口无法解析出表名时，需以此指定
     * @return
     */
    @AutoMapper(columns = "distinct name", table = "mybatis_auto_mapper_test")
    public List<String> findByNameNotNull() {
        return null;
    }

    /**
     * countBy 开头的方法名可获取数量，用法同 findBy/deleteBy
     * 若需去重可使用 columns
     * @param id
     * @return
     */
    @AutoMapper
    public int countById(@Param("id") Integer id) {
        return 0;
    }

    /**
     * 返回值为 Map 时，须使用 @MapKey 注解指定 key 属性
     * @param id
     * @return
     */
    @AutoMapper
    @MapKey("id")
    public Map<Integer, MybatisAutoMapperTest> findByIdNotIn(@Param("ids") List<Integer> id) {
        return null;
    }

    /**
     * 使用注解 @Pageable 且方法参数最后两参数应依次为 pageNum, pageSize 时进行分页
     * 返回值泛型须存在
     * 若需要返回 PageInfo 需自行转换
     * 若不使用 @Pageable 注解，请阅读 mybatis-helper 使用文档
     * pageBy/pageAll 仅做与 findBy/findAll 区分之用
     * @param sid
     * @param eid
     * @param ids
     * @return
     */
    @Pageable
    @AutoMapper
    public List<MybatisAutoMapperTest> pageByIdBetweenOrIdInOrderByNameAscCreateTimeDesc(@Param("sid") Integer sid, @Param("eid") Integer eid, @Param("ids")List<Integer> ids, int pageNum, int pageSize) {
        return null;
    }

    @Test
    public void test() {
        MapperMethodConfiguration mapperMethodConfiguration = new MapperMethodConfiguration();

        mapperMethodConfiguration.initConfiguration(this.getMethod("insertMybatisAutoMapperTest", MybatisAutoMapperTest.class));
        System.out.println(new MapperHandler(mapperMethodConfiguration).parse().getMapperXml());

        mapperMethodConfiguration.initConfiguration(this.getMethod("insertAllMybatisAutoMapperTest", List.class));
        System.out.println(new MapperHandler(mapperMethodConfiguration).parse().getMapperXml());

        mapperMethodConfiguration.initConfiguration(this.getMethod("updateMybatisAutoMapperTest", MybatisAutoMapperTest.class));
        System.out.println(new MapperHandler(mapperMethodConfiguration).parse().getMapperXml());

        mapperMethodConfiguration.initConfiguration(this.getMethod("updateAllMybatisAutoMapperTest", List.class));
        System.out.println(new MapperHandler(mapperMethodConfiguration).parse().getMapperXml());

        mapperMethodConfiguration.initConfiguration(this.getMethod("updateByCodeAndNameSetAgeAndCreateTimeAndUpdateTime", String.class, String.class, Integer.class, Date.class, Date.class));
        System.out.println(new MapperHandler(mapperMethodConfiguration).parse().getMapperXml());

        mapperMethodConfiguration.initConfiguration(this.getMethod("findById", Integer.class));
        System.out.println(new MapperHandler(mapperMethodConfiguration).parse().getMapperXml());

        mapperMethodConfiguration.initConfiguration(this.getMethod("findNameById", Integer.class));
        System.out.println(new MapperHandler(mapperMethodConfiguration).parse().getMapperXml());

        mapperMethodConfiguration.initConfiguration(this.getMethod("countNameAndCreateTimeById", Integer.class));
        System.out.println(new MapperHandler(mapperMethodConfiguration).parse().getMapperXml());

        mapperMethodConfiguration.initConfiguration(this.getMethod("findByIdLessThanOrCreateTimeLessEqual", Integer.class, Date.class));
        System.out.println(new MapperHandler(mapperMethodConfiguration).parse().getMapperXml());

        mapperMethodConfiguration.initConfiguration(this.getMethod("findByIdEqualAndNameNotNull", Integer.class, String.class));
        System.out.println(new MapperHandler(mapperMethodConfiguration).parse().getMapperXml());

        mapperMethodConfiguration.initConfiguration(this.getMethod("findByIdEqualAndNameNotNullOrCreateTimeBetween", Integer.class, String.class, Date.class, Date.class));
        System.out.println(new MapperHandler(mapperMethodConfiguration).parse().getMapperXml());

        mapperMethodConfiguration.initConfiguration(this.getMethod("findByIdBetweenOrIdInOrderByNameAscCreateTimeDesc", Integer.class, Integer.class, List.class));
        System.out.println(new MapperHandler(mapperMethodConfiguration).parse().getMapperXml());

        mapperMethodConfiguration.initConfiguration(this.getMethod("findAllName"));
        System.out.println(new MapperHandler(mapperMethodConfiguration).parse().getMapperXml());

        mapperMethodConfiguration.initConfiguration(this.getMethod("findAllOrderByNameAsc"));
        System.out.println(new MapperHandler(mapperMethodConfiguration).parse().getMapperXml());

        mapperMethodConfiguration.initConfiguration(this.getMethod("findByNameNotNull"));
        System.out.println(new MapperHandler(mapperMethodConfiguration).parse().getMapperXml());

        mapperMethodConfiguration.initConfiguration(this.getMethod("findByIdNotIn", List.class));
        System.out.println(new MapperHandler(mapperMethodConfiguration).parse().getMapperXml());

        mapperMethodConfiguration.initConfiguration(this.getMethod("countById", Integer.class));
        System.out.println(new MapperHandler(mapperMethodConfiguration).parse().getMapperXml());

        mapperMethodConfiguration.initConfiguration(this.getMethod("pageByIdBetweenOrIdInOrderByNameAscCreateTimeDesc", Integer.class, Integer.class, List.class, int.class, int.class));
        System.out.println(new MapperHandler(mapperMethodConfiguration).parse().getMapperXml());
    }
}
