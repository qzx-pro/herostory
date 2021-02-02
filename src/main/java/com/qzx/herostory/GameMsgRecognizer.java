package com.qzx.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import com.qzx.herostory.msg.GameMsgProtocol;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: qzx
 * @Date: 2021/2/2 - 02 - 02 - 19:56
 * @Description: 消息识别器
 * @version: 1.0
 */
public final class GameMsgRecognizer {
    /**
     * msgCode->消息对象字典
     */
    private static final ConcurrentHashMap<Integer, GeneratedMessageV3> MSGCODE_MESSAGE_MAP = new ConcurrentHashMap<>();
    /**
     * GeneratedMessageV3消息字节码->msgCode
     */
    private static final ConcurrentHashMap<Class<?>,Integer> CLAZZ_MSGCODE_MAP = new ConcurrentHashMap<>();
    /**
     * 私有化构造方法
     */
    private GameMsgRecognizer(){}

    static {
        MSGCODE_MESSAGE_MAP.putIfAbsent(GameMsgProtocol.MsgCode.USER_ENTRY_CMD_VALUE,GameMsgProtocol.UserEntryCmd.getDefaultInstance());
        MSGCODE_MESSAGE_MAP.putIfAbsent(GameMsgProtocol.MsgCode.WHO_ELSE_IS_HERE_CMD_VALUE,GameMsgProtocol.WhoElseIsHereCmd.getDefaultInstance());
        MSGCODE_MESSAGE_MAP.putIfAbsent(GameMsgProtocol.MsgCode.USER_MOVE_TO_CMD_VALUE,GameMsgProtocol.UserMoveToCmd.getDefaultInstance());

        CLAZZ_MSGCODE_MAP.putIfAbsent(GameMsgProtocol.UserEntryResult.class,GameMsgProtocol.MsgCode.USER_ENTRY_RESULT_VALUE);
        CLAZZ_MSGCODE_MAP.putIfAbsent(GameMsgProtocol.WhoElseIsHereResult.class,GameMsgProtocol.MsgCode.WHO_ELSE_IS_HERE_RESULT_VALUE);
        CLAZZ_MSGCODE_MAP.putIfAbsent(GameMsgProtocol.UserMoveToResult.class,GameMsgProtocol.MsgCode.USER_MOVE_TO_RESULT_VALUE);
    }

    /**
     * 根据消息类型获取对应的消息构建器
     * @param msgCode 消息类型
     * @return Message.Builder 消息构建器
     */
    public static Message.Builder getMsgBuilderByMsgCode(int msgCode){
        if(msgCode<0) return null;

        return MSGCODE_MESSAGE_MAP.get(msgCode).newBuilderForType();
    }

    /**
     * 根据消息字节码获取对应的消息类型
     * @param clazz 消息字节码
     * @return msgCode
     */
    public static int getMsgCodeByMsgClazz(Class<?> clazz){
        if(clazz==null) return -1;
        return CLAZZ_MSGCODE_MAP.get(clazz);
    }
}
