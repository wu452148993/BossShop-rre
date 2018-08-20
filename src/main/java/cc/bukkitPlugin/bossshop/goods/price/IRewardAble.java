package cc.bukkitPlugin.bossshop.goods.price;

import org.bukkit.OfflinePlayer;

public interface IRewardAble{
    
    /**
     * 给予回报
     * @param pPlayer 玩家
     * @param pMulCount 倍数
     */
    void giveReward(OfflinePlayer pPlayer,double pMulCount);
    
}
