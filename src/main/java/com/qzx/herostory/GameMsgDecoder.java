package com.qzx.herostory;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: qzx
 * @Date: 2021/2/1 - 02 - 01 - 18:15
 * @Description: com.qzx.herostory
 * @version: 1.0
 */
public class GameMsgDecoder extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMsgDecoder.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (ctx == null || msg == null) {
            return;
        }
        if (!(msg instanceof BinaryWebSocketFrame)) {
            return;
        }

        try {
            BinaryWebSocketFrame webSocketFrame = (BinaryWebSocketFrame) msg;
            // 获取消息内容
            ByteBuf content = webSocketFrame.content();

            // 先读取消息长度
            content.readShort();
            // 再读取消息编号,代表了消息类型
            int msgCode = content.readShort();
            byte[] msgBody = new byte[content.readableBytes()];
            // 读取消息体
            content.readBytes(msgBody);

            // 获取消息构建器
            Message.Builder builder = GameMsgRecognizer.getMsgBuilderByMsgCode(msgCode);
            if (builder == null) {
                return;
            }

            // 构建消息
            builder.mergeFrom(msgBody);
            Message message = builder.build();

            if (message != null) {
                // 处理完成后，传递该消息给下一个处理器
                ctx.fireChannelRead(message);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
