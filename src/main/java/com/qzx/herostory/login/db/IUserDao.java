package com.qzx.herostory.login.db;

import org.apache.ibatis.annotations.Param;

/**
 * @author: qzx
 * @date: 2021/2/7 - 02 - 07 - 15:39
 * @description: com.qzx.herostory.login.db
 * @version: 1.0
 */
public interface IUserDao {
    /**
     * 根据用户名称获取用户实体
     *
     * @param userName 用户名称
     * @return 用户实体
     */
    UserEntity getUserByName(@Param("userName") String userName);

    /**
     * 添加用户实体
     *
     * @param newUserEntity 用户实体
     */
    void insertInto(UserEntity newUserEntity);
}
