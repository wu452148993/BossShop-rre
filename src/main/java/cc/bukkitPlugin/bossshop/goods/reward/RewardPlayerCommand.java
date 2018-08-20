package cc.bukkitPlugin.bossshop.goods.reward;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.RewardType;
import org.black_ixx.bossshop.managers.WorthHandler;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;


public class RewardPlayerCommand extends ARewardCommand{

    @Override
    public RewardType getRewardType(){
        return RewardType.PlayerCommand;
    }

    @Override
    public boolean initReward(BossShop pPlugin,Object pObjReward){
        this.mPlugin=pPlugin;
        if(pObjReward==null){
            Log.severe("未配置商品奖励,商品["+this.mPlugin.C(this.getRewardType().getNameKey())+"]的奖励是必须的");
            return false;
        }
        boolean initSuccess=super.initReward(pPlugin,pObjReward);
        if(!initSuccess)
            Log.severe("商品类型为玩家命令时,商品配置必须为List<String>或String");
        return initSuccess;
    }
    
    @Override
    public void giveReward(Player pPlayer,int pMulCount){
        WorthHandler worthMan=this.mPlugin.getManager(WorthHandler.class);
        for(int i=0;i<pMulCount;i++)
            worthMan.giveRewardPlayerCommand(pPlayer,this.mReward);
    }

    @Override
    public IReward copy(){
        return this.setContent(new RewardPlayerCommand());
    }

    @Override
    public boolean allowMultiple(){
        return true;
    }

}
