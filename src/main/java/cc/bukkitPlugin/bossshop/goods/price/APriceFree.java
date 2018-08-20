package cc.bukkitPlugin.bossshop.goods.price;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.plugin.manager.fileManager.TLangManager;


public abstract class APriceFree implements IPrice{

    @Override
    public boolean hasPrice(Player pPlayer,double pMulCount){
        return true;
    }

    @Override
    public String takePrice(Player pPlayer,double pMulCount){
        return null;
    }

    @Override
    public boolean initPrice(BossShop pPlugin,Object pObjPrice){
        return true;
    }

    @Override
    public String getDescription(double pMulCount){
        return TLangManager.staticGetNode("WordFree","免费");
    }

}
