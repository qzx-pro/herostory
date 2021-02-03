package com.qzx.herostory.cmdHandler;

import com.qzx.herostory.BroadCaster;
import com.qzx.herostory.model.User;
import com.qzx.herostory.model.UserManager;
import com.qzx.herostory.msg.GameMsgProtocolLogin;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: qzx
 * @Date: 2021/2/2 - 02 - 02 - 18:59
 * @Description: com.qzx.herostory.cmdHandler
 * @version: 1.0
 */
public class UserEntryCmdHandler implements ICmdHandler<GameMsgProtocolLogin.UserEntryCmd> {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserEntryCmdHandler.class);

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, GameMsgProtocolLogin.UserEntryCmd msg) {
        if (channelHandlerContext == null || msg == null) {
            return;
        }
        // 获取userId
        Integer userId = (Integer) channelHandlerContext.channel().attr(AttributeKey.valueOf("userId")).get();
        if (userId == null) {
            LOGGER.error("userId为空");
            return;
        }
        // 获取当前登录对象
        User user = UserManager.getUserByUserId(userId);
        if (user == null) {
            LOGGER.error("user为空");
            return;
        }

        // 构建一个UserEntryResult对象
        GameMsgProtocolLogin.UserEntryResult.Builder builder = GameMsgProtocolLogin.UserEntryResult.newBuilder();
        builder.setUserId(userId);
        builder.setHeroAvatar(user.getHeroAvatar());
        builder.setUserName(user.getUserName());
        GameMsgProtocolLogin.UserEntryResult userEntryResult = builder.build();

        // 将该消息进行广播
        BroadCaster.broadcast(userEntryResult);
    }
}
