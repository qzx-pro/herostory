package com.qzx.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.qzx.herostory.model.UserManager;
import com.qzx.herostory.msg.GameMsgProtocolLogin;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qiaozixin
 */
public class GameMsgHandler extends SimpleChannelInboundHandler<Object> {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMsgHandler.class);

    /**
     * 用户加入逻辑
     *
     * @param ctx ChannelHandlerContext
     * @throws Exception Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (ctx == null) {
            return;
        }

        // 将当前channel加入到group中进行管理
        BroadCaster.addChannel(ctx.channel());
    }

    /**
     * 用户离线逻辑
     *
     * @param ctx ChannelHandlerContext
     * @throws Exception Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        if (ctx == null) {
            return;
        }

        // 移除用户channel
        BroadCaster.removeChannel(ctx.channel());

        // 处理用户离线逻辑
        Integer userId = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        if (userId == null) {
            return;
        }

        // 将该用户从用户字典中移除
        UserManager.removeByUserId(userId);

        // 构建UserQuitResult消息
        GameMsgProtocolLogin.UserQuitResult.Builder newBuilder = GameMsgProtocolLogin.UserQuitResult.newBuilder();
        newBuilder.setQuitUserId(userId);

        // 群发该用户离线的消息
        BroadCaster.broadcast(newBuilder.build());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        if (channelHandlerContext == null || msg == null) {
            return;
        }
        if (!(msg instanceof GeneratedMessageV3)) {
            return; // 不是protobuf类型的消息返回
        }

        //处理命令
        MainMsgHandler.process(channelHandlerContext, msg);

        LOGGER.info("收到消息：msg:{}", msg);
    }

}
