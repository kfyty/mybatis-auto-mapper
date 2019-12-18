package com.kfyty.mybatis.jpa.support.handle;

import com.kfyty.mybatis.jpa.support.configure.MapperMethodConfiguration;
import org.apache.ibatis.builder.IncompleteElementException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.xml.XMLStatementBuilder;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;

/**
 * 功能描述: 方法处理器，解析单独的 Mapper 标签，并放入全局配置
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/6 16:57
 * @since JDK 1.8
 */
public class MethodHandler {
    private Method method;
    private Configuration configuration;

    public MethodHandler(Method method, Configuration configuration) {
        this.method = method;
        this.configuration = configuration;
    }

    public MethodHandler parse() {
        MapperMethodConfiguration mapperMethodConfiguration = new MapperMethodConfiguration(method);
        MapperHandler mapperHandler = new MapperHandler(mapperMethodConfiguration).parse();
        MapperBuilderAssistant mapperBuilderAssistant = new MapperBuilderAssistant(configuration, mapperHandler.getMapperXml());
        mapperBuilderAssistant.setCurrentNamespace(mapperMethodConfiguration.getMapperInterface().getName());
        XNode xNode = new XPathParser(mapperHandler.getMapperXml()).evalNode(mapperHandler.getMapperXmlLabel());
        XMLStatementBuilder xmlStatementBuilder = new XMLStatementBuilder(configuration, mapperBuilderAssistant, xNode, configuration.getDatabaseId());
        try {
            xmlStatementBuilder.parseStatementNode();
        } catch (IncompleteElementException e) {
            configuration.addIncompleteStatement(xmlStatementBuilder);
        }
        return this;
    }
}
