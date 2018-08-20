package cc.bukkitPlugin.bossshop.gui.holder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import cc.bukkitPlugin.bossshop.gui.GuiFactory;
import cc.bukkitPlugin.bossshop.gui.GuiType;
import cc.bukkitPlugin.bossshop.gui.slotlistener.ButtonSlot;
import cc.bukkitPlugin.bossshop.gui.slotlistener.IButtonRecive;
import cc.bukkitPlugin.bossshop.gui.slotlistener.SlotListener;
import cc.bukkitPlugin.bossshop.mail.MailItem;
import cc.bukkitPlugin.bossshop.mail.MailManager;
import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.commons.util.ToolKit;

public class MailHolder extends AInvHolder implements Listener,IButtonRecive{

    /** 每页的邮件显示数量,位置从0开始 */
    public static final int MAIL_PREPAGE=21;

    /** Slot对应的合成表Key */
    private HashMap<Integer,MailItem> mMailMap=new HashMap<>();
    /** 合成物品列表界面 */
    private SlotListener mButtonPrePage=null;
    private SlotListener mButtonNextPage=null;
    private SlotListener mButtonRecive=null;
    private SlotListener mButtonReciveAll=null;
    /** 当前页码 */
    private int mCurrentPage=0;
    private MailManager mMailMan;

    public MailHolder(BossShop pPlugin,Player pOwner){
        super(pPlugin,pOwner);

        this.mMailMan=this.mPlugin.getManager(MailManager.class);
        this.mPlugin.registerEvents(this);
    }

    @Override
    public void initHolder(Inventory pInv){
        this.setOpenInv(pInv);

        // 初始化按钮
        this.addSlotListener(this.mButtonPrePage=new ButtonSlot(this.mInv,39,this));
        this.addSlotListener(this.mButtonNextPage=new ButtonSlot(this.mInv,41,this));
        this.addSlotListener(this.mButtonReciveAll=new ButtonSlot(this.mInv,40,this));

        this.mButtonRecive=new ButtonSlot(this.mInv,this);
        for(int i=0;i<MAIL_PREPAGE;i++){
            this.addSlotListener(zeroIndexTo21Slot(i),this.mButtonRecive);
        }

        this.initGui();

        this.openMailPage(0,true);
    }

    @Override
    public void initGui(){
        int tInvSize=this.mInv.getSize();
        for(int i=0;i<tInvSize;i++){
            if(this.mSlotListener.get(i)!=this.mButtonRecive){
                this.mInv.setItem(i,this.getGuiTexture(i));
            }
        }
    }

    public static int zeroIndexTo21Slot(int pIndex){
        return (pIndex/7+1)*9+(pIndex%7+1);
    }

    /**
     * 添加背包Slot监听器
     * 
     * @param pListener
     *            Slot监听器
     */
    protected void addSlotListener(SlotListener pListener){
        this.mSlotListener.put(pListener.mSlotIndex,pListener);
    }

    /**
     * 添加背包Slot监听器
     * 
     * @param pListener
     *            Slot监听器
     */
    protected void addSlotListener(int pSlot,SlotListener pListener){
        this.mSlotListener.put(pSlot,pListener);
    }

    @Override
    public void onButtonClick(ButtonSlot pButton,int pSlotIndex){
        if(pButton==this.mButtonRecive){
            this.reciveMail(pSlotIndex);
        }else if(pButton==this.mButtonReciveAll){
            this.mMailMan.reciveMail(this.mOwner,(String)null);
            this.closeInvNextTick();
        }else if(pButton==this.mButtonPrePage){
            this.openMailPage(this.mCurrentPage-1,false);
        }else if(pButton==this.mButtonNextPage){
            this.openMailPage(this.mCurrentPage+1,false);
        }
    }

    /**
     * 处理背包关闭事件
     */
    public void handleInvClose(InventoryView pView){
        HandlerList.unregisterAll(this);
        super.handleInvClose(pView);
    }

    @EventHandler(priority=EventPriority.NORMAL)
    public void onInvClose(InventoryCloseEvent pEvent){
        if(!this.isThisInv(pEvent.getInventory()))
            return;

        this.handleInvClose(pEvent.getView());
    }

