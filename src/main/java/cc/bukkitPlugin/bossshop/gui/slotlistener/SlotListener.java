package cc.bukkitPlugin.bossshop.gui.slotlistener;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.commons.util.ToolKit;

public abstract class SlotListener{

    public final Inventory mInv;
    public final int mSlotIndex;

    public SlotListener(Inventory pInv,int pSlotIndex){
        this.mInv=pInv;
        this.mSlotIndex=ToolKit.between(0,this.mInv.getSize()-1,pSlotIndex);
    }

    /** 是否接受一个物品,只检查类型等限制,不要检查数量 */
    public abstract boolean acceptThisItem(ItemStack pItem);

    /** 在Slot中有当前物品时,可以将此物品放入此Slot中几件,可能返回比物品此数量大的值 */
    public int allowPushAmountNow(ItemStack pItem){
        if(this.acceptThisItem(pItem)){
            ItemStack tExistItem=this.getItem();
            if(pItem.isSimilar(tExistItem)){
                return ToolKit.between(0,pItem.getMaxStackSize(),pItem.getMaxStackSize()-tExistItem.getAmount());
            }else if(BukkitUtil.isInvalidItem(tExistItem)){
                return pItem.getAmount();
            }else return 0;
        }
        return 0;
    }

    /** 在Slot中无任何物品时,可以将此物品放入此Slot中几件,可能返回比物品此数量大的值 */
    public int allowPushAmountEmpty(ItemStack pItem){
        return this.acceptThisItem(pItem)?pItem.getMaxStackSize():0;
    }

    /**
     * 不检查物品存入限制,如果有物品但不相同,将会被直接覆盖
     */
    public void push(ItemStack pItem){
        if(BukkitUtil.isInvalidItem(pItem))
            return;

        ItemStack tItem=this.getItem();
        if(BukkitUtil.isValidItem(tItem)&&pItem.isSimilar(tItem)){
            pItem.setAmount(pItem.getAmount()+tItem.getAmount());
        }
        this.setItem(pItem);
    }

    public abstract boolean allowPopItem();

    /** 处理点击事件,如果返回false,将终止后续操作并禁止事件 */
    public boolean onClick(int pRawSlotIndex){
        return true;
    }

    /** 取出多少物品,<=0表示取出所有物品,不检查pop限制 */
    public ItemStack pop(int pAmount){
        if(pAmount<=0)
            pAmount=Integer.MAX_VALUE;
        ItemStack tItem=this.getItem();
        if(BukkitUtil.isInvalidItem(tItem))
            return new ItemStack(Material.AIR,0,(short)0);
        pAmount=Math.min(tItem.getAmount(),pAmount);
        ItemStack tPopItem=tItem.clone();
        tPopItem.setAmount(pAmount);
        tItem.setAmount(tItem.getAmount()-pAmount);
        this.setItem(tItem);
        return tPopItem;
    }

    /**
     * 不检查物品存入限制
     */
    public void setItem(ItemStack pItem){
        if(BukkitUtil.isInvalidItem(pItem)){
            pItem=null;
        }
        this.mInv.setItem(this.mSlotIndex,pItem);
    }

    public ItemStack getItem(){
        return this.mInv.getItem(this.mSlotIndex);
    }

    /** 背包关闭时,弹出所有该监听器的物品 */
    public ItemStack getItemOnClose(){
        ItemStack tItem=this.getItem();
        this.setItem(null);
        return tItem;
    }

    public boolean isEmpty(){
        return !BukkitUtil.isValidItem(this.mInv.getItem(this.mSlotIndex));
    }

    public int getItemAmount(){
        ItemStack tItem=this.getItem();
        return BukkitUtil.isValidItem(tItem)?tItem.getAmount():0;
    }

}
