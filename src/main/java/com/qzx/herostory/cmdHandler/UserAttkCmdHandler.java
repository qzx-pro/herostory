package com.qzx.herostory.cmdHandler;

import com.qzx.herostory.BroadCaster;
import com.qzx.herostory.model.User;
import com.qzx.herostory.model.UserManager;
import com.qzx.herostory.msg.GameMsgProtocolLogin;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * @author: qzx
 * @date: 2021/2/3 - 02 - 03 - 12:02
 * @description: com.qzx.herostory.cmdHandler
 * @version: 1.0
 */
public class UserAttkCmdHandler implements ICmdHandler<GameMsgProtocolLogin.UserAttkCmd> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAttkCmdHandler.class);

    private static final Random RANDOM = new Random();

    /**
     * 广播攻击结果消息
     *
     * @param attkUserId   攻击者ID
     * @param targetUserId 被攻击者ID
     */
    private static void broadcastUserAttkResult(int attkUserId, int targetUserId) {
        // 构建UserAttkResult消息
        GameMsgProtocolLogin.UserAttkResult.Builder newBuilder = GameMsgProtocolLogin.UserAttkResult.newBuilder();
        newBuilder.setAttkUserId(attkUserId);
        newBuilder.setTargetUserId(targetUserId);
        // 广播UserAttkResult消息
        BroadCaster.broadcast(newBuilder.build());
    }

    /**
     * 广播掉血消息
     *
     * @param targetUserId 被攻击者ID
     * @param subtractHp   掉血的血量
     */
    private static void broadcastUserSubtractHpResult(int targetUserId, int subtractHp) {
        // 构建UserSubtractHpResult消息
        GameMsgProtocolLogin.UserSubtractHpResult.Builder newBuilder = GameMsgProtocolLogin.UserSubtractHpResult.newBuilder();
        newBuilder.setSubtractHp(subtractHp);
        newBuilder.setTargetUserId(targetUserId);
        // 广播UserSubtractHpResult消息
        BroadCaster.broadcast(newBuilder.build());
    }

    /**
     * 广播被攻击用户死亡消息
     *
     * @param targetUserId 被攻击者的ID
     */
    private static void broadcastUserDieResult(int targetUserId) {
        // 构建UserDieResult消息
        GameMsgProtocolLogin.UserDieResult.Builder newBuilder = GameMsgProtocolLogin.UserDieResult.newBuilder();
        newBuilder.setTargetUserId(targetUserId);
        // 广播UserDieResult消息
        BroadCaster.broadcast(newBuilder.build());
    }

    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, GameMsgProtocolLogin.UserAttkCmd msg) {

        if (channelHandlerContext == null || msg == null) {
            return;
        }

        // 获取攻击方id
        Integer userId = (Integer) channelHandlerContext.channel().attr(AttributeKey.valueOf("userId")).get();

        if (userId == null) {
            return;
        }

        // 获取攻击用户
        User user = UserManager.getUserByUserId(userId);

        if (user == null) {
            return;
        }

        // 获取攻击目标
        int targetUserId = msg.getTargetUserId();
        if (targetUserId <= 0) {
            // 没有攻击对象就广播攻击行为
            broadcastUserAttkResult(userId, 0);
        } else {
            // 每一次攻击的伤害
            int subtractHp = RANDOM.nextInt(10) + 1;
            User targetUser = UserManager.getUserByUserId(targetUserId);
            // 攻击对象还活着就掉血
            if (targetUser.getCurrentHp() > 0) {
                // 广播攻击消息UserAttkResult
                broadcastUserAttkResult(userId, targetUserId);
                // 广播掉血消息(有攻击对象才会掉血)
                broadcastUserSubtractHpResult(targetUserId, subtractHp);
            } else {
                // 攻击对象已经死去就广播攻击行为,并不再广播死亡行为
                broadcastUserAttkResult(userId, 0);
                return;
            }
            // 更新当前血量
            int currentHp = targetUser.getCurrentHp() - subtractHp;
            targetUser.setCurrentHp(currentHp);

            // 血量减小到0，广播UserDieResult消息
            if (currentHp <= 0) {
                broadcastUserDieResult(targetUserId);
            }
        }
    }

}
