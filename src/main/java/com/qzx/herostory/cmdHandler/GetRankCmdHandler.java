package com.qzx.herostory.cmdHandler;

import com.qzx.herostory.msg.GameMsgProtocolRank;
import com.qzx.herostory.rank.RankItem;
import com.qzx.herostory.rank.RankService;
import io.netty.channel.ChannelHandlerContext;

import java.util.Collections;

/**
 * @author: qzx
 * @date: 2021/2/14 - 02 - 14 - 16:21
 * @description: 获取排名消息处理器
 * @version: 1.0
 */
public class GetRankCmdHandler implements ICmdHandler<GameMsgProtocolRank.GetRankCmd> {
    @Override
    public void handle(ChannelHandlerContext channelHandlerContext, GameMsgProtocolRank.GetRankCmd msg) {
        if (channelHandlerContext == null || msg == null) {
            return;
        }

        // 获取排行榜数据
        RankService.getInstance().getRankList(rankItemList -> {
            if (rankItemList == null) {
                rankItemList = Collections.emptyList();
            }

            GameMsgProtocolRank.GetRankResult.Builder builder = GameMsgProtocolRank.GetRankResult.newBuilder();

            for (RankItem rankItem : rankItemList) {
                if (rankItem == null) {
                    continue;
                }

                GameMsgProtocolRank.GetRankResult.RankItem.Builder rankItemBuilder =
                        GameMsgProtocolRank.GetRankResult.RankItem.newBuilder();
                rankItemBuilder.setRankId(rankItem.getRankId());
                rankItemBuilder.setHeroAvatar(rankItem.getHeroAvatar());
                rankItemBuilder.setUserName(rankItem.getUserName());
                rankItemBuilder.setWin(rankItem.getWin());
                rankItemBuilder.setUserId(rankItem.getUserId());

                builder.addRankItem(rankItemBuilder.build());
            }

            channelHandlerContext.writeAndFlush(builder.build());

            return null;
        });
    }
}

