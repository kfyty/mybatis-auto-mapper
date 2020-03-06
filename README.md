# mybatis-auto-mapper
mybatis 扩展包，只需引入该依赖，无需任何附加配置，即可根据接口方法名自动查询

下面演示一下：
首先建立一个普通的 springboot 项目，项目结构如下：
![image](image/project-struct.PNG)

添加依赖如下，第三个就是本项目了：
![image](image/pom.PNG)

实体类：
![image](image/po.PNG)

Mapper 接口：
(注意：由于没有 Mapper.xml 文件，所以需要也只需要添加 @AutoMapper 注解)
![image](image/mapper.PNG)

启动类：
![image](image/boot.PNG)
配置数据源后启动即可，测试结果请查看我的博客：
https://blog.csdn.net/kfyty725/article/details/102979010

结束！

有兴趣的可以 clone 试试哦！

PS：更多语法请参照项目中的 SQLConditionEnum.java 文件(编写方法实例请参照 MybatisAutoMapperTest.java 文件)
