package cc.bukkitPlugin.bossshop.goods.reward;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.RewardType;
import org.bukkit.entity.Player;

public interface IReward{
    
    /**
     * 获取商店的回报物品类型
     */
    RewardType getRewardType();
    
    /**
     * 获取商店的奖励
     * @return
     */
    Object getReward(int pMulCount);
    
    /**
     * 给予回报
     * @param pPlayer 玩家
     * @param pMulCount 倍数
     */
    void giveReward(Player pPlayer,int pMulCount);
    
    /**
     * 使用指定的Object生成实例
     * <p>所有错误都会再函数中发出</p>
     * @param pObj 奖励Object主体
     * @return 是否初始化成功
     */
    boolean initReward(BossShop pPlugin,Object pObjReward);
    
    IReward copy();
    
    /**
     * 获取价格的描述
     * @param pMulCount 倍数
     * @return 描述
     */
    String getDescription(int pMulCount);
    
    /**
     * 是否允许奖励为复数
     * @return
     */
    boolean allowMultiple();
    
}
