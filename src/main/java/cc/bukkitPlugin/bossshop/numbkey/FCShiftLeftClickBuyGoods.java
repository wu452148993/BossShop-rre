package cc.bukkitPlugin.bossshop.numbkey;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSGoods;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;


public class FCShiftLeftClickBuyGoods extends AFunction{
	
    public FCShiftLeftClickBuyGoods(BossShop pPlugin){
        super(pPlugin);
    }

    @Override
    public String getFunctionName(){
        return "SHIFT_LEFT_CLICK_BUY_GOODS";
    }

    @Override
    public void doFunction(Player pPlayer,BSGoods pReward){
        if(pPlayer==null||pReward==null) return;
        pReward.buyGoods(pPlayer,ClickType.SHIFT_LEFT);
    }
}
