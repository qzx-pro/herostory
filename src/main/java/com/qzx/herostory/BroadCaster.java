package com.qzx.herostory;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @Auther: qzx
 * @Date: 2021/2/2 - 02 - 02 - 18:38
 * @Description: com.qzx.herostory
 * @version: 1.0
 */
public final class BroadCaster {
    /**
     * 客户端信道数组, 一定要使用 static, 否则无法实现群发
     */
    private static final ChannelGroup CHANNELS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 私有化构造方法
     */
    private BroadCaster() {
    }

    /**
     * 功能描述: 添加channel到ChannelGroup中
     *
     * @param: 待添加的channel
     * @return: void
     */
    public static void addChannel(Channel channel) {
        if (channel == null) {
            return;
        }

        CHANNELS.add(channel);
    }

    /**
     * 功能描述: 从ChannelGroup中移除channel
     *
     * @param: 待移除的channel
     * @return: void
     */
    public static void removeChannel(Channel channel) {
        if (channel == null) {
            return;
        }

        CHANNELS.remove(channel);
    }

    /**
     * 功能描述 ：广播消息o
     *
     * @param: 待广播的消息
     * @return: void
     */
    public static void broadcast(Object o) {
        if (o == null) {
            return;
        }

        CHANNELS.writeAndFlush(o);
    }
}