    @EventHandler(priority=EventPriority.NORMAL)
    public void onInvDrag(InventoryDragEvent pEvent){
        if(!this.isThisInv(pEvent.getInventory()))
            return;

        int tUpInvSize=pEvent.getView().getTopInventory().getSize();
        for(Map.Entry<Integer,ItemStack> sEntry : pEvent.getNewItems().entrySet()){
            if(BukkitUtil.isInvalidItem(sEntry.getValue())||sEntry.getKey()>=tUpInvSize)
                continue;

            pEvent.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority=EventPriority.NORMAL)
    public void onInvClick(InventoryClickEvent pEvent){
        if(!this.isThisInv(pEvent.getInventory()))
            return;

        int tRawSlot=pEvent.getRawSlot();
        boolean tUpInv=tRawSlot<pEvent.getView().getTopInventory().getSize();
        try{
            switch(pEvent.getAction()){
                case DROP_ONE_CURSOR:
                case NOTHING:
                    return;
                case COLLECT_TO_CURSOR:
                    if(!tUpInv&&!this.upInvExistSameItemNotAllowPop(pEvent.getCurrentItem())){
                        return;
                    }
                    break;
                case DROP_ALL_CURSOR:
                    if(!pEvent.isShiftClick()||!this.upInvExistSameItemNotAllowPop(pEvent.getCursor())){
                        return;
                    }
                    break;
                case DROP_ALL_SLOT:
                case DROP_ONE_SLOT:
                case HOTBAR_SWAP:
                    if(!tUpInv){
                        return;
                    }
                    break;
                case PICKUP_ALL:
                case PICKUP_HALF:
                case PICKUP_ONE:
                case PICKUP_SOME:
                case PLACE_ALL:
                case PLACE_ONE:
                case PLACE_SOME:
                case SWAP_WITH_CURSOR:
                    if(!tUpInv){
                        return;
                    }else{
                        SlotListener tListener=this.mSlotListener.get(tRawSlot);
                        if(tListener!=null){
                            tListener.onClick(tRawSlot);
                        }
                    }
                    break;
                case MOVE_TO_OTHER_INVENTORY:
                case HOTBAR_MOVE_AND_READD:
                case CLONE_STACK:
                case UNKNOWN:
                default:
            }
        }catch(Throwable exp){
            Log.send(this.mOwner,this.mPlugin.C("MsgErrorHappendPleaseContanctAdmin"));
            Log.severe("处理背包点击事件时发生了错误",exp);
        }
        pEvent.setCancelled(true);
    }

    /**
     * 打开所有物品合成表视图
     * 
     * @param pPage
     *            页码
     * @param pForce
     *            是否在即使页码与当前相同时也进行强制的刷新
     */
    public void openMailPage(int pPage,boolean pForce){
        ArrayList<MailItem> tMails=this.mMailMan.getPlayerMails(this.mOwner.getName(),(String)null);
        int tPageAmount=(tMails.size()+MAIL_PREPAGE-1)/MAIL_PREPAGE;
        pPage=ToolKit.between(0,tPageAmount-1,pPage);
        if(!pForce&&pPage==this.mCurrentPage)
            return;

        this.mCurrentPage=pPage;
        this.mMailMap.clear();
        for(int i=0;i<MAIL_PREPAGE;i++){
            int tSlot=zeroIndexTo21Slot(i);
            if(BukkitUtil.isValidItem(this.mInv.getItem(tSlot))){
                this.mInv.setItem(tSlot,null);
            }
        }

        int tLimit=Math.min(MAIL_PREPAGE,tMails.size()-pPage*MAIL_PREPAGE);
        int dI=pPage*MAIL_PREPAGE;
        for(int i=0;i<tLimit;i++){
            MailItem tRecipe=tMails.get(i+dI);
            int tSlot=zeroIndexTo21Slot(i);
            this.mInv.setItem(tSlot,tRecipe.generateItem(this.mOwner));
            this.mMailMap.put(tSlot,tRecipe);
        }

        int tSlot=this.mButtonPrePage.mSlotIndex;
        this.mInv.setItem(tSlot,this.mCurrentPage==0?this.getGuiTexture(100+tSlot):this.getGuiTexture(tSlot));
        tSlot=this.mButtonNextPage.mSlotIndex;
        this.mInv.setItem(tSlot,this.mCurrentPage>=tPageAmount-1?this.getGuiTexture(100+tSlot):this.getGuiTexture(tSlot));
    }

    /**
     * 使用指定位置的合成表切换背包到合成物品状态
     * 
     * @param pSlot
     *            当前背包位置
     */
    public void reciveMail(int pSlot){
        MailItem tMail=this.mMailMap.get(pSlot);
        if(tMail==null) return;

        this.mMailMan.reciveMail(this.mOwner,tMail.getName());
        this.onMailChange();
    }

    /**
     * 获取当前背包状态下,指定位置的物品材质
     * 
     * @param pSlot
     *            背包位置
     * @return 物品材质
     */
    public ItemStack getGuiTexture(int pSlot){
        return getGuiTexture(pSlot,GuiType.Mail);
    }

    /**
     * 获取指定背包类型指定位置的物品材质
     * 
     * @param pSlot
     *            背包位置
     * @param pType
     *            背包类型
     * @return 物品材质
     */
    public static ItemStack getGuiTexture(int pSlot,GuiType pType){
        return GuiFactory.getInstance().getGuiTexture(pSlot,pType);
    }

    /**
     * 在合成表重载后调用
     */
    public void onMailChange(){
        this.openMailPage(this.mCurrentPage,true);
    }

}
