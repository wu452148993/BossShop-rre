package org.black_ixx.bossshop.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSGoods;
import org.black_ixx.bossshop.core.BSShop;
import org.black_ixx.bossshop.core.BSShopHolder;
import org.black_ixx.bossshop.core.BSShopManager;
import org.black_ixx.bossshop.points.PointsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import cc.bukkitPlugin.bossshop.api.points.IPointsManager;
import cc.bukkitPlugin.commons.Log;
import cc.commons.commentedyaml.CommentedSection;

public class BossShopAPI{

    private BossShop mPlugin;
    private List<BossShopAddon> enabled_addons;

    public BossShopAPI(BossShop plugin){
        this.mPlugin=plugin;
    }

    // For Single Shop
    public boolean isValidShop(Inventory i){
        return (i.getHolder() instanceof BSShopHolder&&this.mPlugin.getManager(BSShopManager.class).getShop(i.getName())!=null);
    }

    public BSShop getShop(String name){
        return this.mPlugin.getManager(BSShopManager.class).getShop(name.toLowerCase());
    }

    public void openShop(Player p,String name){
        BSShop shop=getShop(name);
        if(shop==null){
            Log.warn("尝试打开不存在的商店["+name+"]");
            return;
        }
        openShop(p,shop);
    }

    public void openShop(Player p,BSShop shop){
        this.mPlugin.getManager(BSShopManager.class).openShop(p,shop);
    }

    // Get Managers
    public BSShopManager getShopHandler(){
        return this.mPlugin.getManager(BSShopManager.class);
    }

    // Modify Shop/Shops
    public void addItemToShop(BSGoods shop_item,BSShop shop){
        shop.addShopItem(shop_item,true);
    }

    // Modify Shop/Shops
    public void addItemToShop(BSGoods shop_item,BSShop shop,boolean pUpdateInv){
        shop.addShopItem(shop_item,pUpdateInv);
    }

    public void finishedAddingItemsToShop(BSShop shop){
        shop.finishedAddingItems();
    }

    // Get Shop Items
    public HashMap<BSShop,List<BSGoods>> getAllShopItems(){
        HashMap<BSShop,List<BSGoods>> all=new HashMap<BSShop,List<BSGoods>>();
        for(BSShop sShop : this.mPlugin.getManager(BSShopManager.class).getShops().values()){
            all.put(sShop,new ArrayList<>(sShop.getAllGoods()));
        }
        return all;
    }

    public HashMap<BSShop,List<BSGoods>> getAllShopItems(String config_option){

        HashMap<BSShop,List<BSGoods>> all=new HashMap<BSShop,List<BSGoods>>();
        for(BSShop shop : this.mPlugin.getManager(BSShopManager.class).getShops().values()){
            List<BSGoods> items=new ArrayList<>();
            for(BSGoods sGoods : shop.getAllGoods()){
                CommentedSection tGoodsSection=sGoods.getConfigurationSection();
                if(tGoodsSection!=null&&tGoodsSection.getBoolean(config_option)==false&&tGoodsSection.getInt(config_option)==0)
                    continue;
                items.add(sGoods);
            }
            all.put(shop,items);
        }
        return all;
    }

    // Addon API
    protected void addEnabledAddon(BossShopAddon addon){
        Plugin addonplugin=Bukkit.getPluginManager().getPlugin(addon.getAddonName());
        if(addonplugin==null){
            return;
        }
        if(enabled_addons==null){
            enabled_addons=new ArrayList<BossShopAddon>();
        }
        if(enabled_addons.contains(addon)){
            return;
        }
        enabled_addons.add(addon);
    }

    public List<BossShopAddon> getEnabledAddons(){
        return enabled_addons;
    }

    public IPointsManager getPointManager(){
        return this.mPlugin.getManager(PointsManager.class);
    }

}
