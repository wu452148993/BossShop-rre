package org.black_ixx.bossshop.managers;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import cc.bukkitPlugin.bossshop.goods.price.PriceItem.MatchKind;
import cc.bukkitPlugin.bossshop.goods.price.PriceItem.PriceInfo;
import cc.bukkitPlugin.commons.plugin.manager.AManager;
import cc.bukkitPlugin.commons.util.BukkitUtil;

/**
 * 用于背包物品检查,背包物品收取
 * 
 * @author 聪聪
 */
public class ItemStackChecker extends AManager<BossShop>{

    public static interface MatchAction{

        boolean isMatch(ItemStack pItem1,ItemStack pItem2);

    }

    public ItemStackChecker(BossShop pPlugin){
        super(pPlugin);
    }

    /** 检查两个物品是否完全相同,不比较数量 */
    public static boolean isSame(ItemStack pItem1,ItemStack pItem2){
        if(!BukkitUtil.isValidItem(pItem1))
            return !BukkitUtil.isValidItem(pItem2);

        if(!BukkitUtil.isValidItem(pItem2))
            return false;
        return pItem1.isSimilar(pItem2);
    }

    /**
     * 检查两个物品的是否相似<br>
     * 只对Lore和name进行比较,不比较数量和其他NBT标签
     */
    public static boolean isSimilar(ItemStack pItem1,ItemStack pItem2){
        if(!BukkitUtil.isValidItem(pItem1))
            return !BukkitUtil.isValidItem(pItem2);

        if(!BukkitUtil.isValidItem(pItem2))
            return false;

        if(pItem1.getType()!=pItem2.getType()||pItem1.getDurability()!=pItem2.getDurability())
            return false;

        if(pItem1.hasItemMeta()==pItem2.hasItemMeta()){
            if(pItem1.hasItemMeta()){
                ItemMeta tMeta1=pItem1.getItemMeta();
                ItemMeta tMeta2=pItem2.getItemMeta();

                if(tMeta1.hasDisplayName()&&tMeta1.getDisplayName().equals(tMeta2.getDisplayName())){
                    return tMeta1.hasLore()?(tMeta1.getLore().equals(tMeta2.getLore())):!tMeta2.hasLore();
                }else return false; // 名字不相同,不相同

            }else return true; // 无NBT,相同
        }else return false; // 一个有NBT,一个无NBT
    }

    /**
     * 检查玩家背包中是否存在指数量的物品
     * 
     * @param pPlayer
     *            玩家
     * @param pItem
     *            物品,忽略数量
     * @param pAmount
     *            数量
     * @return 是否存在
     */
    public static boolean inventoryContainsItem(Player pPlayer,ItemStack pItem,int pAmount){
        return ItemStackChecker.inventoryContainsItem(pPlayer,pItem,pAmount,(pItem1,pItem2)->ItemStackChecker.isSame(pItem1,pItem2));
    }

    /**
     * 检查玩家背包中是否存在指数量的物品
     * 
     * @param pPlayer
     *            玩家
     * @param pInfo
     *            物品信息
     * @return 是否存在
     */
    public static boolean inventoryContainsItem(Player pPlayer,PriceInfo pInfo){
        return ItemStackChecker.inventoryContainsItem(pPlayer,pInfo.mPriceItem,pInfo.mAmount,(pItem1,pItem2)->{
            for(MatchKind sKinds : pInfo.mMatchKinds){
                if(!sKinds.isMatch(pItem1,pItem2)) return false;
            }
            return true;
        });
    }

    /**
     * 检查玩家背包中是否存在指数量的物品
     * 
     * @param pPlayer
     *            玩家
     * @param pItem
     *            物品,忽略数量
     * @param pAmount
     *            数量
     * @param pMatchAction
     *            物品匹配规则
     * @return 是否存在
     */
    public static boolean inventoryContainsItem(Player pPlayer,ItemStack pItem,int pAmount,MatchAction pMatchAction){
        if(pPlayer==null) return false;
        int existAmount=0;
        int takeAmount=Math.max(1,pAmount);

        for(ItemStack sInvItem : pPlayer.getInventory().getContents()){
            if(!BukkitUtil.isValidItem(sInvItem))
                continue;
            if(pMatchAction.isMatch(sInvItem,pItem))
                existAmount+=Math.max(0,sInvItem.getAmount());
            if(takeAmount<=existAmount)
                return true;
        }
        return false;
    }

    /**
     * 从玩家背包中拿走指定数量的物品
     * 
     * @param pPlayer
     *            玩家
     * @param pItem
     *            物品,忽略数量
     * @param pAmount
     *            要取走的数量
     * @return 是否已经取走足够数量的物品
     */
    public static boolean takeItem(Player pPlayer,PriceInfo pInfo){
        return ItemStackChecker.takeItem(pPlayer,pInfo.mPriceItem,pInfo.mAmount,(pItem1,pItem2)->{
            for(MatchKind sKinds : pInfo.mMatchKinds){
                if(!sKinds.isMatch(pItem1,pItem2)) return false;
            }
            return true;
        });
    }
    
    /**
     * 从玩家背包中拿走指定数量的物品
     * 
     * @param pPlayer
     *            玩家
     * @param pItem
     *            物品,忽略数量
     * @param pAmount
     *            要取走的数量
     * @return 是否已经取走足够数量的物品
     */
    public static boolean takeItem(Player pPlayer,ItemStack pItem,int pAmount){
        return ItemStackChecker.takeItem(pPlayer,pItem,pAmount,(pItem1,pItem2)->ItemStackChecker.isSame(pItem1,pItem2));
    }

    /**
     * 从玩家背包中拿走指定数量的物品
     * 
     * @param pPlayer
     *            玩家
     * @param pItem
     *            物品,忽略数量
     * @param pAmount
     *            要取走的数量
     * @param pMatchAction
     *            物品匹配规则
     * @return 是否已经取走足够数量的物品
     */
    public static boolean takeItem(Player pPlayer,ItemStack pItem,int pAmount,MatchAction pMatchAction){
        int alreadyTakeAmount=0;
        int takeAmount=Math.max(1,pAmount);

        ItemStack[] tInvItems=pPlayer.getInventory().getContents();
        int invIndex=0;
        for(;invIndex<tInvItems.length;invIndex++){
            ItemStack tInvItem=tInvItems[invIndex];
            if(!BukkitUtil.isValidItem(tInvItem))
                continue;

            if(!pMatchAction.isMatch(pItem,tInvItem))
                continue;

            int amount=Math.max(0,tInvItem.getAmount());
            int remove=alreadyTakeAmount+amount<=takeAmount?amount:takeAmount-alreadyTakeAmount;
            alreadyTakeAmount+=remove;
            tInvItem.setAmount(amount-remove);
            tInvItems[invIndex]=amount-remove==0?null:tInvItem;
            if(alreadyTakeAmount>=takeAmount){
                break;
            }
        }

        if(alreadyTakeAmount>=takeAmount){
            pPlayer.getInventory().setContents(tInvItems);
            return true;
        }else return false;
    }

    public static boolean isValidEnchantment(ItemStack item,Enchantment enchantment,int level){
        try{
            item.clone().addEnchantment(enchantment,level);
        }catch(Exception e){
            return false;
        }
        return true;
    }

}
