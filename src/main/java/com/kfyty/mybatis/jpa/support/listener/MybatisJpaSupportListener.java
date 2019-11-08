package com.kfyty.mybatis.jpa.support.listener;

import com.kfyty.mybatis.jpa.support.annotation.JpaQuery;
import com.kfyty.mybatis.jpa.support.handle.MethodHandler;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Method;
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
            this.parseMapperInterface((ContextRefreshedEvent) applicationEvent);
        }
    }

    private void parseMapperInterface(ContextRefreshedEvent contextRefreshedEvent) {
        ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) contextRefreshedEvent.getApplicationContext();
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(Mapper.class);
        for (Map.Entry<String, Object> entry : beansWithAnnotation.entrySet()) {
            Method[] methods = entry.getValue().getClass().getInterfaces()[0].getMethods();
            for (Method method : methods) {
                if(method.isAnnotationPresent(JpaQuery.class)) {
                    new MethodHandler(method, applicationContext.getBean(SqlSessionTemplate.class).getConfiguration()).parse();
                }
            }
        }
    }
}
