package cc.bukkitPlugin.bossshop.goods.reward;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.RewardType;
import org.bukkit.entity.Player;


public class RewardNothing implements IReward{

    /**该变量只在调用initPrice才会初始化*/
    private  BossShop mPlugin;
    
    @Override
    public RewardType getRewardType(){
        return RewardType.Nothing;
    }

    @Override
    public void giveReward(Player pPlayer,int pMulCount){
    }

    @Override
    public boolean initReward(BossShop pPlugin,Object pObjReward){
        this.mPlugin=pPlugin;
        return true;
    }

    @Override
    public IReward copy(){
        RewardNothing tReward=new RewardNothing();
        tReward.mPlugin=this.mPlugin;
        return tReward;
    }

    @Override
    public String getDescription(int pMulCount){
        return "";
    }

    @Override
    public Object getReward(int pMulCount){
        return null;
    }

    @Override
    public boolean allowMultiple(){
        return false;
    }

}
