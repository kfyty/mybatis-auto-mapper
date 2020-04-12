package com.kfyty.mybatis.auto.mapper.listener;

import com.github.pagehelper.PageInterceptor;
import com.kfyty.mybatis.auto.mapper.annotation.AutoMapper;
import com.kfyty.mybatis.auto.mapper.handle.MethodHandler;
import com.kfyty.mybatis.auto.mapper.proxy.MybatisPageHelperProxyFactoryBean;
import com.kfyty.mybatis.auto.mapper.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 功能描述: Spring 监听器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/6 11:06
 * @since JDK 1.8
 */
@Slf4j
public class MybatisAutoMapperListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent applicationEvent) {
        if(applicationEvent.getApplicationContext().getParent() == null) {
            this.parseMapperInterface(applicationEvent);
        }
    }

    private void parseMapperInterface(ContextRefreshedEvent contextRefreshedEvent) {
        ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) contextRefreshedEvent.getApplicationContext();
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
        Configuration configuration = applicationContext.getBean(SqlSessionFactory.class).getConfiguration();
        Set<Class<?>> mapperInterfaces = this.getMapperInterfaces(configuration);
        for (Class<?> mapperInterface : mapperInterfaces) {
            Method[] methods = mapperInterface.getMethods();
            Arrays.stream(methods).filter(e -> e.isAnnotationPresent(AutoMapper.class)).forEach(e -> new MethodHandler(e, configuration).parse());
            BeanDefinitionBuilder beanFactoryDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(MybatisPageHelperProxyFactoryBean.class);
            beanFactoryDefinitionBuilder.addConstructorArgValue(applicationContext.getBean(mapperInterface));
            beanFactory.removeBeanDefinition(CommonUtil.convert2BeanName(mapperInterface));
            beanFactory.registerBeanDefinition(CommonUtil.convert2BeanName(mapperInterface), beanFactoryDefinitionBuilder.getBeanDefinition());
        }
        if(configuration.getInterceptors().stream().noneMatch(e -> PageInterceptor.class.isAssignableFrom(e.getClass()))) {
            configuration.addInterceptor(applicationContext.getBean(PageInterceptor.class));
        }
    }

    @SuppressWarnings("unchecked")
    private Set<Class<?>> getMapperInterfaces(Configuration configuration) {
        try {
            Set<Class<?>> mapperInterfaces = new HashSet<>();
            Field resources = configuration.getClass().getDeclaredField("loadedResources");
            boolean accessible = resources.isAccessible();
            resources.setAccessible(true);
            Set<String> mapperInterfaceNames = (Set<String>) resources.get(configuration);
            resources.setAccessible(accessible);
            for (String mapperInterfaceName : mapperInterfaceNames) {
                mapperInterfaces.add(Class.forName(mapperInterfaceName.replaceFirst("interface ", "")));
            }
            return mapperInterfaces;
        } catch(NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            log.error("Load mapper interface error !", e);
            throw new RuntimeException(e);
        }
    }
}
