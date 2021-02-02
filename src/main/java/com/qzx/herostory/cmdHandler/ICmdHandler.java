package com.qzx.herostory.cmdHandler;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;

/**
 * @Auther: qzx
 * @Date: 2021/2/2 - 02 - 02 - 19:12
 * @Description: com.qzx.herostory.cmdHandler
 * @version: 1.0
 */
public interface ICmdHandler<T extends GeneratedMessageV3> {
    /**
    * 功能描述 : 命令处理逻辑
    * @param: channle处理器上下文和待处理消息
    * @return: void
    */
    void handle(ChannelHandlerContext channelHandlerContext, T msg);
}
