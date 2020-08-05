package com.wyd.sqlSession;

import com.wyd.config.BoundSql;
import com.wyd.pojo.Configuration;
import com.wyd.pojo.MappedStatement;
import com.wyd.utils.GenericTokenParser;
import com.wyd.utils.ParameterMapping;
import com.wyd.utils.ParameterMappingTokenHandler;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

public class SimpleExecutor implements  Executor{

    /**
     * @param configuration
     * @param mappedStatement
     * @param params 客户端传来的对象，存放了参数值，用来填充占位符
     * @param <E>
     * @return
     * @throws Exception
     */
    @Override
    public <E> List<E> query(Configuration configuration, MappedStatement mappedStatement, Object... params) throws Exception {
        // 1 注册驱动，获取连接
        Connection connection = configuration.getDataSource().getConnection();
        // 2 获取 sql 语句：select * from user where id = #{id} and username = #{username}
        String sql = mappedStatement.getSql();
        // 3 转换 sql 语句：select * from user where id = ？ and username = ？,转换过程中，需要对 #{} 的值进行解析存储
        BoundSql boundSql = getBoundSql(sql);
        // 4 获取预处理对象 preparedStatement
        PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getSqlText());
        // 5 设置参数，为占位符赋值
        // 5.1 获取参数全路径
        String parameterType = mappedStatement.getParameterType();
        // 5.2 根据反射得到 class 对象
        Class<?> parameterTypeClass = getClassType(parameterType);
        // 存放 #{} 解析出来的参数名称
        List<ParameterMapping> parameterMappingList = boundSql.getParameterMappingList();
        for (int i = 0; i < parameterMappingList.size(); i++) {
            ParameterMapping parameterMapping = parameterMappingList.get(i);
            // 参数名称，如 id
            String content = parameterMapping.getContent();
            // 5.3 根绝参数名称获取 属性， 如：java.lang.Integer com.wyd.pojo.User.id
            Field declaredField = parameterTypeClass.getDeclaredField(content);
            // 5.4 暴力访问
            declaredField.setAccessible(true);
            // 5.5 获取属性值，如 1
            Object o = declaredField.get(params[0]);
            preparedStatement.setObject(i+1,o);
        }
        // 6 执行 sql
        ResultSet resultSet = preparedStatement.executeQuery();
        String resultType = mappedStatement.getResultType();
        Class<?> resultTypeClass = getClassType(resultType);

        ArrayList<Object> objects = new ArrayList<>();

        // 7 封装返回结果集
        while(resultSet.next()){
            Object o = resultTypeClass.getDeclaredConstructor().newInstance();
            // 元数据
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                // 字段名
                String columnName = metaData.getColumnName(i);
                // 字段的值
                Object value = resultSet.getObject(columnName);
                // 使用反射或内省，根据数据库表和实体的对应关系，完成封装
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(columnName, resultTypeClass);
                Method writeMethod = propertyDescriptor.getWriteMethod();
                writeMethod.invoke(o,value);
            }
            objects.add(o); 
        }
        return (List<E>) objects;
    }

    private Class<?> getClassType(String parameterType) throws ClassNotFoundException {
        if(parameterType != null){
            Class<?> aClass = Class.forName(parameterType);
            return aClass;
        }
        return null;
    }

    /**
     * 完成对 #{} 的解析：1 用？代替#{} 2 存储 #{} 里的值
     * @param sql
     * @return
     */
    private BoundSql getBoundSql(String sql) {
        // 标记处理类：配置标记解析器来完成对占位符的解析处理工作
        ParameterMappingTokenHandler parameterMappingTokenHandler = new ParameterMappingTokenHandler();
        GenericTokenParser genericTokenParser = new GenericTokenParser("#{", "}", parameterMappingTokenHandler);
        // 解析出来的 sql
        String parseSql = genericTokenParser.parse(sql);
        // #{} 解析出来的参数名称
        List<ParameterMapping> parameterMappings = parameterMappingTokenHandler.getParameterMappings();
        BoundSql boundSql = new BoundSql(parseSql, parameterMappings);
        return boundSql;
    }
}
