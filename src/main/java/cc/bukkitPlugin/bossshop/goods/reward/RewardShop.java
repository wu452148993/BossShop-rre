package cc.bukkitPlugin.bossshop.goods.reward;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.RewardType;
import org.black_ixx.bossshop.managers.WorthHandler;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;
import cc.commons.commentedyaml.CommentedSection;


public class RewardShop implements IReward{

    private String mReward;
    /**该变量只在调用initPrice才会初始化*/
    private  BossShop mPlugin;
    
    @Override
    public RewardType getRewardType(){
        return RewardType.Shop;
    }

    @Override
    public void giveReward(Player pPlayer,int pMulCount){
        this.mPlugin.getManager(WorthHandler.class).giveRewardShop(pPlayer,this.mReward);
    }

    @Override
    public boolean initReward(BossShop pPlugin,Object pObjReward){
        this.mPlugin=pPlugin;
        if(pObjReward==null){
            Log.severe("未配置商品奖励,商品["+this.mPlugin.C(this.getRewardType().getNameKey())+"]的奖励是必须的");
            return false;
        }
        if(!(pObjReward instanceof String)&&!CommentedSection.isPrimitiveWrapper(pObjReward)){
            Log.severe("商品类型为商店时,商品配置必须为String");
            return false;
        }
        this.mReward=String.valueOf(pObjReward);
        return true;
    }

    @Override
    public IReward copy(){
        RewardShop tReward=new RewardShop();
        tReward.mPlugin=this.mPlugin;
        tReward.mReward=this.mReward;
        return tReward;
    }

    @Override
    public String getDescription(int pMulCount){
        return this.mPlugin.C("WordShop")+": "+this.mReward;
    }

    @Override
    public String getReward(int pMulCount){
        return this.mReward;
    }

    @Override
    public boolean allowMultiple(){
        return false;
    }

}
