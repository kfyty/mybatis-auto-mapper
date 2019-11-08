package com.kfyty.mybatis.jpa.support.handle;

import com.kfyty.mybatis.jpa.support.annotation.JpaQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.IncompleteElementException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.xml.XMLStatementBuilder;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
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
    private List<String> queryParameters;

    public MethodHandler(Method method, Configuration configuration) {
        this.method = method;
        this.configuration = configuration;
    }

    public MethodHandler parse() {
        MapperHandler mapperHandler = new MapperHandler(this).parse();
        MapperBuilderAssistant mapperBuilderAssistant = new MapperBuilderAssistant(configuration, mapperHandler.getMapperXml());
        mapperBuilderAssistant.setCurrentNamespace(this.getMethod().getDeclaringClass().getName());
        XNode xNode = new XPathParser(mapperHandler.getMapperXml()).evalNode(mapperHandler.getMapperLabel());
        XMLStatementBuilder xmlStatementBuilder = new XMLStatementBuilder(configuration, mapperBuilderAssistant, xNode, configuration.getDatabaseId());
        try {
            xmlStatementBuilder.parseStatementNode();
        } catch (IncompleteElementException e) {
            configuration.addIncompleteStatement(xmlStatementBuilder);
        }
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
}
