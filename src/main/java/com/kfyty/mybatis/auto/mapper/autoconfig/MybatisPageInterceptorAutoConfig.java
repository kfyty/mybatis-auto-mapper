package com.kfyty.mybatis.auto.mapper.autoconfig;

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
public class MybatisPageInterceptorAutoConfig {
    @Autowired(required = false)
    @Qualifier("pageInterceptorProperties")
    private Properties properties;

    @Bean
    @ConditionalOnMissingBean(PageInterceptor.class)
    public PageInterceptor createPageInterceptor() {
        if (this.properties == null) {
            this.properties = new Properties();
            this.properties.setProperty("supportMethodsArguments", "true");
        }
        PageInterceptor pageInterceptor = new PageInterceptor();
        pageInterceptor.setProperties(this.properties);
        return pageInterceptor;
    }
}
