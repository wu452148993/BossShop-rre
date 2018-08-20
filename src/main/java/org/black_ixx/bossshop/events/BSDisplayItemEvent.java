package org.black_ixx.bossshop.events;
import org.black_ixx.bossshop.core.BSGoods;
import org.black_ixx.bossshop.core.BSShop;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BSDisplayItemEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final BSGoods buy;
    private final BSShop shop;
    private boolean cancelled = false;
    public BSDisplayItemEvent(Player player, BSShop shop, BSGoods buy) {
        this.player=player;
        this.buy=buy;
        this.shop=shop;
    }

    public Player getPlayer() {
        return player;
    }

    public BSGoods getShopItem(){
        return buy;
    }

    public BSShop getShop(){
        return shop;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}