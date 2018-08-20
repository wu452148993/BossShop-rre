package cc.bukkitPlugin.bossshop.numbkey;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSGoods;
import org.bukkit.entity.Player;

public abstract class AFunction{

    protected BossShop mPlugin;
    
    public AFunction(BossShop pPlugin){
        this.mPlugin=pPlugin;
    }
    
    public abstract String getFunctionName();
    
    public abstract void doFunction(Player pPlayer,BSGoods pReward);
    
}
