package com.qzx.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.qzx.herostory.cmdHandler.*;
import com.qzx.herostory.msg.GameMsgProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameMsgHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameMsgHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (ctx == null) return;

        // 将当前channel加入到group中进行管理
        BroadCaster.addChannel(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        if (ctx == null) return;

        // 移除用户channel
        BroadCaster.removeChannel(ctx.channel());

        // 处理用户离线逻辑
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        if (userId == null) return;

        // 将该用户从用户字典中移除
        UserManager.removeByUserId(userId);

        // 构建UserQuitResult消息
        GameMsgProtocol.UserQuitResult.Builder newBuilder = GameMsgProtocol.UserQuitResult.newBuilder();
        newBuilder.setQuitUserId(userId);

        // 群发该用户离线的消息
        BroadCaster.broadcast(newBuilder.build());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        if (channelHandlerContext == null || msg == null) return;
        if (!(msg instanceof GeneratedMessageV3)) return; // 不是protobuf类型的消息返回

        try {
            ICmdHandler<? extends GeneratedMessageV3> cmdHandler = CmdHandlerFactory.createCmdHandler(msg.getClass());

            if (null == cmdHandler) {
                LOGGER.error(
                        "未找到相对应的命令处理器, msgClazz = {}",
                        msg.getClass().getName()
                );
                return;
            }
            // 解析命令
            cmdHandler.handle(channelHandlerContext,cast(msg));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        LOGGER.info("收到消息：msg:{}", msg);
    }

    private <T extends GeneratedMessageV3> T cast(Object msg){
        if(!(msg instanceof GeneratedMessageV3)) return null;
        return (T)msg;
    }
}
