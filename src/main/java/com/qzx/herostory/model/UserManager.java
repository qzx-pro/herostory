package com.qzx.herostory.model;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: qzx
 * @Date: 2021/2/2 - 02 - 02 - 18:50
 * @Description: com.qzx.herostory
 * @version: 1.0
 */
public final class UserManager {
    /**
     * 用户字典，存储所有已经登陆的用户信息
     */
    private static final ConcurrentHashMap<Integer, User> USER_MAP = new ConcurrentHashMap<>();

    /**
     * 私有化构造方法
     */
    private UserManager() {
    }

    /**
     * 功能描述 : 根据用户ID删除该用户
     *
     * @param: 待删除的用户ID
     * @return: void
     */
    public static void removeByUserId(Integer userId) {
        if (userId == null) {
            return;
        }

        USER_MAP.remove(userId);
    }

    /**
     * 功能描述 : 存储登陆的用户
     *
     * @param: 待存储的用户
     * @return: void
     */
    public static void save(User user) {
        if (user == null) {
            return;
        }

        USER_MAP.putIfAbsent(user.getUserId(), user);
    }

    /**
     * 功能描述
     *
     * @param: null
     * @return: 用户集合
     */
    public static Collection<User> getUserCollection() {
        return USER_MAP.values();
    }

    public static User getUserByUserId(int userId) {
        return USER_MAP.get(userId);
    }
}
