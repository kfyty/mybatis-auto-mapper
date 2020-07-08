package com.kfyty.mybatis.auto.mapper.handle;

import com.kfyty.mybatis.auto.mapper.configure.MapperMethodConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.xml.XMLStatementBuilder;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 功能描述: 方法处理器，解析单独的 Mapper 标签，并放入全局配置
 *
 * @author kfyty725@hotmail.com
 * @date 2019/11/6 16:57
 * @since JDK 1.8
 */
@Slf4j
public class MethodHandler {
    private String database;
    private Class<?> childInterface;
    private Method method;
    private Configuration configuration;
    private MapperHandler mapperHandler;

    public MethodHandler() {
        this.mapperHandler = new MapperHandler();
    }

    public MethodHandler(Method method, Configuration configuration) {
        this(method.getDeclaringClass(), method, configuration);
    }

    public MethodHandler(Class<?> childInterface, Method method, Configuration configuration) {
        this();
        this.setHandleData(childInterface, method, configuration);
    }

    public MethodHandler setHandleData(Class<?> childInterface, Method method, Configuration configuration) {
        this.childInterface = childInterface;
        this.method = method;
        this.configuration = configuration;
        try(Connection connection = configuration.getEnvironment().getDataSource().getConnection()) {
            this.database = connection.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            log.error("Load database product type failed !", e);
        }
        return this;
    }

    public MethodHandler parse() {
        MapperMethodConfiguration mapperMethodConfiguration = new MapperMethodConfiguration(childInterface, method, database);
        this.mapperHandler.setMapperMethodConfiguration(mapperMethodConfiguration).parse();
        MapperBuilderAssistant mapperBuilderAssistant = new MapperBuilderAssistant(this.configuration, mapperHandler.getMapperXml());
        mapperBuilderAssistant.setCurrentNamespace(mapperMethodConfiguration.getMapperInterface().getName());
        XNode xNode = new XPathParser(mapperHandler.getMapperXml()).evalNode(mapperHandler.getMapperNodeType());
        XMLStatementBuilder xmlStatementBuilder = new XMLStatementBuilder(this.configuration, mapperBuilderAssistant, xNode, this.configuration.getDatabaseId());
        xmlStatementBuilder.parseStatementNode();
        if(log.isDebugEnabled()) {
            log.debug("Auto mapper label for method:\n[{}]", method);
            log.debug("Auto mapper label:\n[{}]", mapperHandler.getMapperXml());
        }
        return this;
    }
}
