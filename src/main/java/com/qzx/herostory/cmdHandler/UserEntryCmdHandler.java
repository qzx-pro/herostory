package com.qzx.herostory.cmdHandler;

import com.google.protobuf.GeneratedMessageV3;
import com.qzx.herostory.BroadCaster;
import com.qzx.herostory.User;
import com.qzx.herostory.UserManager;
import com.qzx.herostory.msg.GameMsgProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

/**
 * @Auther: qzx
 * @Date: 2021/2/2 - 02 - 02 - 18:59
 * @Description: com.qzx.herostory.cmdHandler
 * @version: 1.0
 */
public class UserEntryCmdHandler implements ICmdHandler {

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, GeneratedMessageV3 msg){
        // 入场消息
        int userId = ((GameMsgProtocol.UserEntryCmd) msg).getUserId();
        String heroAvatar = ((GameMsgProtocol.UserEntryCmd) msg).getHeroAvatar();

        // 存储该登录用户
        UserManager.save(new User(userId, heroAvatar));

        // 记录当前用户ID到channel，相当于存储在session中
        channelHandlerContext.channel().attr(AttributeKey.valueOf("userId")).set(userId);

        // 构建一个UserEntryResult对象
        GameMsgProtocol.UserEntryResult.Builder builder = GameMsgProtocol.UserEntryResult.newBuilder();
        builder.setUserId(userId);
        builder.setHeroAvatar(heroAvatar);
        GameMsgProtocol.UserEntryResult userEntryResult = builder.build();

        // 将该消息进行广播
        BroadCaster.broadcast(userEntryResult);
    }
}
