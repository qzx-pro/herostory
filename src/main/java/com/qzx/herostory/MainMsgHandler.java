package com.qzx.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.qzx.herostory.cmdHandler.CmdHandlerFactory;
import com.qzx.herostory.cmdHandler.ICmdHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: qzx
 * @date: 2021/2/6 - 02 - 06 - 15:30
 * @description: 所有消息处理器的入口
 * @version: 1.0
 */
public final class MainMsgHandler {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MainMsgHandler.class);
    /**
     * 单例对象
     */
    private static final MainMsgHandler MSG_HANDLER = new MainMsgHandler();
    /**
     * 构建单线程线程池
     */
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            (runnable) -> {
                Thread thread = new Thread(runnable);
                // 设置线程的名字
                thread.setName("MainMsgHandler");
                return thread;
            });

    /**
     * 私有化构造方法
     */
    private MainMsgHandler() {

    }

    /**
     * 处理逻辑入口
     *
     * @param channelHandlerContext channelHandlerContext
     * @param msg                   命令
     */
    public static void process(ChannelHandlerContext channelHandlerContext, Object msg) {
        // 提交任务到线程池中执行
        EXECUTOR_SERVICE.submit(() -> {
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
                cmdHandler.handle(channelHandlerContext, cast(msg));
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
    }

    /**
     * 将GeneratedMessageV3类型的消息转化为其子类
     *
     * @param msg 接受到的消息
     * @param <T> GeneratedMessageV3子类
     * @return T
     */
    private static <T extends GeneratedMessageV3> T cast(Object msg) {
        if (!(msg instanceof GeneratedMessageV3)) {
            return null;
        }
        return (T) msg;
    }

    /**
     * 获取MainMsgHandler单例对象
     *
     * @return MainMsgHandler单例
     */
    public MainMsgHandler getInstance() {
        return MSG_HANDLER;
    }
}
