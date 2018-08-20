package cc.bukkitPlugin.bossshop.goods.reward;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.RewardType;
import org.black_ixx.bossshop.managers.WorthHandler;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;


public class RewardMoney implements IReward{

    private double mReward=0D;
    /**该变量只在调用initPrice才会初始化*/
    private  BossShop mPlugin;
    
    @Override
    public void giveReward(Player pPlayer,int pMulCount){
        double tPrice=this.mReward*pMulCount;
        this.mPlugin.getManager(WorthHandler.class).giveRewardMoney(pPlayer,tPrice);
    }

    @Override
    public RewardType getRewardType(){
        return RewardType.Money;
    }

    @Override
    public boolean initReward(BossShop pPlugin,Object pObjReward){
        this.mPlugin=pPlugin;
        if(pObjReward==null){
            Log.severe("未配置商品奖励,商品["+this.mPlugin.C(this.getRewardType().getNameKey())+"]的奖励是必须的");
            return false;
        }
        double tReward=0D;
        try{
            tReward=Double.parseDouble(pObjReward.toString());
        }catch(Exception exc){
            Log.severe("商品类型为金币时,商品配置必须为数字");
            return false;
        }
        this.mReward=tReward;
        return true;
    }

    @Override
    public RewardMoney copy(){
        RewardMoney tReward=new RewardMoney();
        tReward.mPlugin=this.mPlugin;
        tReward.mReward=this.mReward;
        return tReward;
    }

    @Override
    public String getDescription(int pMulCount){
        return (this.mReward*pMulCount)+this.mPlugin.C("WordMoney");
    }

    @Override
    public Double getReward(int pMulCount){
        return this.mReward*pMulCount;
    }

    @Override
    public boolean allowMultiple(){
        return true;
    }

}
