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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

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
        Map<String, SqlSessionFactory> sqlSessionFactoryMap = applicationContext.getBeansOfType(SqlSessionFactory.class);
        if(CommonUtil.empty(sqlSessionFactoryMap)) {
            log.info("No SqlSessionFactory instance found !");
            return;
        }
        MethodHandler methodHandler = new MethodHandler();
        for (Map.Entry<String, SqlSessionFactory> entry : sqlSessionFactoryMap.entrySet()) {
            Configuration configuration = entry.getValue().getConfiguration();
            Collection<Class<?>> mapperInterfaces = configuration.getMapperRegistry().getMappers();
            for (Class<?> mapperInterface : mapperInterfaces) {
                Method[] methods = mapperInterface.getMethods();
                Arrays.stream(methods).filter(e -> e.isAnnotationPresent(AutoMapper.class)).forEach(e -> methodHandler.setHandleData(mapperInterface, e, configuration).parse());
                BeanDefinitionBuilder beanFactoryDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(MybatisPageHelperProxyFactoryBean.class);
                beanFactoryDefinitionBuilder.addConstructorArgValue(applicationContext.getBean(mapperInterface));
                beanFactory.removeBeanDefinition(CommonUtil.convert2BeanName(mapperInterface));
                beanFactory.registerBeanDefinition(CommonUtil.convert2BeanName(mapperInterface), beanFactoryDefinitionBuilder.getBeanDefinition());
            }
            if(configuration.getInterceptors().stream().noneMatch(e -> PageInterceptor.class.isAssignableFrom(e.getClass()))) {
                configuration.addInterceptor(applicationContext.getBean(PageInterceptor.class));
            }
        }
    }
}
