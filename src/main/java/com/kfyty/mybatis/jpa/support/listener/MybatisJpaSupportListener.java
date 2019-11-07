package com.kfyty.mybatis.jpa.support.listener;

import com.kfyty.mybatis.jpa.support.proxy.MybatisJpaSupportProxyFactory;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;

/**
 * 功能描述: Spring 监听器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/6 11:06
 * @since JDK 1.8
 */
public class MybatisJpaSupportListener implements ApplicationListener<ApplicationEvent> {

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if(applicationEvent instanceof ContextRefreshedEvent) {
            this.generateNewBeanDefine((ContextRefreshedEvent) applicationEvent);
        }
    }

    private void generateNewBeanDefine(ContextRefreshedEvent contextRefreshedEvent) {
        ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) contextRefreshedEvent.getApplicationContext();
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(Mapper.class);
        for (Map.Entry<String, Object> entry : beansWithAnnotation.entrySet()) {
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(MybatisJpaSupportProxyFactory.class);
            beanDefinitionBuilder.addConstructorArgValue(entry.getValue());
            beanFactory.removeBeanDefinition(entry.getKey());
            beanFactory.registerBeanDefinition(entry.getKey(), beanDefinitionBuilder.getBeanDefinition());
        }
    }
}
