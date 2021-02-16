package com.qzx.herostory.mq;

/**
 * @author: qzx
 * @date: 2021/2/16 - 02 - 16 - 18:05
 * @description: 击杀成功消息
 * @version: 1.0
 */
public class VictorMsg {
    /**
     * 胜利者Id
     */
    private Integer winnerId;

    /**
     * 失败者Id
     */
    private Integer loseId;

    public Integer getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Integer winnerId) {
        this.winnerId = winnerId;
    }

    public Integer getLoseId() {
        return loseId;
    }

    public void setLoseId(Integer loseId) {
        this.loseId = loseId;
    }
}
