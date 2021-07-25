package com.kfyty.mybatis.auto.mapper.listener;

import com.github.pagehelper.PageInterceptor;
import com.kfyty.mybatis.auto.mapper.annotation.AutoMapper;
import com.kfyty.mybatis.auto.mapper.handle.MethodHandler;
import com.kfyty.support.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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
public class MybatisAutoMapperListener implements ApplicationContextAware, InitializingBean {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
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
                Arrays.stream(methods).filter(e -> e.isAnnotationPresent(AutoMapper.class)).forEach(e -> methodHandler.setHandleData(mapperInterface, e, configuration).doResolve());
            }
            if(configuration.getInterceptors().stream().noneMatch(e -> PageInterceptor.class.isAssignableFrom(e.getClass()))) {
                configuration.addInterceptor(applicationContext.getBean(PageInterceptor.class));
            }
        }
    }
}
