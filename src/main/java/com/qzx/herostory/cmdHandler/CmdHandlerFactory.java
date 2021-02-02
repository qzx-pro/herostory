package com.qzx.herostory.cmdHandler;

import com.google.protobuf.GeneratedMessageV3;
import com.qzx.herostory.msg.GameMsgProtocol;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: qzx
 * @Date: 2021/2/2 - 02 - 02 - 19:23
 * @Description: com.qzx.herostory.cmdHandler
 * @version: 1.0
 */
public final class CmdHandlerFactory {
    /**
     * 命令类型->命令解析器字典
     */
    private static final ConcurrentHashMap<Class<?>,ICmdHandler<? extends GeneratedMessageV3> > CMD_HANDLER_MAP = new ConcurrentHashMap<>();
    /**
     * 私有化构造方法
     */
    private CmdHandlerFactory(){}

    static {
        CMD_HANDLER_MAP.putIfAbsent(GameMsgProtocol.UserEntryCmd.class,new UserEntryCmdHandler());
        CMD_HANDLER_MAP.putIfAbsent(GameMsgProtocol.WhoElseIsHereCmd.class,new WhoElseIsHereCmdHandler());
        CMD_HANDLER_MAP.putIfAbsent(GameMsgProtocol.UserMoveToCmd.class,new UserMoveToCmdHandler());
    }

    /**
    * 功能描述 : 创造命令对应的命令解析器
    * @param: 命令的字节码
    * @return: 命令处理器
    */
    public static ICmdHandler<? extends GeneratedMessageV3> createCmdHandler(Class<?> clazz){
        if(clazz==null) return null;

        return CMD_HANDLER_MAP.get(clazz);
    }
}
