package com.qzx.herostory.model;

/**
 * @author : qzx
 * @Date: 2021/2/1 - 02 - 01 - 19:59
 * @Description: com.qzx.herostory
 * @version: 1.0
 */
public class User {
    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String heroAvatar;

    /**
     * 用户的移动状态
     */
    private final MoveState moveState = new MoveState();

    /**
     * 血量
     */
    private Integer currentHp;

    public Integer getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(Integer currentHp) {
        this.currentHp = currentHp;
    }

    public User(Integer userId, String heroAvatar, Integer currentHp) {
        this.userId = userId;
        this.heroAvatar = heroAvatar;
        this.currentHp = currentHp;
    }

    public User(Integer userId, String heroAvatar) {
        this.userId = userId;
        this.heroAvatar = heroAvatar;
    }

    public User() {
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

    public MoveState getMoveState() {
        return moveState;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", heroAvatar='" + heroAvatar + '\'' +
                ", moveState=" + moveState +
                ", currentHp=" + currentHp +
                '}';
    }
}
