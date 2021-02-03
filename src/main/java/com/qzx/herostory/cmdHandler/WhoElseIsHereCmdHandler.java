package com.qzx.herostory.cmdHandler;

import com.qzx.herostory.model.MoveState;
import com.qzx.herostory.model.User;
import com.qzx.herostory.model.UserManager;
import com.qzx.herostory.msg.GameMsgProtocolLogin;
import io.netty.channel.ChannelHandlerContext;

import java.util.Collection;

/**
 * @author: qzx
 * @Date: 2021/2/2 - 02 - 02 - 19:02
 * @Description: com.qzx.herostory.cmdHandler
 * @version: 1.0
 */
public class WhoElseIsHereCmdHandler implements ICmdHandler<GameMsgProtocolLogin.WhoElseIsHereCmd> {

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, GameMsgProtocolLogin.WhoElseIsHereCmd msg) {
        // 谁还在消息
        // 构建一个WhoElseIsHereResult消息进行返回
        GameMsgProtocolLogin.WhoElseIsHereResult.Builder builder = GameMsgProtocolLogin.WhoElseIsHereResult.newBuilder();
        // 在WhoElseIsHereResult消息中将所有用户字典的用户添加到UserInfo中
        Collection<User> userCollection = UserManager.getUserCollection();
        for (User user : userCollection) {

            if (user == null) {
                continue;
            }

            // 构建用户信息
            GameMsgProtocolLogin.WhoElseIsHereResult.UserInfo.Builder
                    userInfoBuilder = GameMsgProtocolLogin.WhoElseIsHereResult.UserInfo.newBuilder();
            userInfoBuilder.setUserId(user.getUserId());
            userInfoBuilder.setHeroAvatar(user.getHeroAvatar());
            userInfoBuilder.setUserName(user.getUserName());

            MoveState userMoveState = user.getMoveState();

            // 利用本地的MoveState构建UserInfo中的MoveState消息
            GameMsgProtocolLogin.WhoElseIsHereResult.UserInfo.MoveState.Builder
                    moveStateBuilder = GameMsgProtocolLogin.WhoElseIsHereResult.UserInfo.MoveState.newBuilder();
            moveStateBuilder.setFromPosX(userMoveState.getFromPosX());
            moveStateBuilder.setFromPosY(userMoveState.getFromPosY());
            moveStateBuilder.setToPosX(userMoveState.getToPosX());
            moveStateBuilder.setToPosY(userMoveState.getToPosY());
            moveStateBuilder.setStartTime(userMoveState.getStartTime());

            // 设置每一个用户的移动状态
            userInfoBuilder.setMoveState(moveStateBuilder.build());

            builder.addUserInfo(userInfoBuilder);
        }
        // 返回消息(无需广播)
        channelHandlerContext.writeAndFlush(builder.build());
    }
}
