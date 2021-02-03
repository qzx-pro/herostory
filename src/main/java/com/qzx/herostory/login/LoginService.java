package com.qzx.herostory.login;

import com.qzx.herostory.MySqlSessionFactory;
import com.qzx.herostory.login.db.IUserDao;
import com.qzx.herostory.login.db.UserEntity;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: qzx
 * @date: 2021/2/7 - 02 - 07 - 15:42
 * @description: com.qzx.herostory.login.db
 * @version: 1.0
 */
public class LoginService {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);

    /**
     * 单例对象
     */
    private static final LoginService LOGIN_SERVICE = new LoginService();

    /**
     * 私有化构造方法
     */
    private LoginService() {

    }

    /**
     * 获取LoginService对象
     *
     * @return LoginService对象
     */
    public static LoginService getInstance() {
        return LOGIN_SERVICE;
    }

    /**
     * 根据用户名获取用户对象
     *
     * @param userName 用户名
     * @param password 登陆密码
     * @return 用户对象
     */
    public UserEntity login(String userName, String password) {
        if (userName == null) {
            LOGGER.error("userName为空");
            return null;
        }
        UserEntity userEntity = null;
        try (SqlSession session = MySqlSessionFactory.getConnection()) {
            if (session == null) {
                LOGGER.error("获取连接失败");
                return null;
            }

            IUserDao iUserDao = session.getMapper(IUserDao.class);

            if (iUserDao == null) {
                LOGGER.error("iUserDao为空");
                return null;
            }
            userEntity = iUserDao.getUserByName(userName);

            if (userEntity == null) {
                LOGGER.info("用户不存在，开始创建用户");

                userEntity = new UserEntity();
                userEntity.setUserName(userName);
                userEntity.setPassword(password);
                userEntity.setHeroAvatar("Hero_Shaman");
                iUserDao.insertInto(userEntity);
                session.commit();

                LOGGER.info("创建成功，并开始登陆");
            } else {
                if (!userEntity.getPassword().equals(password)) {
                    LOGGER.error("密码不正确");
                    return null;
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return userEntity;
    }


}
