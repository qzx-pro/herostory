package com.qzx.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import com.qzx.herostory.msg.GameMsgProtocolRank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qiaozixin
 * @Date: 2021/2/2 - 02 - 02 - 19:56
 * @Description: 消息识别器
 * @version: 1.0
 */
public final class GameMsgRecognizer {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMsgRecognizer.class);
    /**
     * msgCode->消息对象字典
     */
    private static final ConcurrentHashMap<Integer, GeneratedMessageV3> MSGCODE_MESSAGE_MAP = new ConcurrentHashMap<>();
    /**
     * GeneratedMessageV3消息类->msgCode
     */
    private static final ConcurrentHashMap<Class<?>, Integer> CLAZZ_MSGCODE_MAP = new ConcurrentHashMap<>();

    /**
     * 私有化构造方法
     */
    private GameMsgRecognizer() {
    }

    /**
     * 初始化MAP
     */
    public static void init() {
        LOGGER.info("开始初始化GameMsgRecognizer");
        // 获取GameMsgProtocol所有的内部类
        Class<?>[] innerClazzArray = GameMsgProtocolRank.class.getDeclaredClasses();
        GameMsgProtocolRank.MsgCode[] msgCodes = GameMsgProtocolRank.MsgCode.values();
        for (Class<?> clazz : innerClazzArray) {
            // 如果不是消息类就跳过
            if (clazz == null ||
                    !GeneratedMessageV3.class.isAssignableFrom(clazz)
            ) {
                continue;
            }
            // 获取简单类名小写
            String clazzName = clazz.getSimpleName().toLowerCase();

            for (GameMsgProtocolRank.MsgCode msgCode : msgCodes) {
                // 将所有的下划线去除并转化为小写
                String msgCodeString = msgCode.toString().replaceAll("_", "").toLowerCase();

                if (msgCodeString.startsWith(clazzName)) {
                    // 对应上消息类型和消息类名称
                    try {
                        // 调用clazz类的getDefaultInstance方法获得对象
                        Object instance = clazz.getDeclaredMethod("getDefaultInstance").invoke(clazz);

                        // 存储Map
                        MSGCODE_MESSAGE_MAP.putIfAbsent(msgCode.getNumber(),(GeneratedMessageV3) instance);
                        CLAZZ_MSGCODE_MAP.putIfAbsent(clazz,msgCode.getNumber());
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(),e);
                    }
                }
            }

        }
        LOGGER.info("GameMsgRecognizer初始化完成");
    }

    /**
     * 根据消息类型获取对应的消息构建器
     *
     * @param msgCode 消息类型
     * @return Message.Builder 消息构建器
     */
    public static Message.Builder getMsgBuilderByMsgCode(int msgCode) {
        if (msgCode < 0) {
            return null;
        }

        return MSGCODE_MESSAGE_MAP.get(msgCode).newBuilderForType();
    }

    /**
     * 根据消息类获取对应的消息类型
     *
     * @param clazz 消息字节码
     * @return msgCode
     */
    public static int getMsgCodeByMsgClazz(Class<?> clazz) {
        if (clazz == null) {
            return -1;
        }
        Integer result = CLAZZ_MSGCODE_MAP.get(clazz);
        return result == null ? -1 : result;
    }
}
