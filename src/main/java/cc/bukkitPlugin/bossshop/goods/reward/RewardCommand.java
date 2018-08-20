package cc.bukkitPlugin.bossshop.goods.reward;

import org.black_ixx.bossshop.BossShop;

import cc.bukkitPlugin.commons.Log;

public abstract class RewardCommand extends ARewardCommand{

    @Override
    public boolean initReward(BossShop pPlugin,Object pObjReward){
        this.mPlugin=pPlugin;
        if(pObjReward==null){
            Log.severe("未配置商品奖励,商品["+this.mPlugin.C(this.getRewardType().getNameKey())+"]的奖励是必须的");
            return false;
        }
        boolean initSuccess=super.initReward(pPlugin,pObjReward);
        if(!initSuccess)
            Log.severe("商品类型为命令时,商品奖励配置必须为List<String>或String");
        return initSuccess;
    }

    @Override
    public boolean allowMultiple(){
        return true;
    }

}
