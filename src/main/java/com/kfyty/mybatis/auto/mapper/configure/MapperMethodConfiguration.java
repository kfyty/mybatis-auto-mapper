package com.kfyty.mybatis.auto.mapper.configure;

import com.kfyty.mybatis.auto.mapper.BaseMapper;
import com.kfyty.mybatis.auto.mapper.annotation.AutoMapper;
import com.kfyty.mybatis.auto.mapper.annotation.SelectKey;
import com.kfyty.mybatis.auto.mapper.match.SQLOperateEnum;
import com.kfyty.mybatis.auto.mapper.struct.TableFieldStruct;
import com.kfyty.mybatis.auto.mapper.struct.TableStruct;
import com.kfyty.support.generic.SimpleGeneric;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.Param;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 功能描述: 映射方法配置
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/19 18:36
 * @since JDK 1.8
 */
@NoArgsConstructor
public class MapperMethodConfiguration {
    private static final String DEFAULT_SUFFIX = "Pojo";
    private static final String[] DEFAULT_PRIMARY_KEY = new String[] {"id"};

    @Getter
    private String database;

    @Getter
    private Class<?> mapperInterface;

    @Getter
    private Class<?> childInterface;

    @Getter
    private Method mapperMethod;

    @Getter
    private Class<?> returnType;

    @Getter
    private Class<?> parameterType;

    @Getter
    private List<String> queryParameters;

    @Getter
    private String[] primaryKey;

    @Getter
    private String suffix;

    @Getter
    private String where;

    @Getter
    private String columns;

    @Getter
    private Boolean useDefault;

    @Getter
    private Boolean allowNull;

    @Getter
    private String table;

    @Getter
    private SQLOperateEnum operateEnum;

    @Getter
    private SelectKey selectKey;

    private AutoMapper classAnnotation;

    private AutoMapper methodAnnotation;

    public MapperMethodConfiguration(Method mapperMethod, String database) {
        this(mapperMethod.getDeclaringClass(), mapperMethod, database);
    }

    public MapperMethodConfiguration(Class<?> childInterface, Method mapperMethod, String database) {
        this.parseConfiguration(childInterface, mapperMethod, database);
    }

    public MapperMethodConfiguration parseConfiguration(Method mapperMethod, String database) {
        return this.parseConfiguration(mapperMethod.getDeclaringClass(), mapperMethod, database);
    }

    public MapperMethodConfiguration parseConfiguration(Class<?> childInterface, Method mapperMethod, String database) {
        this.database = database;
        this.mapperInterface = mapperMethod.getDeclaringClass();
        this.childInterface = childInterface;
        this.mapperMethod = mapperMethod;
        this.classAnnotation = mapperInterface.equals(BaseMapper.class)
                                    ? childInterface.getAnnotation(AutoMapper.class)
                                    : this.mapperInterface.getAnnotation(AutoMapper.class);
        this.methodAnnotation = this.mapperMethod.getAnnotation(AutoMapper.class);
        this.operateEnum = SQLOperateEnum.matchSQLOperate(mapperMethod.getName());
        this.initReturnType();
        this.initParameterType();
        this.initPrimaryKey();
        this.initQueryParameters();
        this.initSuffix();
        this.initWhere();
        this.initColumns();
        this.initUseDefault();
        this.initAllowNull();
        this.initTable();
        this.initSelectKey();
        return this;
    }

