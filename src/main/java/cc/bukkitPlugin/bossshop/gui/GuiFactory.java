package cc.bukkitPlugin.bossshop.gui;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import cc.bukkitPlugin.bossshop.gui.holder.AInvHolder;
import cc.bukkitPlugin.bossshop.gui.holder.MailHolder;
import cc.bukkitPlugin.bossshop.util.ItemLoader;
import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.INeedClose;
import cc.bukkitPlugin.commons.plugin.INeedReload;
import cc.bukkitPlugin.commons.plugin.Status;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.AFileManager;
import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.bukkitPlugin.commons.util.BukkitUtil.Task;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.util.StringUtil;

public class GuiFactory extends AFileManager<BossShop> implements INeedReload,INeedClose{

    private static GuiFactory mInstance=null;
    /** 通用界面大小 */
    public static final int GUI_SIZE=5*9;
    public static Pattern RANGE_SLOT=Pattern.compile("(\\d+)-(\\d+)");

    public static GuiFactory getInstance(){
        synchronized(GuiFactory.class){
            if(GuiFactory.mInstance==null){
                GuiFactory.mInstance=new GuiFactory(BossShop.getInstance());
            }
        }
        return GuiFactory.mInstance;
    }

    private ItemStack mDefItem=null;

    protected GuiFactory(BossShop pPlugin){
        super(pPlugin,"Gui.yml","1.0");

        this.mPlugin.registerReloadModel(this);
        this.mPlugin.registerCloseModel(this);
    }

    @Override
    protected void addDefaults(){
        super.addDefaults();

        ItemLoader.addItemTemplate(this.mConfig.getOrCreateSection("ItemTemplate","物品材质配置格式样例"));
        this.mConfig.addDefault("DefaultTexture","BLACK_STAINED_GLASS_PANE;0","所有Gui的默认物品材质","要想使其他Slot使用该物品材质,只需将其值设置为'default'即可");

        CommentedSection tSec=this.mConfig.getOrCreateSection(GuiType.Mail.mYamlKey,
                "邮件界面设置",
                "界面大小为5行,最下面一行为功能区",
                "第40格为上一页,第41格为接收所有邮件,第42格为下一页",
                "第140格为上一页按钮无效时的材质,第142格为下一页按钮无效时的材质");
        tSec.addDefault("DefaultTexture","BLACK_STAINED_GLASS_PANE;0","合成表列出界面默认材质");
        tSec.addDefault("1-45","default");
        tSec.addDefault("40","GLOWSTONE_DUST;0;1;§2上一页");
        tSec.addDefault("41","GLOWSTONE_DUST;0;1;§2接受全部");
        tSec.addDefault("42","REDSTONE;0;1;§2下一页");
    }

    @Override
    public boolean reloadConfig(CommandSender pSender){
        if(!super.reloadConfig(pSender)){
            String tMsg="Gui配置文载入错误"+(pSender instanceof Player?",请查看控制台":"");
            Log.warn(pSender,tMsg);
            return false;
        }

        this.addDefaults();

        Log.info(pSender,"开始载入Gui配置");
        this.mDefItem=ItemLoader.loadItem("DefaultTexture",this.mConfig.get("DefaultTexture"),null);
        for(GuiType sType : GuiType.values()){
            this.loadGuiTexture(sType,this.mConfig.getOrCreateSection(sType.mYamlKey));
        }

        if(!BossShop.mStatusStack.contains(Status.Reload_Plugin)){
            this.onMailChange();
        }

        Log.info(pSender,"Gui配置载入完毕");
        return this.saveConfig(pSender);
    }

    /**
     * 为指定的Gui载入材质
     * 
     * @param pGuiType
     *            Gui类型
     * @param pGuiSec
     *            Gui材质配置节点
     */
    public void loadGuiTexture(GuiType pGuiType,CommentedSection pGuiSec){
        String tPath=pGuiSec.getCurrentPath();
        pGuiType.mSlotTexture.clear();
        for(String sKey : pGuiSec.getKeys(false)){
            if(sKey.equals("DefaultTexture")){
                pGuiType.mDefTexture=ItemLoader.loadItem(tPath,pGuiSec.get("DefaultTexture"),this.mDefItem);
                continue;
            }

            ItemStack tItem=ItemLoader.loadItem(tPath,pGuiSec.get(sKey),pGuiType.mDefTexture);
            if(tItem==null) continue;
            ArrayList<String> tSubStrs=StringUtil.splitNoEmpty(sKey,',');
            for(String sSub : tSubStrs){
                sSub=sSub.trim();
                if(sSub.matches("\\d+")){
                    try{
                        pGuiType.mSlotTexture.put(Integer.parseInt(sSub)-1,tItem);
                    }catch(NumberFormatException ignore){
                    }
                    continue;
                }

                Matcher tMatcher=RANGE_SLOT.matcher(sSub);
                if(tMatcher.find()){
                    int tLowSlot=0,tHighSlot=0;
                    try{
                        tLowSlot=Integer.parseInt(tMatcher.group(1));
                        tHighSlot=Integer.parseInt(tMatcher.group(2));
                    }catch(NumberFormatException ignore){
                        continue;
                    }

                    for(int i=tLowSlot;i<=tHighSlot;i++){
                        pGuiType.mSlotTexture.put(i-1,tItem);
                    }
                }
            }
        }
    }

    @Override
    public void disable(){
        for(Player sPlayer : BukkitUtil.getOnlinePlayers()){
            InventoryView tView=sPlayer.getOpenInventory();
            if(tView.getTopInventory()!=null&&tView.getTopInventory().getHolder() instanceof AInvHolder){
                ((AInvHolder)tView.getTopInventory().getHolder()).handleInvClose(tView);
                sPlayer.closeInventory();
            }
        }
    }

    /**
     * 为玩家打开显示合成物品界面
     * 
     * @param pPlayer
     *            玩家
     */
    public void openMailGui(Player pPlayer){
        MailHolder tHolder=new MailHolder(this.mPlugin,pPlayer);
        Inventory tInv=Bukkit.createInventory(tHolder,GUI_SIZE,this.mPlugin.C("GuiMail"));
        tHolder.initHolder(tInv);
        pPlayer.openInventory(tInv);
    }

    /**
     * 在Gui配置重载时调用
     */
    public void onGuiChange(){
        GuiFactory.onHolderAction(AInvHolder.class,(pHolder)->{
            pHolder.initGui();
            return pHolder;
        });
    }

    /**
     * 在邮件配置重载时调用
     */
    public void onMailChange(){
        GuiFactory.onHolderAction(MailHolder.class,(pMailHolder)->{
            pMailHolder.onMailChange();
            return pMailHolder;
        });
    }

    public static <T extends AInvHolder> void onHolderAction(Class<T> pHolderType,Task<T> pTask){
        for(Player sPlayer : BukkitUtil.getOnlinePlayers()){
            InventoryView tView=sPlayer.getOpenInventory();
            if(tView.getTopInventory()!=null&&pHolderType.isInstance(tView.getTopInventory().getHolder())){
                pTask.perform((T)tView.getTopInventory().getHolder());
            }
        }
    }

    public ItemStack getGuiTexture(int pSlot,GuiType pType){
        return pType.mSlotTexture.get(pSlot);
    }

}
