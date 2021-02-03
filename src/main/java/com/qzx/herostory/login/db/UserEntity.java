package com.qzx.herostory.login.db;

/**
 * @author: qzx
 * @date: 2021/2/7 - 02 - 07 - 15:38
 * @description: 用户实体
 * @version: 1.0
 */
public class UserEntity {
    /**
     * 用户 Id
     */
    public int userId;

    /**
     * 用户名称
     */
    public String userName;

    /**
     * 密码
     */
    public String password;

    /**
     * 英雄形象
     */
    public String heroAvatar;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHeroAvatar() {
        return heroAvatar;
    }

    public void setHeroAvatar(String heroAvatar) {
        this.heroAvatar = heroAvatar;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", heroAvatar='" + heroAvatar + '\'' +
                '}';
    }
}
