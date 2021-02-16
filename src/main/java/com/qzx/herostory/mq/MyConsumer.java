package com.qzx.herostory.mq;

import com.alibaba.fastjson.JSONObject;
import com.qzx.herostory.rank.RankService;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author: qzx
 * @date: 2021/2/16 - 02 - 16 - 20:10
 * @description: 消息队列消费者
 * @version: 1.0
 */
public class MyConsumer {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MyConsumer.class);

    /**
     * 消费者
     */
    private static DefaultMQPushConsumer consumer = null;

    /**
     * 私有化构造方法
     */
    private MyConsumer() {

    }

    /**
     * 初始化consumer
     */
    public static void init() {
        try {
            // 创建消费者
            consumer = new DefaultMQPushConsumer("herostory");
            // 设置nameserver
            consumer.setNamesrvAddr("192.168.221.139:9876");
            // 订阅topic
            consumer.subscribe("herostory_victor", "*");
            // 注册回调
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    // 消费消息并且写入redis中
                    for (MessageExt msg : msgs) {
                        VictorMsg victorMsg = JSONObject.parseObject(msg.getBody(), VictorMsg.class);
                        LOGGER.info("从消息队列中获取消息：winnerId={},loserId={} ", victorMsg.getWinnerId(), victorMsg.getLoseId());
                        RankService.getInstance().refreshRedis(victorMsg.getWinnerId(), victorMsg.getLoseId());
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            // 启动消费者
            consumer.start();
            LOGGER.info("消息队列(消费者)连接成功!");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
