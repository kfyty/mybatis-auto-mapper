package com.kfyty.mybatis.jpa.support.listener;

import com.github.pagehelper.PageInterceptor;
import com.kfyty.mybatis.jpa.support.annotation.AutoMapper;
import com.kfyty.mybatis.jpa.support.handle.MethodHandler;
import com.kfyty.mybatis.jpa.support.proxy.MybatisPageHelperProxyFactoryBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * 功能描述: Spring 监听器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/6 11:06
 * @since JDK 1.8
 */
public class MybatisJpaSupportListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent applicationEvent) {
        this.parseMapperInterface(applicationEvent);
    }

    private void parseMapperInterface(ContextRefreshedEvent contextRefreshedEvent) {
        ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) contextRefreshedEvent.getApplicationContext();
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
        Map<String, Object> mapperMap = applicationContext.getBeansWithAnnotation(Mapper.class);
        Configuration configuration = applicationContext.getBean(SqlSessionTemplate.class).getConfiguration();
        for (Map.Entry<String, Object> entry : mapperMap.entrySet()) {
            Method[] methods = entry.getValue().getClass().getInterfaces()[0].getMethods();
            Arrays.stream(methods).filter(e -> e.isAnnotationPresent(AutoMapper.class)).forEach(e -> new MethodHandler(e, configuration).parse());
            BeanDefinitionBuilder beanFactoryDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(MybatisPageHelperProxyFactoryBean.class);
            beanFactoryDefinitionBuilder.addConstructorArgValue(entry.getValue());
            beanFactory.removeBeanDefinition(entry.getKey());
            beanFactory.registerBeanDefinition(entry.getKey(), beanFactoryDefinitionBuilder.getBeanDefinition());
        }
        if(configuration.getInterceptors().stream().noneMatch(e -> Interceptor.class.isAssignableFrom(e.getClass()))) {
            configuration.addInterceptor(applicationContext.getBean(PageInterceptor.class));
        }
    }
}
