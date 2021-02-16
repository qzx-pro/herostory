package com.qzx.herostory.rank;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qzx.herostory.async.AsyncOperationProcessor;
import com.qzx.herostory.async.IAsyncOperation;
import com.qzx.herostory.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * @author: qzx
 * @date: 2021/2/16 - 02 - 16 - 10:39
 * @description: 获取排名服务
 * @version: 1.0
 */
public class RankService {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RankService.class);

    /**
     * 单例对象
     */
    private static final RankService RANK_SERVICE = new RankService();

    /**
     * 私有化构造方法
     */
    private RankService() {

    }

    /**
     * 获取单例对象
     *
     * @return RankService
     */
    public static RankService getInstance() {
        return RANK_SERVICE;
    }

    /**
     * 获取排名列表
     *
     * @param callback 回调函数
     */
    public void getRankList(Function<List<RankItem>, Void> callback) {
        // 异步执行从redis中 获取排名
        AsyncOperationProcessor.getInstance().process(new AsyncRankOperation() {
            @Override
            public void doFinish() {
                callback.apply(this.getRankItemList());
            }
        });
    }

    /**
     * 刷新redis排行榜数据
     *
     * @param winnerId 赢家Id
     * @param loserId  输家Id
     */
    public void refreshRedis(int winnerId, int loserId) {
        if (winnerId <= 0 || loserId <= 0) {
            return;
        }

        try (final Jedis jedis = RedisUtil.getJedis()) {
            if (jedis == null) {
                return;
            }
            // 增加用户胜利和失败的次数
            jedis.hincrBy("User_" + winnerId, "Win", 1);
            jedis.hincrBy("User_" + loserId, "Lose", 1);
            // 获取winnerId胜利的次数
            int win = Integer.parseInt(jedis.hget("User_" + winnerId, "Win"));
            // 修改排名数据
            jedis.zadd("Rank", win, String.valueOf(winnerId));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static class AsyncRankOperation implements IAsyncOperation {
        /**
         * 获取排名结果集
         */
        private List<RankItem> rankItemList;

        /**
         * 返回排名列表
         *
         * @return 排名列表
         */
        public List<RankItem> getRankItemList() {
            return rankItemList;
        }

        @Override
        public void doAsync() {
            // 获取jedis对象
            try (Jedis jedis = RedisUtil.getJedis()) {

                if (jedis == null) {
                    return;
                }

                rankItemList = new LinkedList<>();

                // 当前用户排名
                int rank = 0;
                // 排名结果前10集合
                Set<Tuple> tuples = jedis.zrevrangeWithScores("Rank", 0, 9);
                for (Tuple tuple : tuples) {
                    // 获取用户Id
                    int userId = Integer.parseInt(tuple.getElement());
                    // 获取胜利次数
                    int win = (int) tuple.getScore();
                    // 获取用户信息
                    String userInfo = jedis.hget("User_" + userId, "BasicInfo");

                    if (userInfo == null) {
                        continue;
                    }

                    // 构建RankItem
                    JSONObject jsonObject = JSON.parseObject(userInfo);
                    String userName = jsonObject.getString("userName");
                    String heroAvatar = jsonObject.getString("heroAvatar");
                    RankItem rankItem = new RankItem();
                    rankItem.setRankId(++rank);
                    rankItem.setUserId(userId);
                    rankItem.setWin(win);
                    rankItem.setUserName(userName);
                    rankItem.setHeroAvatar(heroAvatar);

                    // 将rankItem添加到rankItemList中
                    rankItemList.add(rankItem);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
