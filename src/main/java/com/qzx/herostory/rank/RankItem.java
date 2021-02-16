package com.qzx.herostory.rank;

/**
 * @author: qzx
 * @date: 2021/2/16 - 02 - 16 - 10:36
 * @description: 排名条目
 * @version: 1.0
 */
public class RankItem {
    /**
     * 排名Id
     */
    private Integer rankId;

    /**
     * 用户Id
     */
    private Integer userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户形象
     */
    private String heroAvatar;

    /**
     * 胜利次数
     */
    private Integer win;

    public Integer getRankId() {
        return rankId;
    }

    public void setRankId(Integer rankId) {
        this.rankId = rankId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHeroAvatar() {
        return heroAvatar;
    }

    public void setHeroAvatar(String heroAvatar) {
        this.heroAvatar = heroAvatar;
    }

    public Integer getWin() {
        return win;
    }

    public void setWin(Integer win) {
        this.win = win;
    }
}