    public String getMatchName(SQLOperateEnum operateEnum) {
        if (operateEnum.pattern() == null) {
            return this.mapperMethod.getName().replaceFirst(operateEnum.operate(), "");
        }
        Matcher matcher = operateEnum.pattern().matcher(this.mapperMethod.getName());
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid SQL operate enum !");
        }
        return this.mapperMethod.getName().replaceFirst(matcher.group(), "");
    }

    private Class<?> deduceEntityClass() {
        return ReflectUtil.getSuperGeneric(this.childInterface, 1, e -> e instanceof ParameterizedType && ((ParameterizedType) e).getRawType().equals(BaseMapper.class));
    }

    private void initReturnType() {
        if (mapperInterface.equals(BaseMapper.class)) {
            if (Collection.class.isAssignableFrom(mapperMethod.getReturnType()) || Object.class.equals(mapperMethod.getReturnType())) {
                this.returnType = this.deduceEntityClass();
                return;
            }
        }
        SimpleGeneric generic = SimpleGeneric.from(this.mapperMethod);
        if (Map.class.isAssignableFrom(generic.getSimpleActualType()) || generic.isMapGeneric() && generic.getMapValueType().get().equals(Object.class)) {
            this.returnType = HashMap.class;
            return;
        }
        this.returnType = generic.getSimpleActualType();
    }

    private void initParameterType() {
        if (mapperInterface.equals(BaseMapper.class)) {
            this.parameterType = this.deduceEntityClass();
            return;
        }
        if (mapperMethod.getName().contains(SQLOperateEnum.OPERATE_INSERT_ALL.operate()) || mapperMethod.getName().contains(SQLOperateEnum.OPERATE_UPDATE_ALL.operate())) {
            this.parameterType = (Class<?>) ((ParameterizedType) mapperMethod.getGenericParameterTypes()[0]).getActualTypeArguments()[0];
            return;
        }
        if (mapperMethod.getName().contains(SQLOperateEnum.OPERATE_INSERT.operate()) || mapperMethod.getName().contains(SQLOperateEnum.OPERATE_UPDATE.operate())) {
            this.parameterType = mapperMethod.getParameterTypes()[0];
        }
    }

    private void initQueryParameters() {
        this.queryParameters = new ArrayList<>();
        Parameter[] parameters = mapperMethod.getParameters();
        for (int i = 0; parameters != null && i < parameters.length; i++) {
            if (!parameters[i].isAnnotationPresent(Param.class)) {
                continue;
            }
            Param param = parameters[i].getAnnotation(Param.class);
            if (!mapperInterface.equals(BaseMapper.class) || !parameters[i].getType().isArray() || !"pk".equals(param.value())) {
                this.queryParameters.add(param.value());
                continue;
            }
            IntStream.range(0, primaryKey.length).forEach(e -> this.queryParameters.add(param.value() + "[" + e + "]"));
        }
    }

    private void initPrimaryKey() {
        if (classAnnotation != null) {
            this.primaryKey = classAnnotation.primaryKey();
        }
        if (!Arrays.equals(methodAnnotation.primaryKey(), new String[] {""})) {
            this.primaryKey = methodAnnotation.primaryKey();
        }
        if (primaryKey == null || Arrays.equals(primaryKey, new String[] {""})) {
            this.primaryKey = DEFAULT_PRIMARY_KEY;
        }
    }

    private void initSuffix() {
        if (classAnnotation != null) {
            this.suffix = classAnnotation.suffix();
        }
        if (CommonUtil.notEmpty(methodAnnotation.suffix())) {
            this.suffix = methodAnnotation.suffix();
        }
        if (CommonUtil.empty(suffix)) {
            this.suffix = DEFAULT_SUFFIX;
        }
    }

    private void initWhere() {
        this.where = "";
        if (!methodAnnotation.extend()) {
            if (CommonUtil.notEmpty(methodAnnotation.where())) {
                this.where = getSeparator(methodAnnotation) + methodAnnotation.where();
            }
            return;
        }
        if (classAnnotation == null && CommonUtil.empty(methodAnnotation.where())) {
            return;
        }
        if (classAnnotation != null && CommonUtil.empty(classAnnotation.where()) && CommonUtil.empty(methodAnnotation.where())) {
            return;
        }
        if (CommonUtil.notEmpty(methodAnnotation.where())) {
            this.where = getSeparator(methodAnnotation) + methodAnnotation.where();
        }
        if (classAnnotation != null && CommonUtil.notEmpty(classAnnotation.where())) {
            this.where = getSeparator(classAnnotation) + classAnnotation.where() + this.where;
        }
    }

    private void initColumns() {
        this.columns = methodAnnotation.columns();
        if (CommonUtil.empty(this.columns)) {
            this.columns = "*";
        }
        if (operateEnum.pattern() == null || !methodAnnotation.parseColumn()) {
            return;
        }
        String group = null;
        Matcher matcher = operateEnum.pattern().matcher(this.mapperMethod.getName());
        if (!matcher.find() || CommonUtil.empty(group = matcher.group(1))) {
            return;
        }
        List<String> columns = CommonUtil.split(group, "And").stream().map(CommonUtil::camelCase2Underline).collect(Collectors.toList());
        this.columns = String.join(", ", columns);
    }

    private void initUseDefault() {
        this.useDefault = methodAnnotation.useDefault();
    }

    private void initAllowNull() {
        this.allowNull = methodAnnotation.allowNull();
    }

    private void initTable() {
        if (classAnnotation != null) {
            this.table = classAnnotation.table();
        }
        if (CommonUtil.notEmpty(methodAnnotation.table())) {
            this.table = methodAnnotation.table();
        }
        if (CommonUtil.notEmpty(table)) {
            return;
        }
        if (!tableNameInvalid()) {
            this.table = CommonUtil.camelCase2Underline(getReturnType().getSimpleName().replace(getSuffix(), ""));
            return;
        }
        if (operateEnum.equals(SQLOperateEnum.OPERATE_INSERT)         ||
                operateEnum.equals(SQLOperateEnum.OPERATE_UPDATE)     ||
                operateEnum.equals(SQLOperateEnum.OPERATE_INSERT_ALL) ||
                operateEnum.equals(SQLOperateEnum.OPERATE_UPDATE_ALL)) {
            this.table = CommonUtil.camelCase2Underline(getParameterType().getSimpleName().replace(getSuffix(), ""));
            return;
        }
        this.table = CommonUtil.camelCase2Underline(childInterface.getSimpleName().replaceAll("Mapper$|Dao$", ""));
    }

    private void initSelectKey() {
        this.selectKey = mapperInterface.getAnnotation(SelectKey.class);
        if (mapperInterface.equals(BaseMapper.class)) {
            this.selectKey = childInterface.getAnnotation(SelectKey.class);
        }
        if (mapperMethod.isAnnotationPresent(SelectKey.class)) {
            this.selectKey = mapperMethod.getAnnotation(SelectKey.class);
        }
    }

    private String getSeparator(AutoMapper autoMapper) {
        return " " + autoMapper.separator() + " ";
    }

    private boolean tableNameInvalid() {
        return !"*".equals(getColumns())                                 ||
                getReturnType().equals(void.class)                       ||
                getReturnType().equals(Void.class)                       ||
                ReflectUtil.isBaseDataType(getReturnType())              ||
                Map.class.isAssignableFrom(getReturnType())              ||
                TableStruct.class.isAssignableFrom(getReturnType())      ||
                TableFieldStruct.class.isAssignableFrom(getReturnType()) ||
                getReturnType().getSimpleName().endsWith("Vo")           ||
                getReturnType().getSimpleName().endsWith("VO")           ||
                getReturnType().getSimpleName().endsWith("Bo")           ||
                getReturnType().getSimpleName().endsWith("BO");
    }
}
