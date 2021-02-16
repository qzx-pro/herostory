package com.qzx.herostory.mq;

import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: qzx
 * @date: 2021/2/16 - 02 - 16 - 17:15
 * @description: 消息生产者
 * @version: 1.0
 */
public final class MyProducer {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MyProducer.class);

    /**
     * 生产者
     */
    private static DefaultMQProducer producer = null;

    /**
     * 私有化构造方法
     */
    private MyProducer() {

    }

    /**
     * 初始化消息队列（生产者）
     */
    public static void init() {
        try {
            // 创建生产者
            producer = new DefaultMQProducer("herostory");
            // 设置nameServer
            producer.setNamesrvAddr("192.168.221.139:9876");
            // 启动生产者
            producer.start();
            // 设置发送消息重试次数
            producer.setRetryTimesWhenSendAsyncFailed(3);

            LOGGER.info("消息队列(生产者)启动成功!");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 发送消息
     *
     * @param topic 指定的主题
     * @param msg   消息实体
     */
    public static void sendMsg(String topic, Object msg) {
        if (topic == null || msg == null) {
            return;
        }

        try {
            final Message message = new Message();
            message.setTopic(topic);
            message.setBody(JSONObject.toJSONBytes(msg));

            if (producer == null) {
                LOGGER.error("生产者未初始化!");
                return;
            }

            producer.send(message);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
