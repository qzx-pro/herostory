package com.qzx.herostory.login;

import com.qzx.herostory.MySqlSessionFactory;
import com.qzx.herostory.async.AsyncOperationProcessor;
import com.qzx.herostory.async.IAsyncOperation;
import com.qzx.herostory.login.db.IUserDao;
import com.qzx.herostory.login.db.UserEntity;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

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
     */
    public void login(String userName, String password, Function<UserEntity, Void> callback) {
        if (userName == null) {
            LOGGER.error("userName为空");
            return;
        }

        IAsyncOperation asyncOperation = new AsyncLogin(userName, password) {
            @Override
            public int getBindId() {
                // 取最后一个字符作为选择的线程id
                int bindId = userName.charAt(userName.length() - 1);
                LOGGER.info("获取bindId: " + bindId);
                return bindId;
            }

            @Override
            public void doFinish() {
                if (callback != null) {
                    LOGGER.info("执行登录成功后回调函数，当前线程为：" + Thread.currentThread().getName());
                    callback.apply(this.getUserEntity());
                }
            }
        };

        AsyncOperationProcessor.getInstance().process(asyncOperation);
    }

    private static class AsyncLogin implements IAsyncOperation {
        /**
         * 日志对象
         */
        private static final Logger LOGGER = LoggerFactory.getLogger(AsyncLogin.class);
        /**
         * 用户名
         */
        String username;

        /**
         * 密码
         */
        String password;

        /**
         * 结果对象
         */
        UserEntity userEntity;

        AsyncLogin(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public UserEntity getUserEntity() {
            return userEntity;
        }

        /**
         * 执行异步登录操作
         */
        @Override
        public void doAsync() {
            try (SqlSession session = MySqlSessionFactory.getConnection()) {
                if (session == null) {
                    LOGGER.error("获取连接失败");
                    return;
                }

                IUserDao iUserDao = session.getMapper(IUserDao.class);

                LOGGER.info("开始执行登录IO操作，当前线程为：" + Thread.currentThread().getName());

                if (iUserDao == null) {
                    LOGGER.error("iUserDao为空");
                    return;
                }

                UserEntity userEntity = iUserDao.getUserByName(this.username);

                if (userEntity == null) {
                    LOGGER.info("用户不存在，开始创建用户");

                    userEntity = new UserEntity();
                    userEntity.setUserName(this.username);
                    userEntity.setPassword(this.password);
                    userEntity.setHeroAvatar("Hero_Shaman");
                    iUserDao.insertInto(userEntity);
                    session.commit();

                    LOGGER.info("创建成功，并开始登陆");
                } else {
                    if (!userEntity.getPassword().equals(this.password)) {
                        LOGGER.error("密码不正确");
                        return;
                    }
                }
                this.userEntity = userEntity;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
