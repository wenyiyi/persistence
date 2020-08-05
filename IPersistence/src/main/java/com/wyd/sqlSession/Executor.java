package com.wyd.sqlSession;

import com.wyd.pojo.Configuration;
import com.wyd.pojo.MappedStatement;

import java.sql.SQLException;
import java.util.List;

public interface Executor {

    <E> List<E> query(Configuration configuration, MappedStatement mappedStatement, Object... params) throws SQLException, ClassNotFoundException, Exception;

}
