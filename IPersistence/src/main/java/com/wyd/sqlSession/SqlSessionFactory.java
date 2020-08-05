package com.wyd.sqlSession;

public interface SqlSessionFactory {

    /**
     * 封装了 JDBC 代码
     * @return
     */
    SqlSession openSession();

}
