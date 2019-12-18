package com.kfyty.mybatis.jpa.support.configure;

import com.kfyty.mybatis.jpa.support.annotation.AutoMapper;
import com.kfyty.mybatis.jpa.support.match.SQLOperateEnum;
import com.kfyty.mybatis.jpa.support.utils.CommonUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.Param;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    private Class<?> mapperInterface;

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
    private String table;

    @Getter
    private SQLOperateEnum operateEnum;

    private AutoMapper classAnnotation;

    private AutoMapper methodAnnotation;

    public MapperMethodConfiguration(Method mapperMethod) {
        this.initConfiguration(mapperMethod);
    }

    public MapperMethodConfiguration initConfiguration(Method mapperMethod) {
        this.mapperInterface = mapperMethod.getDeclaringClass();
        this.mapperMethod = mapperMethod;
        this.classAnnotation = this.mapperInterface.getAnnotation(AutoMapper.class);
        this.methodAnnotation = this.mapperMethod.getAnnotation(AutoMapper.class);
        this.operateEnum = SQLOperateEnum.matchSQLOperate(mapperMethod.getName());
        this.initReturnType();
        this.initParameterType();
        this.initQueryParameters();
        this.initPrimaryKey();
        this.initSuffix();
        this.initWhere();
        this.initColumns();
        this.initUseDefault();
        this.initTable();
        return this;
    }

    public String getMatchName(SQLOperateEnum operateEnum) {
        return this.mapperMethod.getName().replaceFirst(operateEnum.operate(), "");
    }

    private void initReturnType() {
        if(mapperMethod.getReturnType().isArray()) {
            this.returnType = returnType.getComponentType();
            return ;
        }
        if(Map.class.isAssignableFrom(mapperMethod.getReturnType())) {
            this.returnType = mapperMethod.getReturnType();
            return ;
        }
        this.returnType = mapperMethod.getReturnType();
        Type genericReturnType = mapperMethod.getGenericReturnType();
        if(genericReturnType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
            if(Collection.class.isAssignableFrom(returnType)) {
                if(actualTypeArguments[0] instanceof ParameterizedType) {
                    Class<?> clazz = (Class<?>) ((ParameterizedType) actualTypeArguments[0]).getRawType();
                    if(Map.class.isAssignableFrom(clazz)) {
                        this.returnType = clazz;
                        return ;
                    }
                    throw new IllegalArgumentException("build sql error: nested return type must be Map type and nested type must be one level !");
                }
                this.returnType = (Class<?>) actualTypeArguments[0];
                return ;
            }
            throw new IllegalArgumentException("build sql error: return type must be base type or Map/Collection/Pojo type !");
        }
    }

    private void initParameterType() {
        if(mapperMethod.getName().contains(SQLOperateEnum.OPERATE_INSERT_ALL.operate()) || mapperMethod.getName().contains(SQLOperateEnum.OPERATE_UPDATE_ALL.operate())) {
            this.parameterType = (Class<?>) ((ParameterizedType) mapperMethod.getGenericParameterTypes()[0]).getActualTypeArguments()[0];
            return ;
        }
        if(mapperMethod.getName().contains(SQLOperateEnum.OPERATE_INSERT.operate()) || mapperMethod.getName().contains(SQLOperateEnum.OPERATE_UPDATE.operate())) {
            this.parameterType = mapperMethod.getParameterTypes()[0];
        }
    }

    private void initQueryParameters() {
        this.queryParameters = new ArrayList<>();
        Parameter[] parameters = mapperMethod.getParameters();
        for (int i = 0; parameters != null && i < parameters.length; i++) {
            if(parameters[i].isAnnotationPresent(Param.class)) {
                this.queryParameters.add(parameters[i].getAnnotation(Param.class).value());
            }
        }
    }

    private void initPrimaryKey() {
        if(classAnnotation != null) {
            this.primaryKey = classAnnotation.primaryKey();
        }
        if(!Arrays.equals(methodAnnotation.primaryKey(), new String[] {""})) {
            this.primaryKey = methodAnnotation.primaryKey();
        }
        if(primaryKey == null || Arrays.equals(primaryKey, new String[] {""})) {
            this.primaryKey = DEFAULT_PRIMARY_KEY;
        }
    }

    private void initSuffix() {
        if(classAnnotation != null) {
            this.suffix = classAnnotation.suffix();
        }
        if(!CommonUtil.empty(methodAnnotation.suffix())) {
            this.suffix = methodAnnotation.suffix();
        }
        if(CommonUtil.empty(suffix)) {
            this.suffix = DEFAULT_SUFFIX;
        }
    }

    private void initWhere() {
        this.where = "";
        if(classAnnotation == null && CommonUtil.empty(methodAnnotation.where())) {
            return ;
        }
        if(classAnnotation != null && CommonUtil.empty(classAnnotation.where()) && CommonUtil.empty(methodAnnotation.where())) {
            return ;
        }
        if(classAnnotation != null && !CommonUtil.empty(classAnnotation.where()) && CommonUtil.empty(methodAnnotation.separator())) {
            return ;
        }
        if(!CommonUtil.empty(methodAnnotation.where())) {
            this.where = getSeparator(methodAnnotation) + methodAnnotation.where();
        }
        if(classAnnotation != null && !CommonUtil.empty(classAnnotation.where())) {
            this.where = getSeparator(classAnnotation) + classAnnotation.where() + this.where;
        }
    }

    private void initColumns() {
        this.columns = methodAnnotation.columns();
    }

    private void initUseDefault() {
        this.useDefault = methodAnnotation.useDefault();
    }

    private void initTable() {
        if(classAnnotation != null) {
            this.table = classAnnotation.table();
        }
        if(!CommonUtil.empty(methodAnnotation.table())) {
            this.table = methodAnnotation.table();
        }
        if(!CommonUtil.empty(table)) {
            return ;
        }
        if(!tableNameInvalid()) {
            this.table = CommonUtil.convert2Underline(getReturnType().getSimpleName().replace(getSuffix(), ""), true);
            return ;
        }
        if(operateEnum.equals(SQLOperateEnum.OPERATE_INSERT)          ||
                operateEnum.equals(SQLOperateEnum.OPERATE_UPDATE)     ||
                operateEnum.equals(SQLOperateEnum.OPERATE_INSERT_ALL) ||
                operateEnum.equals(SQLOperateEnum.OPERATE_UPDATE_ALL)) {
            this.table = CommonUtil.convert2Underline(getParameterType().getSimpleName().replace(getSuffix(), ""), true);
            return ;
        }
        this.table = CommonUtil.convert2Underline(mapperInterface.getSimpleName().replaceAll("Mapper|Dao", ""), true);
    }

    private String getSeparator(AutoMapper autoMapper) {
        return " " + autoMapper.separator() + " ";
    }

    private boolean tableNameInvalid() {
        return !getColumns().equals("*")                      ||
                getReturnType().equals(void.class)            ||
                getReturnType().equals(Void.class)            ||
                CommonUtil.baseType(getReturnType())          ||
                Map.class.isAssignableFrom(getReturnType());
    }
}
