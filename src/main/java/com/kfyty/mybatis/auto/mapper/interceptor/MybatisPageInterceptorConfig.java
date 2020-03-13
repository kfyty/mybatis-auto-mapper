package com.kfyty.mybatis.auto.mapper.interceptor;

import com.github.pagehelper.PageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

/**
 * 功能描述: mybatis 分页插件配置
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/14 19:48
 * @since JDK 1.8
 */
public class MybatisPageInterceptorConfig {
    @Autowired(required = false)
    @Qualifier("pageInterceptorProperties")
    private Properties properties;

    @Bean
    @ConditionalOnMissingBean(PageInterceptor.class)
    public PageInterceptor createPageInterceptor() {
        PageInterceptor pageInterceptor = new PageInterceptor();
        if(this.properties != null) {
            pageInterceptor.setProperties(properties);
        }
        return pageInterceptor;
    }
}
