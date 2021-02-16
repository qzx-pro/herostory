package com.qzx.herostory;

import com.qzx.herostory.mq.MyConsumer;
import com.qzx.herostory.util.RedisUtil;

/**
 * @author: qzx
 * @date: 2021/2/16 - 02 - 16 - 20:09
 * @description: 排行榜进程
 * @version: 1.0
 */
public class RankApp {
    public static void main(String[] args) {
        // 初始化redis
        RedisUtil.init();
        // 初始化消息队列(消费者)
        MyConsumer.init();
    }
}
