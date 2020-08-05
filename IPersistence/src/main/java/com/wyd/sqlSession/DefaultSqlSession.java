package com.wyd.sqlSession;

import com.wyd.pojo.Configuration;
import com.wyd.pojo.MappedStatement;

import java.lang.reflect.*;
import java.util.List;

public class DefaultSqlSession implements SqlSession {

    private Configuration configuration;

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public <E> List<E> selectList(String statementId, Object... params) throws Exception {
        // 调用 SimpleExecutor 的 query 方法
        SimpleExecutor simpleExecutor = new SimpleExecutor();
        MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementId);
        List<Object> query = simpleExecutor.query(configuration, mappedStatement, params);
        return (List<E>) query;
    }

    @Override
    public <T> T selectOne(String statementId, Object... params) throws Exception {
        List<Object> objects = selectList(statementId, params);
        if(objects.size() == 1){
            return (T) objects.get(0);
        }else{
            throw new RuntimeException("查询结果为空或返回结果过多");
        }
    }

    @Override
    public <T> T getMapper(Class<?> mapperClass) {
        // 使用 JDK 动态代理为 Dao 接口生成代理对象，并返回
        Object proxyInstance = Proxy.newProxyInstance(DefaultSqlSession.class.getClassLoader(), new Class[]{mapperClass}, (proxy, method, args) -> {
            // 底层还是去执行 JDBC 代码
            // 准备参数 statementId：sql语句的唯一标识。namespace.id = 接口全限定名.方法名
            String methodName = method.getName();
            String className = method.getDeclaringClass().getName();
            String statementId = className + "." + methodName;
            // params = args
            // 获取被调用方法的返回值类型
            Type genericReturnType = method.getGenericReturnType();
            // 判断是否进行了 泛型参数化，有就是列表，如 List<User>
            if(genericReturnType instanceof ParameterizedType){
                return selectList(statementId, args);
            }
            return selectOne(statementId, args);
        });


        return (T) proxyInstance;
    }
}
