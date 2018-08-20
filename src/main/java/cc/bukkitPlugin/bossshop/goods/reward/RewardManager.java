package cc.bukkitPlugin.bossshop.goods.reward;

import java.util.HashMap;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums;
import org.black_ixx.bossshop.core.BSEnums.RewardType;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.manager.AManager;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.util.CollUtil;

public class RewardManager extends AManager<BossShop>{

    private final HashMap<RewardType,IReward> mRewards=new HashMap<>();

    public RewardManager(BossShop pPlugin){
        super(pPlugin);
        this.registerReward(new RewardEnchantment());
        this.registerReward(new RewardItem());
        this.registerReward(new RewardLottery());
        this.registerReward(new RewardMoney());
        this.registerReward(new RewardNothing());
        this.registerReward(new RewardPermission());
        this.registerReward(new RewardPlayerCommand());
        this.registerReward(new RewardPoints());
        this.registerReward(new RewardShop());
        this.registerReward(new RewardTimeCommand());
        this.registerReward(new RewardOpCommand());
        this.registerReward(new RewardConsoleCommand());
    }

    public boolean isRewardSection(CommentedSection pSec){
        return pSec.get("RewardType")!=null||pSec.get("Reward")!=null;
    }

    /**
     * 获取指定价格类型的实例
     * 
     * @param pBuyType
     *            价格类型字符串
     * @param pObj
     *            价格Object主体
     * @return 创建的价格,如果创建失败,则返回null
     */
    public IReward createReward(CommentedSection pRewardSec,String pName){
        RewardType tRewardType=null;
        String rewardType=pRewardSec.getString("RewardType");
        if(rewardType==null){
            Log.severe("无法创建商品["+pName+"],未设置商品类型");
            return null;
        }else{
            if((tRewardType=BSEnums.getRewardType(rewardType))==null){
                Log.severe("无法创建商品["+pName+"],["+rewardType+"]不是受支持商品类型");
                Log.severe("支持的商品类型有: "+CollUtil.asList(RewardType.values()));
                return null;
            }
        }

        return this.createReward(tRewardType,pRewardSec.get("Reward"));
    }

    public IReward createReward(RewardType pRewardType,Object pReward){
        IReward tRewardModel=this.mRewards.get(pRewardType);
        if(tRewardModel==null){
            Log.severe("商品奖励类型为"+pRewardType.getClass().getSimpleName()+"的类未注册模块");
            return null;
        }
        if(!tRewardModel.initReward(this.mPlugin,pReward))
            return null;
        else return tRewardModel.copy();
    }

    protected void registerReward(IReward pRewardInstance){
        if(pRewardInstance==null)
            throw new IllegalArgumentException("注册商品奖励种类时,商品奖励实例不能为null");
        IReward registered=this.mRewards.put(pRewardInstance.getRewardType(),pRewardInstance);
        if(registered!=null&&registered.getClass()!=pRewardInstance.getClass()){
            Log.severe("商品奖励类["+pRewardInstance.getClass().getSimpleName()+"]与["+registered.getClass().getSimpleName()+"]使用了相同的注册标签");
        }
    }

}
