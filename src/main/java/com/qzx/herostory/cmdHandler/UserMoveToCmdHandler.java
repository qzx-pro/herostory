package com.qzx.herostory.cmdHandler;

import com.qzx.herostory.BroadCaster;
import com.qzx.herostory.msg.GameMsgProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

/**
 * @author qiaozixin
 * @Date: 2021/2/2 - 02 - 02 - 19:03
 * @Description: com.qzx.herostory.cmdHandler
 * @version: 1.0
 */
public class UserMoveToCmdHandler implements ICmdHandler<GameMsgProtocol.UserMoveToCmd> {

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, GameMsgProtocol.UserMoveToCmd msg) {
        // 用户移动消息,构建UserMoveToResult消息
        GameMsgProtocol.UserMoveToResult.Builder newBuilder = GameMsgProtocol.UserMoveToResult.newBuilder();
        Integer userId = (Integer) channelHandlerContext.channel().attr(AttributeKey.valueOf("userId")).get();

        if (userId == null) return;

        newBuilder.setMoveUserId(userId);
        newBuilder.setMoveToPosX(msg.getMoveToPosX());
        newBuilder.setMoveToPosY(msg.getMoveToPosY());

        // 广播消息
        BroadCaster.broadcast(newBuilder.build());
    }
}
