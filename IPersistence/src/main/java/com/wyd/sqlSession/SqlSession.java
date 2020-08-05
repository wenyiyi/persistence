package com.wyd.sqlSession;

import java.sql.SQLException;
import java.util.List;

public interface SqlSession {

    // 查询列表
    <E> List<E> selectList(String statementId, Object... params) throws Exception;

    // 根据条件查询单个
    <T> T selectOne(String statementId, Object... params) throws Exception;

    <T> T getMapper(Class<?> mapperClass);

}
