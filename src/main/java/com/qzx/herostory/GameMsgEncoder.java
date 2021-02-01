package com.qzx.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.qzx.herostory.msg.GameMsgProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Auther: qzx
 * @Date: 2021/2/1 - 02 - 01 - 19:13
 * @Description: 消息编码器
 * @version: 1.0
 */
public class GameMsgEncoder extends ChannelOutboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameMsgEncoder.class);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if(ctx==null||msg==null) return;
        if(!(msg instanceof GeneratedMessageV3)){
            super.write(ctx, msg, promise);
            return;
        }

        try {
            int msgCode = -1;// 消息类型
            GeneratedMessageV3 result = (GeneratedMessageV3) msg;
            if(msg instanceof GameMsgProtocol.UserEntryResult){
                // 入场返回消息
                msgCode = GameMsgProtocol.MsgCode.USER_ENTRY_RESULT_VALUE;
            }else if(msg instanceof GameMsgProtocol.WhoElseIsHereResult){
                // 谁还在消息
                msgCode = GameMsgProtocol.MsgCode.WHO_ELSE_IS_HERE_RESULT_VALUE;
            } else{
                LOGGER.error("无法识别的消息: msg:{}",msg);
                return;
            }
            byte[] content = result.toByteArray();// 消息体
            // 包装成ByteBuf
            ByteBuf buffer = ctx.alloc().buffer();
            buffer.writeShort((short)content.length);// 设置消息长度
            buffer.writeShort((short)msgCode);// 设置消息编号
            buffer.writeBytes(content);// 设置消息体
            // 封装为BinaryWebSocketFrame发送给客户端
            BinaryWebSocketFrame webSocketFrame = new BinaryWebSocketFrame(buffer);
            ctx.writeAndFlush(webSocketFrame);
        }catch (Exception e){
            LOGGER.error(e.getMessage(),e);
        }
    }
}
