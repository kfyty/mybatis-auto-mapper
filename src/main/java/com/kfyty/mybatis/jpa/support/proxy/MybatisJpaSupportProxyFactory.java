package com.kfyty.mybatis.jpa.support.proxy;

import com.kfyty.mybatis.jpa.support.annotation.JpaQuery;
import com.kfyty.mybatis.jpa.support.handle.MethodHandler;
import org.apache.ibatis.annotations.Param;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 功能描述: 工厂 Bean，用于生成 mapper 接口代理类的代理类
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/6 11:25
 * @since JDK 1.8
 */
public class MybatisJpaSupportProxyFactory implements InvocationHandler, FactoryBean<Object> {
    private Object target;

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    public MybatisJpaSupportProxyFactory(Object target) {
        this.target = target;
    }

    @Override
    public Object getObject() throws Exception {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
    }

    @Override
    public Class<?> getObjectType() {
        return this.target.getClass();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(!method.isAnnotationPresent(JpaQuery.class)) {
            return method.invoke(this.target, args);
        }
        MethodHandler methodHandler = new MethodHandler(method, sqlSessionTemplate.getConfiguration());
        try {
            this.sqlSessionTemplate.getConfiguration().getMappedStatement(methodHandler.getId());
        } catch(IllegalArgumentException e) {
            methodHandler.parse();
        }
        Class<?> returnType = method.getReturnType();
        Map<String, Object> queryParameters = this.prepareQueryParameters(method.getParameters(), args);
        if(method.getName().contains("insert")) {
            return this.sqlSessionTemplate.insert(methodHandler.getId(), queryParameters);
        }
        if(method.getName().contains("update")) {
            return this.sqlSessionTemplate.update(methodHandler.getId(), queryParameters);
        }
        if(method.getName().contains("delete")) {
            return this.sqlSessionTemplate.delete(methodHandler.getId(), queryParameters);
        }
        if(List.class.isAssignableFrom(returnType)) {
            return this.sqlSessionTemplate.selectList(methodHandler.getId(), queryParameters);
        }
        if(Set.class.isAssignableFrom(returnType)) {
            return new HashSet<>(this.sqlSessionTemplate.selectList(methodHandler.getId(), queryParameters));
        }
        if(Map.class.isAssignableFrom(returnType)) {
            return this.sqlSessionTemplate.selectMap(methodHandler.getId(), queryParameters, method.getAnnotation(JpaQuery.class).mapKey());
        }
        return this.sqlSessionTemplate.selectOne(methodHandler.getId(), queryParameters);
    }

    private Map<String, Object> prepareQueryParameters(Parameter[] parameters, Object[] args) {
        Map<String, Object> params = new HashMap<>();
        if(parameters == null) {
            return params;
        }
        for (int i = 0; i < parameters.length; i++) {
            if(parameters[i].isAnnotationPresent(Param.class)) {
                params.put(parameters[i].getAnnotation(Param.class).value(), args[i]);
            }
        }
        return params;
    }
}
