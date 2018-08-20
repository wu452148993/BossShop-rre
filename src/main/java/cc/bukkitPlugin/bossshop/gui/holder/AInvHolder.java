package cc.bukkitPlugin.bossshop.gui.holder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import cc.bukkitPlugin.bossshop.gui.slotlistener.SlotListener;
import cc.bukkitPlugin.bossshop.gui.tasks.CloseInvTask;
import cc.bukkitPlugin.bossshop.gui.tasks.UpdateInvTask;
import cc.bukkitPlugin.commons.util.BukkitUtil;

public abstract class AInvHolder implements InventoryHolder{

    protected BossShop mPlugin;
    /** 所有者 */
    protected final Player mOwner;
    /** 打开的背包 */
    protected Inventory mInv=null;
    /** Slot监听器 */
    protected HashMap<Integer,SlotListener> mSlotListener=new HashMap<>();

    public AInvHolder(BossShop pPlugin,Player pOwner){
        this.mPlugin=pPlugin;
        this.mOwner=pOwner;
    }

    @Override
    public Inventory getInventory(){
        return this.mInv;
    }

    public boolean isThisInv(Inventory pInv){
        return pInv!=null&&pInv.getHolder()==this;
    }

    /**
     * 设置打开的背包,只允许设置一次
     * 
     * @param pInv
     *            打开的背包
     */
    public void setOpenInv(Inventory pInv){
        if(pInv!=null){
            if(this.mInv!=null)
                throw new IllegalStateException("不能重复为Holder设置打开的背包");
            this.mInv=pInv;
        }
    }

    /**
     * 在下次tick时关闭背包,同时设置{@link #mMarkClose}为true
     */
    protected void closeInvNextTick(){
        new CloseInvTask(this.mOwner).runTask(this.mPlugin);
    }

    public void handleInvClose(InventoryView pView){
        List<ItemStack> tReturnItem=this.getItemOnClose(pView);
        for(ItemStack sItem : tReturnItem){
            if(BukkitUtil.isValidItem(sItem)){
                BukkitUtil.giveItem(this.mOwner,sItem);
            }
        }
    }

    /**
     * 获取在背包关闭时应该返回给玩家的物品
     * 
     * @param pView
     *            背包视图
     * @return 需要返还的物品,不为null
     */
    protected List<ItemStack> getItemOnClose(InventoryView pView){
        ArrayList<ItemStack> tReturnItem=new ArrayList<>();

        tReturnItem.add(pView.getCursor());
        pView.setCursor(null);

        for(SlotListener sListener : this.mSlotListener.values()){
            tReturnItem.add(sListener.getItemOnClose());
        }
        return tReturnItem;
    }

    /**
     * 上面的背包是否存在相同物品但是不允许pop的
     * 
     * @param pItem
     *            pop的物品
     * @return 是否
     */
    public boolean upInvExistSameItemNotAllowPop(ItemStack pItem){
        for(int i=0;i<this.mInv.getSize();i++){
            ItemStack tItem=this.mInv.getItem(i);
            if(BukkitUtil.isValidItem(tItem)&&tItem.isSimilar(pItem)){
                SlotListener tListener=this.mSlotListener.get(1);
                if(tListener==null||!tListener.allowPopItem()){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 安全的设置指定监听Slot的物品
     * 
     * @param pSlotListener
     *            Slot监听器
     * @param pItem
     *            物品
     */
    public void safeSetSlotItem(SlotListener pSlotListener,ItemStack pItem){
        if(BukkitUtil.isInvalidItem(pItem))
            return;
        if(pSlotListener.isEmpty()){
            pSlotListener.setItem(pItem);
            new UpdateInvTask(this.mOwner);
        }else{
            BukkitUtil.giveItem(this.mOwner,pItem);
        }
    }

    /**
     * 初始化,包括设置背包,设置Slot监听器
     * 
     * @param pInv
     *            绑定的背包
     */
    public abstract void initHolder(Inventory pInv);

    /**
     * 初始化或者重新设置界面
     * <p>
     * GuiFactory只会在Gui重载后才会调用此函数
     * </p>
     */
    public abstract void initGui();

}
