package org.black_ixx.bossshop.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.managers.ShopCustomizer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * used by Inventory of BSShop
 */

public class BSShopHolder implements InventoryHolder{

    protected BossShop mPlugin;
    protected final BSShop mShop;
    protected final Player mOwner;
    protected Inventory mInv;
    protected Map<Integer,String> mDisplayPos=Collections.synchronizedMap(new TreeMap<Integer,String>());

    public BSShopHolder(BossShop pPlugin,Player pOwner,BSShop pShop){
        this.mPlugin=pPlugin;
        this.mOwner=pOwner;
        this.mShop=pShop;

        this.setShopItemToDisplayPos();
    }

    protected void setShopItemToDisplayPos(){
        this.mDisplayPos.clear();
        synchronized(this.mShop){
            for(Entry<Integer,ArrayList<BSGoods>> sEntry : this.mShop.getGoodsesPosition().entrySet()){
                for(BSGoods sGoods : sEntry.getValue()){
                    this.mDisplayPos.put(sEntry.getKey(),sGoods.getName());
                    break;
                }
            }
        }
    }

    /**
     * 获取holder所持有的背包
     */
    @Override
    public Inventory getInventory(){
        return mInv;
    }

    /**
     * 设置holder的背包,必须调用
     * 
     * @param pInv
     *            要设置成的背包
     */
    public void setInventory(Inventory pInv){
        this.mInv=pInv;
    }

    /**
     * 添加一个商品到玩家打开的背包
     */
    public void addGoods(BSGoods pGoods){
        String tGoodsName=pGoods.getName();
        synchronized(this.mDisplayPos){
            if(this.mDisplayPos.containsValue(tGoodsName))
                return;

            int tEmptySlot=0;

            for(int sSlot : this.mDisplayPos.keySet()){
                if(sSlot!=tEmptySlot){
                    break;
                }
                tEmptySlot++;
            }

            if(tEmptySlot<this.mInv.getSize()-2){
                this.mDisplayPos.put(tEmptySlot,tGoodsName);
                this.mPlugin.getManager(ShopCustomizer.class).addGoodsToInv(this.mOwner,this.mInv,tEmptySlot,pGoods);
            }
        }
    }

    /**
     * 从玩家打开的商店中移除一个商品
     */
    public String removeGoods(BSGoods pGoods){
        return this.removeGoods(pGoods.getName());
    }

    /**
     * 从玩家打开的商店中移除一个商品
     */
    public String removeGoods(String pGoodsName){
        Iterator<Map.Entry<Integer,String>> it=this.mDisplayPos.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<Integer,String> tEntry=it.next();
            if(tEntry.getValue().equals(pGoodsName)){
                this.mInv.setItem(tEntry.getKey(),null);
                String removeName=tEntry.getValue();
                it.remove();
                return removeName;
            }
        }
        return null;
    }

    public ItemStack getMenuItem(int pPos){
        BSGoods tGoods=this.getGoods(pPos);
        if(tGoods==null)
            return null;
        return tGoods.getMenuItem();
    }

    /**
     * 获取在第pPosition格的物品
     * <p>
     * 此函数获取的物品只要对应的背包序号对照表中存在物品则返回
     * </p>
     */
    public BSGoods getGoods(int pPosition){
        return this.mShop.getGoods(this.mDisplayPos.get(pPosition));
    }

    /**
     * 获取在第pPosition格的物品
     * <p>
     * 注意,此函数获取的物品将可能受到Holder状态的影响
     * </p>
     */
    public BSGoods getDisplayItemAt(int pPosition){
        return this.getGoods(pPosition);
    }

    /**
     * 获取该背包显示物品的位置的Set的拷贝
     */
    public Set<Integer> getDisplayPositions(){
        HashSet<Integer> t=new HashSet<>(this.mDisplayPos.keySet());
        return t;
    }

    /**
     * 请务必返回TreeMap或其包装类
     */
    public Map<Integer,String> getDisplayMap(){
        HashMap<Integer,String> a=new HashMap<>();
        a.putAll(this.mDisplayPos);
        return a;
    }

    /**
     * 获取holder所关联的商店
     */
    public BSShop getShop(){
        return mShop;
    }

    /**
     * 获取holder所关联的玩家
     */
    public Player getOwner(){
        return this.mOwner;
    }

    /**
     * 将定制的物品放入到背包中
     * 
     * @param pReset
     *            是否重置背包,此项参数常用于商店重载时使用
     * @return 如果商店不存在false,否则true
     */
    public boolean updateInventory(boolean pReset){
        this.mInv.clear();

        if(!this.mShop.isShopValid()){
            this.mOwner.closeInventory();
            return false;
        }

        if(this.mShop.getInventorySize()<=this.mInv.getSize()){
            if(pReset){
                this.setShopItemToDisplayPos();
            }
            BossShop.getInstance().getManager(ShopCustomizer.class).updateInventory(this);
        }else{
            this.mOwner.closeInventory();
            this.mShop.openInventory(this.mOwner);
        }
        return true;
    }

}
