package com.qzx.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.qzx.herostory.msg.GameMsgProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class GameMsgHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameMsgHandler.class);
    /**
     * 客户端信道数组, 一定要使用 static, 否则无法实现群发
     */
    private static final ChannelGroup CHANNELS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    /**
     * 用户字典，存储所有已经登陆的用户信息
     */
    private static ConcurrentHashMap<Integer,User> USER_MAP = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 将当前channel加入到group中进行管理
        CHANNELS.add(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        if(channelHandlerContext==null||o==null) return;
        if(!(o instanceof GeneratedMessageV3)) return; // 不是protobuf类型的消息返回

        try {
            if(o instanceof GameMsgProtocol.UserEntryCmd){
                // 入场消息
                int userId = ((GameMsgProtocol.UserEntryCmd) o).getUserId();
                String heroAvatar = ((GameMsgProtocol.UserEntryCmd) o).getHeroAvatar();
                // 存储该登陆用户
                USER_MAP.putIfAbsent(userId,new User(userId,heroAvatar));
                // 构建一个UserEntryResult对象
                GameMsgProtocol.UserEntryResult.Builder builder = GameMsgProtocol.UserEntryResult.newBuilder();
                builder.setUserId(userId);
                builder.setHeroAvatar(heroAvatar);
                GameMsgProtocol.UserEntryResult userEntryResult = builder.build();
                // 将该消息进行广播
                CHANNELS.writeAndFlush(userEntryResult);
            }else if(o instanceof GameMsgProtocol.WhoElseIsHereCmd){
                // 谁还在消息
                // 构建一个WhoElseIsHereResult消息进行返回
                GameMsgProtocol.WhoElseIsHereResult.Builder builder = GameMsgProtocol.WhoElseIsHereResult.newBuilder();
                // 在WhoElseIsHereResult消息中将所有用户字典的用户添加到UserInfo中
                for (User user : USER_MAP.values()) {
                    if(user==null) continue;
                    GameMsgProtocol.WhoElseIsHereResult.UserInfo.Builder userInfoBuilder = GameMsgProtocol.WhoElseIsHereResult.UserInfo.newBuilder();
                    userInfoBuilder.setUserId(user.getUserId());
                    userInfoBuilder.setHeroAvatar(user.getHeroAvatar());
                    builder.addUserInfo(userInfoBuilder);
                }
                // 返回消息(无需广播)
                channelHandlerContext.writeAndFlush(builder.build());
            }
        }catch (Exception e){
            LOGGER.error(e.getMessage(),e);
        }

        LOGGER.info("收到消息：msg:{}",o);
    }
}
