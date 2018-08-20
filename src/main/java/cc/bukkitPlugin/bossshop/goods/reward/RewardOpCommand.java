package cc.bukkitPlugin.bossshop.goods.reward;

import org.black_ixx.bossshop.core.BSEnums.RewardType;
import org.black_ixx.bossshop.managers.WorthHandler;
import org.bukkit.entity.Player;

public class RewardOpCommand extends RewardCommand{

    @Override
    public RewardType getRewardType(){
        return RewardType.OpCommand;
    }
    
    @Override
    public void giveReward(Player pPlayer,int pMulCount){
        WorthHandler worthMan=this.mPlugin.getManager(WorthHandler.class);
        for(int i=0;i<pMulCount;i++)
            worthMan.giveRewardOpCommand(pPlayer,this.mReward);
    }
    
    @Override
    public IReward copy(){
        return this.setContent(new RewardOpCommand());
    }

}
