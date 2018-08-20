package org.black_ixx.bossshop.events;

import org.black_ixx.bossshop.core.BSGoods;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class BSPlayerPurchaseEvent extends BSShopBuyEvent implements Cancellable{

    private final int mCount;
    private boolean cancelled=false;

    public BSPlayerPurchaseEvent(Player player,BSGoods buy,int pCount){
        super(player,buy);
        this.mCount=pCount;
    }

    public int getBuyCount(){
        return this.mCount;
    }

    @Override
    public boolean isCancelled(){
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled){
        this.cancelled=cancelled;
    }

}
