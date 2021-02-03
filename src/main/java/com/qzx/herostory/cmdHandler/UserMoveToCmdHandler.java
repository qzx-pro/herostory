package com.qzx.herostory.cmdHandler;

import com.qzx.herostory.BroadCaster;
import com.qzx.herostory.model.MoveState;
import com.qzx.herostory.model.User;
import com.qzx.herostory.model.UserManager;
import com.qzx.herostory.msg.GameMsgProtocolLogin;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

/**
 * @author qiaozixin
 * @Date: 2021/2/2 - 02 - 02 - 19:03
 * @Description: com.qzx.herostory.cmdHandler
 * @version: 1.0
 */
public class UserMoveToCmdHandler implements ICmdHandler<GameMsgProtocolLogin.UserMoveToCmd> {

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, GameMsgProtocolLogin.UserMoveToCmd msg) {
        // 用户移动消息,构建UserMoveToResult消息
        GameMsgProtocolLogin.UserMoveToResult.Builder newBuilder = GameMsgProtocolLogin.UserMoveToResult.newBuilder();

        // 获取用户id
        Integer userId = (Integer) channelHandlerContext.channel().attr(AttributeKey.valueOf("userId")).get();

        if (userId == null) {
            return;
        }

        // 获取用户的移动状态
        User user = UserManager.getUserByUserId(userId);

        if (user == null) {
            return;
        }

        MoveState userMoveState = user.getMoveState();
        // 设置用户的移动状态，目的是为了在收到WhoElseIsHereCmd命令的时候，将其带回同步自己的位置
        long startTime = System.currentTimeMillis();
        userMoveState.setStartTime(startTime);
        userMoveState.setFromPosX(msg.getMoveFromPosX());
        userMoveState.setFromPosY(msg.getMoveFromPosY());
        userMoveState.setToPosX(msg.getMoveToPosX());
        userMoveState.setToPosY(msg.getMoveToPosY());

        newBuilder.setMoveUserId(userId);
        // 记录移动的起始位置
        newBuilder.setMoveFromPosX(msg.getMoveFromPosX());
        newBuilder.setMoveFromPosY(msg.getMoveFromPosY());
        newBuilder.setMoveToPosX(msg.getMoveToPosX());
        newBuilder.setMoveToPosY(msg.getMoveToPosY());
        // 记录移动的开始时间
        newBuilder.setMoveStartTime(startTime);

        // 广播消息
        BroadCaster.broadcast(newBuilder.build());
    }
}
