package org.black_ixx.bossshop.managers;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSShopManager;
import org.black_ixx.bossshop.points.PointsManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import cc.bukkitPlugin.bossshop.goods.price.PriceItem.PriceInfo;
import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.hooks.vaultHook.EconomyHook;
import cc.bukkitPlugin.commons.hooks.vaultHook.PermissionHook;
import cc.bukkitPlugin.commons.hooks.vaultHook.VaultHook;
import cc.bukkitPlugin.commons.plugin.manager.AManager;
import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.commons.util.StringUtil;

public class WorthHandler extends AManager<BossShop>{

    public WorthHandler(BossShop pPlugin){
        super(pPlugin);
    }

    public String C(String pNode){
        return this.mPlugin.getLangManager().getNode(pNode);
    }

    public void giveRewardOpCommand(Player pPlayer,List<String> pCommands){
        boolean tOp=pPlayer.isOp();
        try{
            for(String sCmd : pCommands){
                sCmd=BossShop.replaceParam(pPlayer,sCmd);
                pPlayer.setOp(true);
                pPlayer.performCommand(sCmd);
            }
        }catch(Throwable exp){
        }finally{
            pPlayer.setOp(tOp);
        }
    }

    public void giveRewardConsoleCommand(Player pPlayer,List<String> pCommands){
        for(String sCmd : pCommands){
            sCmd=BossShop.replaceParam(pPlayer,sCmd);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),sCmd);
        }
    }

    public void giveRewardPlayerCommand(Player pPlayer,List<String> pCommands){
        for(String sCmd : pCommands){
            String command=BossShop.replaceParam(pPlayer,sCmd);
            PlayerCommandPreprocessEvent event=new PlayerCommandPreprocessEvent(pPlayer,"/"+command);
            Bukkit.getPluginManager().callEvent(event);
            if(event.isCancelled())
                continue;
            pPlayer.performCommand(event.getMessage().substring(1));
        }
    }

    public void giveRewardItem(Player pPlayer,List<ItemStack> items){
        for(ItemStack sRewardItem : items){
            BukkitUtil.giveItem(pPlayer,WorthHandler.transformRewardItem(sRewardItem,pPlayer));
        }
    }

    public void giveRewardPermission(Player pPlayer,List<String> pPermissions){
        PermissionHook<?> perHook=this.mPlugin.getManager(VaultHook.class).getPermission();
        for(String sPer : pPermissions){
            perHook.playerAdd(pPlayer,BossShop.replaceParam(pPlayer,sPer));
        }
    }

    public void giveRewardMoney(OfflinePlayer pPlayer,double pMoney){
        EconomyHook<?> ecoHook=this.mPlugin.getManager(VaultHook.class).getEconomy();
        ecoHook.depositPlayer(pPlayer,pMoney);
    }

    public void giveRewardPoints(OfflinePlayer pPlayer,int pPoints){
        this.mPlugin.getManager(PointsManager.class).givePoints(pPlayer,pPoints);
    }

    public void giveRewardShop(Player pPlayer,String pShopName){
        if(StringUtil.isEmpty(pShopName)){
            pPlayer.closeInventory();
            return;
        }
        this.mPlugin.getManager(BSShopManager.class).openShop(pPlayer,pShopName);
    }

    public void giveRewardEnchantment(Player pPlayer,Enchantment pEnchant,int pLevel){
        ItemStack tItem=pPlayer.getItemInHand();
        if(tItem!=null&&tItem.getType()!=Material.AIR){
            tItem.addUnsafeEnchantment(pEnchant,pLevel);
        }
    }

    public void giveRewardTimeCommand(Player pPlayer,IdentityHashMap<String,Long> pCommands){
        TimeHandler timeMan=this.mPlugin.getManager(TimeHandler.class);
        for(Map.Entry<String,Long> entry : pCommands.entrySet()){
            String tCmd=BossShop.replaceParam(pPlayer,entry.getKey());
            timeMan.addCommand(entry.getValue(),tCmd);
        }
    }

    public boolean hasExp(Player pPlayer,int pExp){
        if(pPlayer==null)
            return false;
        if((pPlayer.getLevel()<(Integer)pExp)){
            Log.send(pPlayer,C("MsgExpNotEnough"));
            return false;
        }
        return true;
    }

    /**
     * 检查玩家背包中是否存在指定的物品
     * 
     * @param pPlayer
     *            玩家
     * @param pItems
     *            物品列表
     * @param pReplaceParam
     *            是否替换物品变量
     * @param pNameAndLoreOnly
     *            检查的物品是否只匹配名字和Lore
     * @return 是否存在
     */
    public boolean hasItems(Player pPlayer,List<ItemStack> pItems,boolean pReplaceParam){
        if(pPlayer==null)
            return false;
        for(ItemStack sItem : pItems){
            if(!ItemStackChecker.inventoryContainsItem(pPlayer,pReplaceParam?WorthHandler.transformRewardItem(sItem,pPlayer):sItem,sItem.getAmount())){
                Log.send(pPlayer,C("MsgItemNotEnough"));
                return false;
            }
        }
        return true;
    }
    
    /**
     * 检查玩家背包中是否存在指定的物品
     * 
     * @param pPlayer
     *            玩家
     * @param pItems
     *            物品列表
     * @param pReplaceParam
     *            是否替换物品变量
     * @param pNameAndLoreOnly
     *            检查的物品是否只匹配名字和Lore
     * @return 是否存在
     */
    public boolean hasItems(Player pPlayer,List<PriceInfo> pItems){
        if(pPlayer==null)
            return false;
        for(PriceInfo sItem : pItems){
            if(!ItemStackChecker.inventoryContainsItem(pPlayer,sItem)){
                Log.send(pPlayer,C("MsgItemNotEnough"));
                return false;
            }
        }
        return true;
    }

    public boolean hasMoney(Player pPlayer,double pMoney){
        if(pPlayer==null)
            return false;
        EconomyHook<?> ecoHook=this.mPlugin.getManager(VaultHook.class).getEconomy();
        if(!ecoHook.hasAccount(pPlayer.getName())){
            Log.send(pPlayer,C("MsgYouEconomyHaveNoAccount"));
            return false;
        }
        if(ecoHook.getBalance(pPlayer.getName())<pMoney){
            Log.send(pPlayer,C("MsgMoneyNotEnough"));
            return false;
        }
        return true;
    }

    public boolean hasPoints(Player pPlayer,int pPoints){
        if(pPlayer==null)
            return false;
        if(this.mPlugin.getManager(PointsManager.class).getPoints(pPlayer)<pPoints){
            Log.send(pPlayer,C("MsgPointsNotEnough"));
            return false;
        }
        return true;
    }

    public String takeExp(Player p,int exp){
        p.setLevel(p.getLevel()-exp);
        return ""+p.getLevel();
    }

    /**
     * 从玩家背包中拿走指定的物品
     * 
     * @param pPlayer
     *            玩家
     * @param pItems
     *            物品列表
     * @param pReplaceParam
     *            是否替换物品变量
     * @param pNameAndLoreOnly
     *            拿走的物品是否只需要匹配名字和Lore
     * @return null
     */
    public String takeItems(Player pPlayer,List<ItemStack> pItems,boolean pReplaceParam){
        for(ItemStack sItem : pItems){
            ItemStackChecker.takeItem(pPlayer,pReplaceParam?WorthHandler.transformRewardItem(sItem,pPlayer):sItem,sItem.getAmount());
        }
        return null;
    }
    
    /**
     * 从玩家背包中拿走指定的物品
     * 
     * @param pPlayer
     *            玩家
     * @param pItems
     *            物品列表
     * @param pReplaceParam
     *            是否替换物品变量
     * @param pNameAndLoreOnly
     *            拿走的物品是否只需要匹配名字和Lore
     * @return null
     */
    public String takeItems(Player pPlayer,List<PriceInfo> pItems){
        for(PriceInfo sItem : pItems){
            ItemStackChecker.takeItem(pPlayer,sItem);
        }
        return null;
    }

    public String takeMoney(Player pPlayer,double pMoney){
        EconomyHook<?> ecoHook=this.mPlugin.getManager(VaultHook.class).getEconomy();
        ecoHook.withdrawPlayer(pPlayer,pMoney);
        return ""+ecoHook.getBalance(pPlayer);
    }

    public String takePoints(Player pPlayer,int pPoints){
        return ""+this.mPlugin.getManager(PointsManager.class).takePoints(pPlayer,pPoints);
    }

    public static ItemStack transformRewardItem(ItemStack pItem,Player pPlayer){
        if(pItem.hasItemMeta()){
            boolean changed=false;
            String tStr;
            ItemMeta meta=pItem.getItemMeta();
            //设置骷髅所有者
            if(meta instanceof SkullMeta){
                SkullMeta skullMeta=(SkullMeta)meta;
                if(skullMeta.hasOwner()){
                    String newOwner=BossShop.replaceParam(pPlayer,skullMeta.getOwner());
                    if(!newOwner.equals(skullMeta.getOwner())){
                        skullMeta.setOwner(BossShop.replaceParam(pPlayer,skullMeta.getOwner()));
                        changed=true;
                    }
                }
            }
            if(meta.hasDisplayName()){
                tStr=BossShop.replaceParam(pPlayer,meta.getDisplayName());
                if(!tStr.equals(meta.getDisplayName())){
                    meta.setDisplayName(tStr);
                    changed=true;
                }
            }
            if(meta.hasLore()){
                List<String> new_lore=new ArrayList<String>();
                for(String sLore : meta.getLore()){
                    tStr=BossShop.replaceParam(pPlayer,sLore);
                    if(!tStr.equals(sLore)){
                        changed=true;
                    }
                    new_lore.add(tStr);
                }
                meta.setLore(new_lore);
            }
            if(changed&&!BukkitUtil.isItemMetaEmpty(meta))
                pItem.setItemMeta(meta);
        }
        return pItem;
    }

}
