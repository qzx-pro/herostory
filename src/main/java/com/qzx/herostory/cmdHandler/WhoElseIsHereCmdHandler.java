package com.qzx.herostory.cmdHandler;

import com.google.protobuf.GeneratedMessageV3;
import com.qzx.herostory.User;
import com.qzx.herostory.UserManager;
import com.qzx.herostory.msg.GameMsgProtocol;
import io.netty.channel.ChannelHandlerContext;

import java.util.Collection;

/**
 * @Auther: qzx
 * @Date: 2021/2/2 - 02 - 02 - 19:02
 * @Description: com.qzx.herostory.cmdHandler
 * @version: 1.0
 */
public class WhoElseIsHereCmdHandler implements ICmdHandler {

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, GeneratedMessageV3 msg){
        // 谁还在消息
        // 构建一个WhoElseIsHereResult消息进行返回
        GameMsgProtocol.WhoElseIsHereResult.Builder builder = GameMsgProtocol.WhoElseIsHereResult.newBuilder();
        // 在WhoElseIsHereResult消息中将所有用户字典的用户添加到UserInfo中
        Collection<User> userCollection = UserManager.getUserCollection();
        for (User user : userCollection) {
            if (user == null) continue;
            GameMsgProtocol.WhoElseIsHereResult.UserInfo.Builder userInfoBuilder = GameMsgProtocol.WhoElseIsHereResult.UserInfo.newBuilder();
            userInfoBuilder.setUserId(user.getUserId());
            userInfoBuilder.setHeroAvatar(user.getHeroAvatar());
            builder.addUserInfo(userInfoBuilder);
        }
        // 返回消息(无需广播)
        channelHandlerContext.writeAndFlush(builder.build());
    }
}
