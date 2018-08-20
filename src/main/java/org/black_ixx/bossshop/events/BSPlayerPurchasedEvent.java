package org.black_ixx.bossshop.events;

import org.black_ixx.bossshop.core.BSGoods;
import org.bukkit.entity.Player;

public class BSPlayerPurchasedEvent extends BSShopBuyEvent{
    
    private final int mCount;

    public BSPlayerPurchasedEvent(Player pPlayer,BSGoods pBuy,int pCount){
        super(pPlayer,pBuy);
        this.mCount=pCount;
        
    }
    
    public int getBuyCount(){
        return this.mCount;
    }

}
