package com.kfyty.mybatis.jpa.support.handle;

import com.kfyty.mybatis.jpa.support.annotation.JpaQuery;
import com.kfyty.mybatis.jpa.support.utils.CommonUtil;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 功能描述: 方法处理器，得到 MappedStatement
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/6 16:57
 * @since JDK 1.8
 */
public class MethodHandler {
    private String id;
    private Method method;
    private Class<?> returnType;
    private Configuration configuration;
    private MappedStatement mappedStatement;
    private List<String> queryParameters;
    private List<ResultMapping> resultMappings;

    public MethodHandler(Method method, Configuration configuration) {
        this.method = method;
        this.configuration = configuration;
    }

    public MethodHandler parse() {
        this.initResultMappings();
        this.initMappedStatement();
        return this;
    }

    public String getId() {
        if(id == null) {
            this.id = this.getMethod().getDeclaringClass().getName() + "." + method.getName();
        }
        return this.id;
    }

    public Method getMethod() {
        return this.method;
    }

    public List<String> getQueryParameters() {
        if(queryParameters != null) {
            return this.queryParameters;
        }
        this.queryParameters = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; parameters != null && i < parameters.length; i++) {
            if(parameters[i].isAnnotationPresent(Param.class)) {
                this.queryParameters.add(parameters[i].getAnnotation(Param.class).value());
            }
        }
        return this.queryParameters;
    }

    public String getSuffix() {
        return method.getAnnotation(JpaQuery.class).suffix();
    }

    public Class<?> getReturnType() {
        if(returnType != null) {
            return this.returnType;
        }
        this.returnType = method.getReturnType();
        Type genericReturnType = method.getGenericReturnType();
        if(genericReturnType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
            if(Collection.class.isAssignableFrom(returnType)) {
                this.returnType = (Class<?>) actualTypeArguments[0];
            }
            if(Map.class.isAssignableFrom(returnType)) {
                this.returnType = (Class<?>) actualTypeArguments[1];
            }
        }
        return this.returnType;
    }

    public List<ResultMapping> getResultMappings() {
        if(resultMappings == null) {
            this.initResultMappings();
        }
        return this.resultMappings;
    }

    public MappedStatement getMappedStatement() {
        if(mappedStatement == null) {
            this.parse();
        }
        return this.mappedStatement;
    }

    private void initResultMappings() {
        Map<String, Field> fieldMap = CommonUtil.getFieldMap(this.getReturnType());
        this.resultMappings = new ArrayList<>();
        for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
            this.resultMappings.add(new ResultMapping.Builder(configuration, entry.getKey(), CommonUtil.convert2Underline(entry.getKey(), true), entry.getValue().getType()).build());
        }
    }

    private void initMappedStatement() {
        SQLHandler sqlHandler = new SQLHandler(this).parse();
        this.mappedStatement = new MappedStatement.Builder(
                configuration,
                this.getId(),
                new SqlSourceBuilder(configuration).parse(sqlHandler.getSQL(), Object.class, null),
                sqlHandler.getSQLCommandType())
                .resultMaps(Collections.singletonList(new ResultMap.Builder(
                        configuration,
                        this.getReturnType().getName() + "Map",
                        this.getReturnType(),
                        this.getResultMappings()).build()))
                .build();
    }
}
