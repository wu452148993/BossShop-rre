package cc.bukkitPlugin.bossshop.numbkey;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSGoods;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class FCUnsaleGoods extends AFunction{

    public FCUnsaleGoods(BossShop pPlugin){
        super(pPlugin);
    }

    @Override
    public String getFunctionName(){
        return "UNSALE_GOODS";
    }

    @Override
    public void doFunction(Player pPlayer,BSGoods pReward){
        if(pPlayer==null||pReward==null) return;
        Bukkit.dispatchCommand(pPlayer,this.mPlugin.getName()+" unsale "+pReward.getName());
    }

}
