package org.black_ixx.bossshop.listeners;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums;
import org.black_ixx.bossshop.core.BSGoods;
import org.black_ixx.bossshop.core.BSShopHolder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

import cc.bukkitPlugin.bossshop.goods.reward.IReward;
import cc.bukkitPlugin.bossshop.lottery.LotteryHolder;
import cc.bukkitPlugin.bossshop.numbkey.NumbKeyManager;
import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.util.BukkitUtil;

public class InventoryListener implements Listener{

    private BossShop mPlugin;

    public InventoryListener(BossShop pPlugin){
        this.mPlugin=pPlugin;
        this.mPlugin.getServer().getPluginManager().registerEvents(this,this.mPlugin);
    }

    @EventHandler
    public void closeShop(InventoryCloseEvent event){
        if(!(event.getInventory().getHolder() instanceof BSShopHolder))
            return;
        if(!(event.getPlayer() instanceof Player))
            return;
        Player tPlayer=(Player)event.getPlayer();
        BSShopHolder bsHolder=(BSShopHolder)event.getInventory().getHolder();
        Log.send(tPlayer,bsHolder.getShop().getLeaveMessage());
        if(bsHolder instanceof LotteryHolder){
            LotteryHolder ltHolder=(LotteryHolder)bsHolder;
            ltHolder.closeLottery();
        }
        bsHolder.getShop().removeCustomer(tPlayer);
    }

    @EventHandler(priority=EventPriority.HIGH)
    public void purchase(InventoryClickEvent pEvent){
        if(!(pEvent.getWhoClicked() instanceof Player))
            return;
        if(!(pEvent.getInventory().getHolder() instanceof BSShopHolder))
            return;

        BSShopHolder holder=(BSShopHolder)pEvent.getInventory().getHolder();
        pEvent.setCancelled(true);
        pEvent.setResult(Result.DENY);
        //检查点击的是否为玩家
        if(!(pEvent.getWhoClicked() instanceof Player))
            return;
        Player tPlayer=(Player)pEvent.getWhoClicked();
        if(BukkitUtil.isValidItem(tPlayer.getItemOnCursor()))
            return;

        //获取点击的商品
        ItemStack tItem=pEvent.getCurrentItem();
        if(tItem==null||tItem.getType()==Material.AIR){
            pEvent.setCursor(null);
            return;
        }
        if(pEvent.getSlotType()!=SlotType.CONTAINER)
            return;
        BSGoods tReward=holder.getDisplayItemAt(pEvent.getRawSlot());
        if(tReward==null)
            return;

        boolean tAllNothing=true;
        for(IReward sReward : tReward.getRewards()){
            if(sReward.getRewardType()!=BSEnums.RewardType.Nothing){
                tAllNothing=false;
                break;
            }
        }
        if(tAllNothing) return;
        //处理不同的操作
        ClickType tType=pEvent.getClick();
        try{
            if(tType==ClickType.RIGHT||tType==ClickType.LEFT||tType==ClickType.SHIFT_LEFT){
                tReward.buyGoods(tPlayer,holder,tType);
            }else if(tType==ClickType.NUMBER_KEY){
                this.mPlugin.getManager(NumbKeyManager.class).doFunction(pEvent.getHotbarButton()+1,tPlayer,tReward);
            }
        }catch(Throwable exp){
            Log.severe(exp);
            Log.send(tPlayer,this.mPlugin.C("MsgErrorHappend"));
        }
    }

}
