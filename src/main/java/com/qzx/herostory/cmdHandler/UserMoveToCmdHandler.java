package com.qzx.herostory.cmdHandler;

import com.google.protobuf.GeneratedMessageV3;
import com.qzx.herostory.BroadCaster;
import com.qzx.herostory.msg.GameMsgProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

/**
 * @Auther: qzx
 * @Date: 2021/2/2 - 02 - 02 - 19:03
 * @Description: com.qzx.herostory.cmdHandler
 * @version: 1.0
 */
public class UserMoveToCmdHandler implements ICmdHandler {

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, GeneratedMessageV3 msg){
        // 用户移动消息,构建UserMoveToResult消息
        GameMsgProtocol.UserMoveToResult.Builder newBuilder = GameMsgProtocol.UserMoveToResult.newBuilder();
        Integer userId = (Integer) channelHandlerContext.channel().attr(AttributeKey.valueOf("userId")).get();

        if (userId == null) return;

        newBuilder.setMoveUserId(userId);
        GameMsgProtocol.UserMoveToCmd moveToCmd = (GameMsgProtocol.UserMoveToCmd) msg;
        newBuilder.setMoveToPosX(moveToCmd.getMoveToPosX());
        newBuilder.setMoveToPosY(moveToCmd.getMoveToPosY());

        // 广播消息
        BroadCaster.broadcast(newBuilder.build());
    }
}
