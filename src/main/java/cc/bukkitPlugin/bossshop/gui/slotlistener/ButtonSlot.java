package cc.bukkitPlugin.bossshop.gui.slotlistener;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ButtonSlot extends SlotListener{

    /** 按钮事件接收器 */
    private IButtonRecive mHandler=null;
    /** 是否复用按钮,复用按钮时,内部的Slot索引将无效 */
    private boolean mMultiplex=false;

    public ButtonSlot(Inventory pInv,int pSlotIndex){
        super(pInv,pSlotIndex);
    }

    public ButtonSlot(Inventory pInv,IButtonRecive pHandler){
        this(pInv,0,pHandler);
    }

    public ButtonSlot(Inventory pInv,int pSlotIndex,IButtonRecive pHandler){
        super(pInv,pSlotIndex);
        this.mHandler=pHandler;
    }

    @Override
    public boolean onClick(int pRawSlotIndex){
        if(this.mHandler!=null){
            this.mHandler.onButtonClick(this,pRawSlotIndex);
            return true;
        }else{
            return super.onClick(pRawSlotIndex);
        }
    }

    @Override
    public boolean acceptThisItem(ItemStack pItem){
        return false;
    }

    @Override
    public boolean allowPopItem(){
        return false;
    }

    @Override
    public ItemStack pop(int pAmount){
        return null;
    }

    @Override
    public void push(ItemStack pItem){
        return;
    }

    @Override
    public int allowPushAmountEmpty(ItemStack pItem){
        return 0;
    }

    @Override
    public int allowPushAmountNow(ItemStack pItem){
        return 0;
    }

    @Override
    public ItemStack getItemOnClose(){
        return null;
    }

}
