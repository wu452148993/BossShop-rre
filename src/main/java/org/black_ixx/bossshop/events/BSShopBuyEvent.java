package org.black_ixx.bossshop.events;

import org.black_ixx.bossshop.core.BSGoods;
import org.black_ixx.bossshop.core.BSShop;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public abstract class BSShopBuyEvent extends Event{

    protected final HandlerList mHandlerList=new HandlerList();
    protected final Player mPlayer;
    protected final BSGoods mReward;
    
    
    public BSShopBuyEvent(Player pPlayer,BSGoods pBuy){
        this.mPlayer=pPlayer;
        this.mReward=pBuy;
    }
    
    public Player getPlayer(){
        return this.mPlayer;
    }

    public BSGoods getShopItem(){
        return this.mReward;
    }

    public BSShop getShop(){
        return this.mReward.getShop();
    }
    
    @Override
    public HandlerList getHandlers(){
        return this.mHandlerList;
    }

}
