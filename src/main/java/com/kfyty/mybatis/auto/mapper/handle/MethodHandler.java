package com.kfyty.mybatis.auto.mapper.handle;

import com.kfyty.mybatis.auto.mapper.configure.MapperMethodConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.builder.xml.XMLStatementBuilder;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

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
    private final MapperHandler mapperHandler;

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
            this.database = connection.getMetaData().getDatabaseProductName().toLowerCase();
        } catch (SQLException e) {
            log.error("Load database product type failed !", e);
        }
        return this;
    }

    public void doResolve() {
        MapperMethodConfiguration mapperMethodConfiguration = new MapperMethodConfiguration(childInterface, method, database);
        this.mapperHandler.setMapperMethodConfiguration(mapperMethodConfiguration).doResolve();
        if (mapperHandler.supportMapperNode()) {
            this.parseMapperNode(mapperHandler.getMapperXml(), this.configuration);
        } else  {
            this.parseMapperLabel(this.childInterface, mapperHandler.getMapperXml(), mapperHandler.getMapperNodeType(), this.configuration);
        }
        this.mapperHandler.resetMapperXml();
    }

    public void parseMapperNode(String xml, Configuration configuration) {
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(inputStream, configuration, xml, new HashMap<>());
        xmlMapperBuilder.parse();
        if (log.isDebugEnabled()) {
            log.debug("Auto mapper label for method:\r\n[{}]", method);
            log.debug("Auto mapper label:\r\n[{}]", xml);
        }
    }

    public void parseMapperLabel(Class<?> namespace, String xml, String nodeType, Configuration configuration) {
        MapperBuilderAssistant mapperBuilderAssistant = new MapperBuilderAssistant(configuration, xml);
        mapperBuilderAssistant.setCurrentNamespace(namespace.getName());
        XNode xNode = new XPathParser(xml).evalNode(nodeType);
        XMLStatementBuilder xmlStatementBuilder = new XMLStatementBuilder(configuration, mapperBuilderAssistant, xNode, configuration.getDatabaseId());
        xmlStatementBuilder.parseStatementNode();
        if (log.isDebugEnabled()) {
            log.debug("Auto mapper label for method:\r\n[{}]", method);
            log.debug("Auto mapper label:\r\n[{}]", xml);
        }
    }
}
