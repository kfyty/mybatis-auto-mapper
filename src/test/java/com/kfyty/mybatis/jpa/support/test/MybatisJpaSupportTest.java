package com.kfyty.mybatis.jpa.support.test;

import com.kfyty.mybatis.jpa.support.annotation.JpaQuery;
import com.kfyty.mybatis.jpa.support.annotation.Pageable;
import com.kfyty.mybatis.jpa.support.handle.MapperHandler;
import com.kfyty.mybatis.jpa.support.handle.MethodHandler;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.junit.jupiter.api.Test;

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
public class MybatisJpaSupportTest {
    private Integer id;
    private String name;
    private Date createTime;

    private Method getMethod(String name, Class<?> ... classes) {
        try {
            return MybatisJpaSupportTest.class.getDeclaredMethod(name, classes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 需要解析的方法需要添加 @JpaQuery 注解
     * @param obj
     * @return
     */
    @JpaQuery
    public int insertMybatisJpaSupportTest(@Param("obj") MybatisJpaSupportTest obj) {
        return 0;
    }

    @JpaQuery
    public int insertAllMybatisJpaSupportTest(@Param("objs") List<MybatisJpaSupportTest> objs) {
        return 0;
    }

    @JpaQuery
    public int updateMybatisJpaSupportTest(@Param("obj") MybatisJpaSupportTest obj) {
        return 0;
    }

    /**
     * primaryKey: 更新时指定主键属性，默认为 id
     * @param objs
     * @return
     */
    @JpaQuery(primaryKey = "id")
    public int updateAllMybatisJpaSupportTest(@Param("objs") List<MybatisJpaSupportTest> objs) {
        return 0;
    }

    @JpaQuery
    public MybatisJpaSupportTest findById(@Param("id") Integer id) {
        return null;
    }

    @JpaQuery
    public MybatisJpaSupportTest findByIdLessThanOrCreateTimeLessEqual(@Param("id") Integer id, @Param("createTime") Date createTime) {
        return null;
    }

    @JpaQuery
    public MybatisJpaSupportTest findByIdEqualAndNameNotNull(@Param("id") Integer id, @Param("name") String name) {
        return null;
    }

    @JpaQuery
    public MybatisJpaSupportTest findByIdEqualAndNameNotNullOrCreateTimeBetween(@Param("id") Integer id, @Param("name") String name, @Param("start") Date start, @Param("end") Date end) {
        return null;
    }

    /**
     * OrderBy 之前没有 And/Or，切必须是最后一项
     * @param sid
     * @param eid
     * @param ids
     * @return
     */
    @JpaQuery
    public MybatisJpaSupportTest findByIdBetweenOrIdInOrderByNameAscCreateTimeDesc(@Param("sid") Integer sid, @Param("eid") Integer eid, @Param("ids")List<Integer> ids) {
        return null;
    }

    /**
     * columns: 自定义查询列
     * @return
     */
    @JpaQuery(columns = "distinct name")
    public List<String> findAllName() {
        return null;
    }

    /**
     * table: 当返回值/方法参数/Mapper接口无法解析出表名时，需以此指定
     * @return
     */
    @JpaQuery(columns = "distinct name", table = "mybatis_jpa_support_test")
    public List<String> findByNameNotNull() {
        return null;
    }

    /**
     * countBy 开头的方法名可获取数量，用法同 findBy/deleteBy
     * 若需去重可使用 columns
     * @param id
     * @return
     */
    @JpaQuery
    public int countById(@Param("id") Integer id) {
        return 0;
    }

    /**
     * 返回值为 Map 时，须使用 @MapKey 注解指定 key 属性
     * @param id
     * @return
     */
    @JpaQuery
    @MapKey("id")
    public Map<Integer, MybatisJpaSupportTest> findByIdNotIn(@Param("ids") List<Integer> id) {
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
    @JpaQuery
    @Pageable
    public List<MybatisJpaSupportTest> pageByIdBetweenOrIdInOrderByNameAscCreateTimeDesc(@Param("sid") Integer sid, @Param("eid") Integer eid, @Param("ids")List<Integer> ids, int pageNum, int pageSize) {
        return null;
    }

    @Test
    public void test() {
        MethodHandler methodHandler = null;

        methodHandler = new MethodHandler(this.getMethod("insertMybatisJpaSupportTest", MybatisJpaSupportTest.class), null);
        System.out.println(new MapperHandler(methodHandler).parse().getMapperXml());

        methodHandler = new MethodHandler(this.getMethod("insertAllMybatisJpaSupportTest", List.class), null);
        System.out.println(new MapperHandler(methodHandler).parse().getMapperXml());

        methodHandler = new MethodHandler(this.getMethod("updateMybatisJpaSupportTest", MybatisJpaSupportTest.class), null);
        System.out.println(new MapperHandler(methodHandler).parse().getMapperXml());

        methodHandler = new MethodHandler(this.getMethod("updateAllMybatisJpaSupportTest", List.class), null);
        System.out.println(new MapperHandler(methodHandler).parse().getMapperXml());

        methodHandler = new MethodHandler(this.getMethod("findById", Integer.class), null);
        System.out.println(new MapperHandler(methodHandler).parse().getMapperXml());

        methodHandler = new MethodHandler(this.getMethod("findByIdLessThanOrCreateTimeLessEqual", Integer.class, Date.class), null);
        System.out.println(new MapperHandler(methodHandler).parse().getMapperXml());

        methodHandler = new MethodHandler(this.getMethod("findByIdEqualAndNameNotNull", Integer.class, String.class), null);
        System.out.println(new MapperHandler(methodHandler).parse().getMapperXml());

        methodHandler = new MethodHandler(this.getMethod("findByIdEqualAndNameNotNullOrCreateTimeBetween", Integer.class, String.class, Date.class, Date.class), null);
        System.out.println(new MapperHandler(methodHandler).parse().getMapperXml());

        methodHandler = new MethodHandler(this.getMethod("findByIdBetweenOrIdInOrderByNameAscCreateTimeDesc", Integer.class, Integer.class, List.class), null);
        System.out.println(new MapperHandler(methodHandler).parse().getMapperXml());

        methodHandler = new MethodHandler(this.getMethod("findAllName"), null);
        System.out.println(new MapperHandler(methodHandler).parse().getMapperXml());

        methodHandler = new MethodHandler(this.getMethod("findByNameNotNull"), null);
        System.out.println(new MapperHandler(methodHandler).parse().getMapperXml());

        methodHandler = new MethodHandler(this.getMethod("findByIdNotIn", List.class), null);
        System.out.println(new MapperHandler(methodHandler).parse().getMapperXml());

        methodHandler = new MethodHandler(this.getMethod("countById", Integer.class), null);
        System.out.println(new MapperHandler(methodHandler).parse().getMapperXml());

        methodHandler = new MethodHandler(this.getMethod("pageByIdBetweenOrIdInOrderByNameAscCreateTimeDesc", Integer.class, Integer.class, List.class, int.class, int.class), null);
        System.out.println(new MapperHandler(methodHandler).parse().getMapperXml());
    }
}
