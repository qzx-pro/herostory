package com.qzx.herostory.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author: qzx
 * @date: 2021/2/16 - 02 - 16 - 11:02
 * @description: redis工具类
 * @version: 1.0
 */
public final class RedisUtil {
    /**
     * 单例对象
     */
    private static final RedisUtil REDIS_UTIL = new RedisUtil();
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisUtil.class);
    /**
     * jedis连接池
     */
    private static JedisPool JEDIS_POOL;

    /**
     * 私有化构造方法
     */
    private RedisUtil() {

    }

    /**
     * 获取单例对象
     *
     * @return 单例对象
     */
    public static RedisUtil getInstance() {
        return REDIS_UTIL;
    }

    /**
     * 初始化jedis连接池
     */
    public static void init() {
        try {
            JEDIS_POOL = new JedisPool("192.168.221.139", 6379);

            LOGGER.info("连接redis成功");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 获取jedis客户端
     *
     * @return Jedis
     */
    public static Jedis getJedis() {
        if (JEDIS_POOL == null) {
            LOGGER.error("Jedis连接池未初始化");
            return null;
        }
        final Jedis jedis = JEDIS_POOL.getResource();
        jedis.auth("123456");
        return jedis;
    }
}
