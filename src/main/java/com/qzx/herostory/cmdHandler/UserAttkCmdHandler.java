package com.qzx.herostory.cmdHandler;

import com.qzx.herostory.msg.GameMsgProtocol;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: qzx
 * @date: 2021/2/3 - 02 - 03 - 12:02
 * @description: com.qzx.herostory.cmdHandler
 * @version: 1.0
 */
public class UserAttkCmdHandler implements ICmdHandler<GameMsgProtocol.UserAttkCmd>{
    private static Logger LOGGER = LoggerFactory.getLogger(UserAttkCmdHandler.class);

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, GameMsgProtocol.UserAttkCmd msg) {
        LOGGER.info("UserAttkCmd");
    }
}
