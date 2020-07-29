package com.kfyty.mybatis.auto.mapper.test;

import com.kfyty.mybatis.auto.mapper.BaseMapper;
import com.kfyty.mybatis.auto.mapper.annotation.AutoMapper;
import com.kfyty.mybatis.auto.mapper.annotation.SelectKey;
import com.kfyty.mybatis.auto.mapper.annotation.Transient;
import com.kfyty.mybatis.auto.mapper.configure.MapperMethodConfiguration;
import com.kfyty.mybatis.auto.mapper.handle.MapperHandler;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
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
public class MybatisAutoMapperTest {
    private Integer id;
    private String name;
    private Date createTime;

    @Transient
    private Object transients;

    /**
     * where: 所有的查询/更新均需要拼接的条件
     * entity: 因为该拓展包只在应用启动时起作用，因此继承自 BaseMapper 的接口必须指定实体类型
     */
    @AutoMapper(entity = MybatisAutoMapperTest.class, where = "DELETE = 0")
    private interface AutoMapperTest extends BaseMapper<Integer, MybatisAutoMapperTest> {
        /**
         * 需要解析的方法需要添加 @AutoMapper 注解
         * 需要获取主键需要添加 @SelectKey 注解，默认提供 MySQL 自增实现
         * @param obj obj
         * @return effect
         */
        @SelectKey
        @AutoMapper
        int insertMybatisAutoMapperTest(@Param("obj") MybatisAutoMapperTest obj);

        /**
         * useDefault: 插入数据时，遇到空值转换为插入数据库默认值
         * @param objs objs
         * @return effect
         */
        @AutoMapper(useDefault = true)
        int insertAllMybatisAutoMapperTest(@Param("objs") List<MybatisAutoMapperTest> objs);

        /**
         * where: 额外的更新条件
         * separator: 拼接该 where 条件的分隔符
         * allowNull: 允许字段更新为空值，为 false 时，只更新对象中不为空的字段
         * @param obj obj
         * @return effect
         */
        @AutoMapper(where = "PARENT_ID = 0", separator = "or", allowNull = true)
        int updateMybatisAutoMapperTest(@Param("obj") MybatisAutoMapperTest obj);

        /**
         * primaryKey: 更新时主键属性，默认为 id
         * extend: 设为 false 时，类注解 where() 失效
         * useDefault: 对象中的值为空时，更新为数据库默认值，需数据库支持
         * @param objs objs
         * @return effect
         */
        @AutoMapper(primaryKey = {"id", "code"}, extend = false, useDefault = true)
        int updateAllMybatisAutoMapperTest(@Param("objs") List<MybatisAutoMapperTest> objs);

        /**
         * 支持 updateBy*Set** 风格解析
         * updateBy 中必须包含 Set, 且用 And 分隔 Set 之后的字段
         * @param code code
         * @param name name
         * @param createTime createTime
         * @param updateTime updateTime
         * @return effect
         */
        @AutoMapper
        int updateByCodeAndNameSetAgeAndCreateTimeAndUpdateTime(@Param("code") String code, @Param("name") String name, @Param("age") Integer age, @Param("createTime") Date createTime, @Param("updateTime") Date updateTime);

        /**
         * BaseMapper 中有 findByPk 方法，自动转换为根据主键查询
         * 当主键为 id 时，二者结果相同
         * @param id id
         * @return MybatisAutoMapperTest
         */
        @AutoMapper
        MybatisAutoMapperTest findById(@Param("id") Integer id);

        /**
         * 支持 find*By** 风格
         * @param id id
         * @return name
         */
        @AutoMapper
        List<String> findNameById(@Param("id") Integer id);

        @AutoMapper
        MybatisAutoMapperTest countNameAndCreateTimeById(@Param("id") Integer id);

        @AutoMapper
        MybatisAutoMapperTest findByIdLessThanOrCreateTimeLessEqual(@Param("id") Integer id, @Param("createTime") Date createTime);

        @AutoMapper
        MybatisAutoMapperTest findByIdEqualAndNameNotNull(@Param("id") Integer id, @Param("name") String name);

        @AutoMapper
        MybatisAutoMapperTest findByIdContainsAndNameLeftLikeOrNameRightLike(@Param("id") Integer id, @Param("name") String name, @Param("name") String name2);

        @AutoMapper
        MybatisAutoMapperTest findByIdEqualAndNameNotNullOrCreateTimeBetween(@Param("id") Integer id, String name, @Param("start") Date start, @Param("end") Date end);

        /**
         * OrderBy 之前没有 And/Or，切必须是最后一项
         * @param sid sid
         * @param eid eid
         * @param ids ids
         * @return MybatisAutoMapperTest
         */
        @AutoMapper
        MybatisAutoMapperTest findByIdBetweenOrIdInOrderByNameAscCreateTimeDesc(@Param("sid") Integer sid, @Param("eid") Integer eid, @Param("ids")List<Integer> ids);

        /**
         * columns: 自定义查询列
         * @return names
         */
        @AutoMapper(columns = "distinct name")
        List<String> findAllName();

        /**
         * 根据 name 排序
         * @return MybatisAutoMapperTest
         */
        @AutoMapper
        List<MybatisAutoMapperTest> findAllOrderByNameAsc();

        /**
         * table: 当返回值/方法参数/Mapper接口和数据库不一致而无法解析出表名或解析错误时，可以此指定
         * @return names
         */
        @AutoMapper(columns = "distinct name", table = "mybatis_auto_mapper_test")
        List<String> findByNameNotNull();

        /**
         * countBy 开头的方法名可获取数量，用法同 findBy/deleteBy
         * 若需去重可使用 columns
         * @param id id
         * @return count
         */
        @AutoMapper(columns = "count(distinct *)")
        int countById(@Param("id") Integer id);

        /**
         * 返回值为 Map<PrimaryKey, Entity> 时，须使用 @MapKey 注解指定 key 属性
         * @param id id
         * @return map
         */
        @AutoMapper
        @MapKey("id")
        Map<Integer, MybatisAutoMapperTest> findByIdNotIn(@Param("ids") List<Integer> id);

        /**
         * 方法参数中存在 pageNum, pageSize 时进行分页
         * 返回值泛型须存在
         * 若需要返回 PageInfo 需自行转换
         * pageBy/pageAll 仅做与 findBy/findAll 区分之用
         * @param sid sid
         * @param eid eid
         * @param ids ids
         * @return MybatisAutoMapperTest
         */
        @AutoMapper
        List<MybatisAutoMapperTest> pageByIdBetweenOrIdInOrderByNameAscCreateTimeDesc(@Param("sid") Integer sid, @Param("eid") Integer eid, @Param("ids")List<Integer> ids, @Param("pageNum") int pageNum, @Param("pageSize") int pageSize);
    }

    @Test
    public void test() {
        String database = "oracle";
        MapperHandler mapperHandler = new MapperHandler();
        Arrays.stream(AutoMapperTest.class.getMethods())
                .filter(e -> e.isAnnotationPresent(AutoMapper.class))
                .sorted(Comparator.comparing(Method::getName))
                .map(e -> mapperHandler.setMapperMethodConfiguration(new MapperMethodConfiguration(AutoMapperTest.class, e, database)).parse().getMapperXml())
                .forEach(System.out::println);
    }
}
