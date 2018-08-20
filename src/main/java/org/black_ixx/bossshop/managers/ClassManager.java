package org.black_ixx.bossshop.managers;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.points.PointsManager;

import cc.bukkitPlugin.commons.hooks.vaultHook.VaultHook;
import cc.bukkitPlugin.commons.plugin.manager.AManager;

public class ClassManager extends AManager<BossShop>{

    public static ClassManager manager;

    public ClassManager(BossShop pPlugin){
        super(pPlugin);
    }

    public ItemStackChecker getItemStackChecker(){
        return this.mPlugin.getManager(ItemStackChecker.class);
    }

    public PointsManager getPointsManager(){
        return this.mPlugin.getManager(PointsManager.class);
    }

    public VaultHook<BossShop> getVaultHandler(){
        return this.mPlugin.getManager(VaultHook.class);
    }

    public TimeHandler getTimeHandler(){
        return this.mPlugin.getManager(TimeHandler.class);
    }

    public ItemStackCreator getItemStackCreator(){
        return this.mPlugin.getManager(ItemStackCreator.class);
    }

    public BuyItemHandler getBuyItemHandler(){
        return this.mPlugin.getManager(BuyItemHandler.class);
    }

    public BossShop getPlugin(){
        return this.mPlugin;
    }

    public ShopCustomizer getShopCustomizer(){
        return this.mPlugin.getManager(ShopCustomizer.class);
    }

    public TransactionLog getTransactionLog(){
        return this.mPlugin.getManager(TransactionLog.class);
    }
}
