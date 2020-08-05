package com.wyd.sqlSession;

import com.wyd.config.XMLConfigBuilder;
import com.wyd.pojo.Configuration;
import org.dom4j.DocumentException;

import java.beans.PropertyVetoException;
import java.io.InputStream;

public class SqlSessionFactoryBuilder {

    public SqlSessionFactory build(InputStream in) throws PropertyVetoException, DocumentException {
        // 第一：使用 dom4j解析配置文件，将解析出来的内容封装到 Configuration 中
        XMLConfigBuilder xmlConfigBuilder = new XMLConfigBuilder();
        Configuration configuration = xmlConfigBuilder.parseConfig(in);
        // 第二：创建 SqlSessionFactory 对象，工厂类，用来生产 sqlSession，sqlSession 负责 DB 的crud
        SqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(configuration);
        return sqlSessionFactory;
    }

}
