package com.kfyty.mybatis.jpa.support.proxy;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.kfyty.mybatis.jpa.support.match.SQLOperateEnum;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 功能描述: mybatis 分页代理工厂
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/14 19:53
 * @since JDK 1.8
 */
public class MybatisPageHelperProxyFactoryBean implements InvocationHandler, FactoryBean<Object> {
    private Object target;

    public MybatisPageHelperProxyFactoryBean(Object target) {
        this.target = target;
    }

    @Override
    public Object getObject() throws Exception {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), this.target.getClass().getInterfaces(), this);
    }

    @Override
    public Class<?> getObjectType() {
        return this.target.getClass();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(Page.class.isAssignableFrom(method.getReturnType()) && verifyPageParameters(args)) {
            return PageHelper.startPage((int) args[args.length - 2], (int) args[args.length - 1]).doSelectPage(() -> this.invoke(method, args));
        }
        if((method.getName().startsWith(SQLOperateEnum.OPERATE_PAGE_BY.operate()) || method.getName().startsWith(SQLOperateEnum.OPERATE_PAGE_ALL.operate())) && verifyPageParameters(args)) {
            return PageHelper.startPage((int) args[args.length - 2], (int) args[args.length - 1]).doSelectPage(() -> this.invoke(method, args));
        }
        return this.invoke(method, args);
    }

    private boolean verifyPageParameters(Object[] args) {
        if(args == null || args.length < 2) {
            throw new IllegalArgumentException("start page error: no page number and page size found !");
        }
        return true;
    }

    private Object invoke(Method method, Object ... args) {
        try {
            return method.invoke(this.target, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
