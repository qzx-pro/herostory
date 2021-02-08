package com.qzx.herostory.cmdHandler;

import com.qzx.herostory.login.LoginService;
import com.qzx.herostory.model.User;
import com.qzx.herostory.model.UserManager;
import com.qzx.herostory.msg.GameMsgProtocolLogin;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: qzx
 * @date: 2021/2/7 - 02 - 07 - 16:05
 * @description: 用户登录消息处理器
 * @version: 1.0
 */
public class UserLoginCmdHandler implements ICmdHandler<GameMsgProtocolLogin.UserLoginCmd> {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginService.class);

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, GameMsgProtocolLogin.UserLoginCmd msg) {
        if (channelHandlerContext == null || msg == null) {
            return;
        }
        // 登录
        String userName = msg.getUserName();
        String password = msg.getPassword();

        LOGGER.info("开始登录");
        LOGGER.info("开始处理登录业务逻辑，当前线程为：" + Thread.currentThread().getName());

        LoginService.getInstance().login(userName, password, (userEntity) -> {
            if (userEntity == null) {
                LOGGER.info("登录失败");
                return null;
            }
            // 存放该用户到channel中
            LOGGER.info("登录成功");

            // 创建本地User对象
            User user = new User();
            user.setUserId(userEntity.getUserId());
            user.setCurrentHp(100);
            user.setHeroAvatar(userEntity.getHeroAvatar());
            user.setUserName(userName);

            // 保存当前user对象到本地
            UserManager.save(user);

            // 将当前userId存储在channel中
            channelHandlerContext.channel().attr(AttributeKey.valueOf("userId")).set(user.getUserId());

            // 构建UserLoginResult结果对象
            GameMsgProtocolLogin.UserLoginResult.Builder newBuilder = GameMsgProtocolLogin.UserLoginResult.newBuilder();
            newBuilder.setUserId(user.getUserId());
            newBuilder.setUserName(user.getUserName());
            newBuilder.setHeroAvatar(user.getHeroAvatar());

            // 发送消息给客户端
            channelHandlerContext.writeAndFlush(newBuilder.build());
            return null;
        });
    }
}
