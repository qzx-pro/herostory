package com.qzx.herostory;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author: qzx
 * @date: 2021/2/7 - 02 - 07 - 16:26
 * @description: MySql 会话工厂
 * @version: 1.0
 */
public class MySqlSessionFactory {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MySqlSessionFactory.class);
    private static SqlSessionFactory sqlSessionFactory;

    private MySqlSessionFactory() {

    }

    public static void init() {
        LOGGER.info("开始连接数据库");

        try {
            sqlSessionFactory = new SqlSessionFactoryBuilder()
                    .build(Resources.getResourceAsStream("mybatisConfig.xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info("连接数据库成功");
    }

    public static SqlSession getConnection() {

        if (sqlSessionFactory == null) {
            throw new RuntimeException("sqlSessionFactory 尚未初始化");
        }

        return sqlSessionFactory.openSession();
    }
}
