package cc.bukkitPlugin.bossshop.goods.reward;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.RewardType;
import org.black_ixx.bossshop.managers.WorthHandler;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;


public class RewardPoints implements IReward{

    private int mReward=0;
    /**该变量只在调用initPrice才会初始化*/
    private  BossShop mPlugin;
    
    @Override
    public RewardType getRewardType(){
        return RewardType.Points;
    }

    @Override
    public void giveReward(Player pPlayer,int pMulCount){
        int tPrice=(int)(this.mReward*pMulCount);
        this.mPlugin.getManager(WorthHandler.class).giveRewardPoints(pPlayer,tPrice);
    }

    @Override
    public boolean initReward(BossShop pPlugin,Object pObjReward){
        this.mPlugin=pPlugin;
        if(pObjReward==null){
            Log.severe("未配置商品奖励,商品["+this.mPlugin.C(this.getRewardType().getNameKey())+"]的奖励是必须的");
            return false;
        }
        if(pObjReward instanceof Integer){
            this.mReward=((Integer)pObjReward).intValue();
            return true;
        }
        try{
            this.mReward=Integer.parseInt(pObjReward.toString());
        }catch(Exception exp){
            Log.severe("商品类型为点券时,商品配置必须为数字");
            return false;
        }
        return true;
    }

    @Override
    public IReward copy(){
        RewardPoints tReward=new RewardPoints();
        tReward.mPlugin=this.mPlugin;
        tReward.mReward=this.mReward;
        return tReward;
    }

    @Override
    public String getDescription(int pMulCount){
        return (this.mReward*pMulCount)+this.mPlugin.C("WordPoints");
    }

    @Override
    public Integer getReward(int pMulCount){
        return (int)(this.mReward*pMulCount);
    }

    @Override
    public boolean allowMultiple(){
        return true;
    }

}
