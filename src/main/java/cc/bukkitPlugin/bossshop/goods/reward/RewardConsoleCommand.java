package cc.bukkitPlugin.bossshop.goods.reward;

import org.black_ixx.bossshop.core.BSEnums.RewardType;
import org.black_ixx.bossshop.managers.WorthHandler;
import org.bukkit.entity.Player;

public class RewardConsoleCommand extends RewardCommand{

    @Override
    public RewardType getRewardType(){
        return RewardType.ConsoleCommand;
    }

    @Override
    public void giveReward(Player pPlayer,int pMulCount){
        WorthHandler worthMan=this.mPlugin.getManager(WorthHandler.class);
        for(int i=0;i<pMulCount;i++)
            worthMan.giveRewardConsoleCommand(pPlayer,this.mReward);
    }

    @Override
    public IReward copy(){
        return this.setContent(new RewardConsoleCommand());
    }

}
