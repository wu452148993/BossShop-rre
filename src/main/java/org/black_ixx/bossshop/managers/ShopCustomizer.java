package org.black_ixx.bossshop.managers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSGoods;
import org.black_ixx.bossshop.core.BSShop;
import org.black_ixx.bossshop.core.BSShopHolder;
import org.black_ixx.bossshop.events.BSDisplayItemEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import cc.bukkitPlugin.commons.plugin.manager.AManager;
import cc.bukkitPlugin.commons.util.BukkitUtil;

/**
 * 定制专属于某个玩家的商店
 * @author 聪聪
 *
 */
public class ShopCustomizer extends AManager<BossShop>{

    private final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public ShopCustomizer(BossShop pPlugin){
        super(pPlugin);
    }

    private String C(String key,Player player){
        return BossShop.replaceParam(player,this.mPlugin.getLangManager().getNode(key));
    }

    /**
     * 根据指定的玩家,商店创建物品到指定的背包
     * @param pShop 商店
     * @param pPlayer 玩家
     * @param pInventory 背包
     * @param pPoss 要显示的物品的位置
     */
    public Inventory updateInventory(BSShopHolder pInvHolder){
        Inventory tInv=pInvHolder.getInventory();
        Player tOwner=pInvHolder.getOwner();
        if(tInv==null||tOwner==null)
            return null;

        tInv.clear();
        BSShop tShop=pInvHolder.getShop();
        for(Map.Entry<Integer,String> sEntry : pInvHolder.getDisplayMap().entrySet()){
            BSGoods tGoods=tShop.getGoods(sEntry.getValue());
            if(tGoods!=null){
                this.addGoodsToInv(tOwner,tInv,sEntry.getKey(),tGoods);
            }else{
                pInvHolder.removeGoods(sEntry.getValue());
            }
        }
        return tInv;
    }

    public Inventory updateInventory(Player pOwner,Inventory pInv,Map<Integer,BSGoods> pGoodses){
        if(pInv==null||pOwner==null)
            return null;

        pInv.clear();
        for(Map.Entry<Integer,BSGoods> sEntry : pGoodses.entrySet()){
            this.addGoodsToInv(pOwner,pInv,sEntry.getKey(),sEntry.getValue());
        }
        return pInv;
    }

    public ItemStack addGoodsToInv(Player pOwner,Inventory tInv,int pPosition,BSGoods pGoods){
        if(pPosition>=tInv.getSize())
            return null;
        if(pGoods==null||!pGoods.disaplyToPlayer(pOwner))
            return null;

        ItemStack tMenuItem=pGoods.getMenuItem();
        if(pGoods.isNeedEdit()){
            tMenuItem=this.createPersonalMenuItem(pOwner,pGoods,tMenuItem);
        }

        tInv.setItem(pPosition,tMenuItem);
        return tMenuItem;
    }

    /**
     * 根据玩家信息,商品信息设置菜单物品Lore
     * <p>此函数只管设置lore,不会检测是否隐藏物品</p>
     * @param pPlayer 玩家
     * @param pBuy 商品
     * @param pMenuItem 菜单物品
     * @return 肯定不是null
     */
    public ItemStack createPersonalMenuItem(Player pPlayer,BSGoods pBuy,ItemStack pMenuItem){
        BSShop pShop=pBuy.getShop();
        BSDisplayItemEvent event=new BSDisplayItemEvent(pPlayer,pShop,pBuy);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled())
            return null;
        ItemMeta meta=pMenuItem.getItemMeta();
        //设置骷髅所有者
        if(meta instanceof SkullMeta){
            SkullMeta skullMeta=(SkullMeta)meta;
            if(skullMeta.hasOwner())
                skullMeta.setOwner(BossShop.replaceParam(pPlayer,skullMeta.getOwner()));
        }
        //设置lore和物品名字
        if(meta.hasDisplayName())
            meta.setDisplayName(BossShop.replaceParam(pPlayer,meta.getDisplayName()));
        List<String> lore=meta.getLore();
        if(lore!=null){
            int co=0;
            for(String line : lore){
                lore.set(co,BossShop.replaceParam(pPlayer,line));
                co++;
            }
        }else lore=new ArrayList<String>();
        // 设置右键购买数量
        if(pBuy.getRightClickBuyCount(pPlayer)>1&&pBuy.showRightClickBuyCount())
            lore.add(this.C("LoreRightClickBuyNumb",pPlayer).replace("%numb%",pBuy.getRightClickBuyCount(pPlayer)+""));
        // 设置SHIFT右键购买全部
        if(pBuy.allowShiftLeftClickBuy())
        	lore.add(this.C("LoreShiftLeftClickBuyNumb",pPlayer));
        // 设置剩余数量,售卖时间
        int limit=pBuy.getLimit();
        int perLimit=pBuy.getPersonalLimit();
        Date start_t=pBuy.getStartTime();
        Date stop_t=pBuy.getStopTime();
        ArrayList<String> addlore=new ArrayList<>();
        String faillore=null;
        //设置剩余数量
        if(limit>-1){
            if(limit>0)
                addlore.add(C("LoreGoodsStockLeft",pPlayer).replace("%numb%",limit+""));
            else faillore=C("LoreAlreadySoldOut",pPlayer);
        }
        //设置个人剩余数量
        if(faillore==null&&perLimit>-1){
            int leftbuy=perLimit-this.mPlugin.getManager(RecordManager.class).getBuyRecord(pShop.getShopName(),pBuy.getName(),pPlayer.getUniqueId());
            if(leftbuy<0)
                leftbuy=0;
            if(leftbuy==0)
                faillore=C("MsgOutOfPersonalLimit",pPlayer);
            else addlore.add(C("LoreEveryoneLimit",pPlayer).replace("%numb%",perLimit+"").replace("%left%",leftbuy+""));
        }
        //设置开始和结束时间
        if(faillore==null&&(start_t!=null||stop_t!=null)){
            Date date=new Date();
            if(start_t==null||date.after(start_t)){
                if(stop_t==null||date.before(stop_t)){
                    addlore.add(C("LoreNowCanBuyGoods",pPlayer));
                    if(stop_t!=null)
                        addlore.add(C("LoreWillCloseInTime",pPlayer).replaceAll("%time%",getTimeString(stop_t)));
                }else faillore=C("LoreGoodsOutOfDate",pPlayer);
            }else faillore=C("LoreBuyIsNotTime",pPlayer).replaceAll("%time%",getTimeString(start_t));
        }
        if(faillore==null)
            lore.addAll(addlore);
        else lore.add(faillore);
        if(lore.size()!=0)
            meta.setLore(lore);
        if(!BukkitUtil.isItemMetaEmpty(meta))
            pMenuItem.setItemMeta(meta);
        return pMenuItem;
    }

    private String getTimeString(Date date){
        return sdf.format(date);
    }

}
