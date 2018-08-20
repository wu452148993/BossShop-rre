package cc.bukkitPlugin.bossshop.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums;
import org.black_ixx.bossshop.core.BSEnums.RewardType;
import org.black_ixx.bossshop.core.BSGoods;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import cc.bukkitPlugin.bossshop.goods.reward.IReward;
import cc.bukkitPlugin.bossshop.goods.reward.RewardManager;
import cc.bukkitPlugin.bossshop.gui.GuiFactory;
import cc.bukkitPlugin.bossshop.gui.holder.MailHolder;
import cc.bukkitPlugin.bossshop.nbt.NBTEditManager;
import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTSerializer;
import cc.bukkitPlugin.commons.nmsutil.nbt.exception.NBTSerializeException;
import cc.bukkitPlugin.commons.plugin.INeedReload;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.AFileManager;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.IConfigModel;
import cc.bukkitPlugin.commons.tellraw.ClickEvent;
import cc.bukkitPlugin.commons.tellraw.Color;
import cc.bukkitPlugin.commons.tellraw.Format;
import cc.bukkitPlugin.commons.tellraw.HoverEvent;
import cc.bukkitPlugin.commons.tellraw.Tellraw;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.commentedyaml.CommentedValue;
import cc.commons.commentedyaml.CommentedYamlConfig;
import cc.commons.util.ByteUtil;
import cc.commons.util.CollUtil;
import cc.commons.util.StringUtil;

public class MailManager extends AFileManager<BossShop> implements IConfigModel,INeedReload{

    public static final String SEC_ItemType="Type";
    public static final String SEC_ItemContent="Content";
    public static final String SEC_Source="Source";
    private int mMaxSize=10;
    private int mSendCost=1000;
    private final HashMap<String,ArrayList<MailItem>> mMailItems=new HashMap<>();
    private long mLastReloadTime=0;

    public MailManager(BossShop pPlugin){
        super(pPlugin,"mail_item.yml","1.2");

        this.mPlugin.getConfigManager().registerConfigModel(this);
        this.mPlugin.registerReloadModel(this);
    }

    public long getLastReloadTime(){
        return this.mLastReloadTime;
    }

    @Override
    public void setConfig(CommandSender pSender,CommentedYamlConfig pConfig){
        CommentedYamlConfig tConfig=this.mPlugin.getConfigManager().getConfig();
        CommentedSection tMainSec=tConfig.getSection("MailSystem");
        if(tMainSec==null)
            tMainSec=tConfig.createSection("MailSystem");
        this.mMaxSize=tMainSec.getInt("MaxSize",this.mMaxSize);
        this.mSendCost=tMainSec.getInt("SendCost",mSendCost);
    }

    @Override
    public boolean reloadConfig(CommandSender pSender){
        if(!super.reloadConfig(pSender))
            return false;
        this.addDefaults();
        this.checkUpdate();
        this.mLastReloadTime=System.currentTimeMillis();
        this.loadMailItems();
        GuiFactory.getInstance().onMailChange();

        this.saveConfig(null);
        return true;
    }

    @Override
    protected boolean checkUpdate(){
        if(!super.checkUpdate())
            return false;

        String tVersion=this.getVersion();
        if(tVersion.compareTo("1.2")<0){// 1.0 -> 1.1
            tVersion="1.2";
            for(String sPlayerKey : this.mConfig.getKeys(false)){
                CommentedSection tPlayerSec=this.mConfig.getSection(sPlayerKey);
                if(tPlayerSec==null){
                    if(!sPlayerKey.equals(SEC_CFG_VERSION))
                        this.mConfig.set(sPlayerKey,null);
                    continue;
                }
                UUID tUuid=null;
                try{
                    tUuid=UUID.fromString(sPlayerKey);
                }catch(Exception exp){
                    continue;
                }
                OfflinePlayer tPlayer=Bukkit.getOfflinePlayer(tUuid);
                if(tPlayer!=null&&StringUtil.isNotEmpty(tPlayer.getName())){
                    CommentedSection tNameSec=this.mConfig.getOrCreateSection(tPlayer.getName().toLowerCase());
                    for(Map.Entry<String,CommentedValue> sEntry : tNameSec.values().entrySet()){
                        tNameSec.set((String)sEntry.getKey(),sEntry.getValue());
                    }
                }
            }
        }
        this.mConfig.set(SEC_CFG_VERSION,this.mVersion);
        return true;
    }

    /**
     * 从配置文件中载入邮件物品
     */
    protected void loadMailItems(){
        this.mMailItems.clear();
        for(String sPlayerKey : this.mConfig.getKeys(false)){
            CommentedSection tPlayerSec=this.mConfig.getSection(sPlayerKey);
            if(tPlayerSec==null){
                if(!sPlayerKey.equals(SEC_CFG_VERSION))
                    this.mConfig.set(sPlayerKey,null);
                continue;
            }
            ArrayList<MailItem> allMail=new ArrayList<>();
            boolean hasError=false;
            for(String sItemKey : tPlayerSec.getKeys(false)){
                MailItem tItem=this.loadMailItems(tPlayerSec.getSection(sItemKey));
                if(tItem==null){
                    hasError=true;
                    continue;
                }
                allMail.add(tItem);
                if(allMail.size()>=this.mMaxSize)
                    break;
            }
            if(allMail.isEmpty()&&!hasError){
                this.mConfig.set(sPlayerKey,null);
                this.saveConfig(null);
            }else{
                this.mMailItems.put(sPlayerKey,allMail);
            }
        }
    }

    /**
     * 使用指定的配置节点载入邮件
     * 
     * @param pMailItemSection
     *            配置节点
     * @return 右键或null
     */
    protected MailItem loadMailItems(CommentedSection pMailItemSection){
        try{
            if(pMailItemSection==null)
                return null;
            RewardType tItemType=BSEnums.getRewardType(pMailItemSection.getString(SEC_ItemType));
            if(tItemType==null){
                Log.severe(C("MsgUnsupportMailItemType")+"["+pMailItemSection.getString(SEC_ItemType)+"]");
                return null;
            }
            IReward tReward=this.mPlugin.getManager(RewardManager.class).createReward(tItemType,pMailItemSection.get(SEC_ItemContent));
            if(tReward==null){
                Log.severe(C("MsgCannotCreatMailItem")+"["+pMailItemSection.getName()+"]");
                return null;
            }
            return new MailItem(pMailItemSection.getName(),tReward,pMailItemSection.getString(SEC_Source));
        }catch(Exception exp){
            Log.severe("载入邮件时发生了异常",exp);
            return null;
        }
    }

    /**
     * 添加一封来自他人的邮件,调用前请先检查邮件是否已经满了
     * <p>
     * 如果玩家在线,会提醒玩家有新邮件
     * </p>
     * 
     * @param pFrom
     *            来自谁
     * @param pTo
     *            给谁
     * @param pItem
     *            物品
     */
    public MailItem addMail(Player pFrom,OfflinePlayer pTo,ItemStack pItem){
        ArrayList<ItemStack> tItems=new ArrayList<>();
        tItems.add(pItem);
        CommentedSection tMailItemSection=this.addMailToFile(pTo,RewardType.Item,tItems,C("MsgFromPlayer")+pFrom.getName());
        if(tMailItemSection==null)
            return null;
        MailItem tItem=this.loadMailItems(tMailItemSection);
        if(tItem==null)
            return null;
        this.addMail(pTo.getName(),tItem);
        return tItem;
    }

    /**
     * 添加过期物品到邮件列表,如果玩家在线会发送提醒
     * 
     * @return 失败返回空
     */
    public MailItem addMail(BSGoods pBuy,String pSource){
        if(!pBuy.hasOwner()||pBuy.getLimit()<=0)
            return null;
        OfflinePlayer tOwner=Bukkit.getOfflinePlayer(pBuy.getOwner());
        if(tOwner==null||StringUtil.isEmpty(tOwner.getName()))
            return null;

        if(this.isMailFull(tOwner.getName())){
            this.noticeMailFull(tOwner.getName());
            return null;
        }
        
        IReward tReward=pBuy.getFirstReward();
        CommentedSection tMailItemSection=this.addMailToFile(Bukkit.getOfflinePlayer(pBuy.getOwner()),tReward.getRewardType(),tReward.getReward(pBuy.getLimit()),pSource);
        if(tMailItemSection==null)
            return null;
        MailItem tItem=this.loadMailItems(tMailItemSection);
        if(tItem==null)
            return null;
        this.addMail(tOwner.getName(),tItem);
        return tItem;
    }

    /**
     * 添加邮件到邮件列表,调用前必须检查isMailFull<br />
     * 在此函数中统一发送通知消息
     */
    private void addMail(String pPlayerName,MailItem pItem){
        ArrayList<MailItem> tItems=this.mMailItems.get(pPlayerName.toLowerCase());
        if(tItems==null){
            tItems=new ArrayList<>();
            this.mMailItems.put(pPlayerName.toLowerCase(),tItems);
        }
        if(this.isMailFull(pPlayerName)){
            this.noticeMailFull(pPlayerName);
            return;
        }
        tItems.add(pItem);
        Player tPlayer=Bukkit.getPlayer(pPlayerName);
        if(tPlayer!=null&&tPlayer.isOnline()){
            Tellraw sendMsg=Tellraw.cHead(Log.getMsgPrefix()+" "+this.mPlugin.C("MsgYouReciveMail","%where%",pItem.getSource()));
            this.addCheckAndReciveAllButton(sendMsg);
            sendMsg.sendToPlayer(tPlayer);
            if(this.isMailFull(pPlayerName))
                this.noticeMailFull(pPlayerName);
            else BossShop.soundNotifyPlayer(tPlayer);

            InventoryView tView=tPlayer.getOpenInventory();
            if(tView.getTopInventory()!=null&&tView.getTopInventory().getHolder() instanceof MailHolder){
                ((MailHolder)tView.getTopInventory().getHolder()).onMailChange();
            }
        }
    }

    /**
     * 玩家邮箱是否达到了数量上限
     */
    public boolean isMailFull(String pPlayerName){
        if(this.mMaxSize<=0)
            return true;
        CommentedSection tUserSec=this.mConfig.getSection(pPlayerName.toLowerCase());
        if(tUserSec==null)
            return false;
        return tUserSec.getKeys(false).size()>=this.mMaxSize;
    }

    /**
     * 通知玩家邮箱已经满了
     * 
     * @param pUUID
     */
    public void noticeMailFull(String pPlayerName){
        Player tPlayer=Bukkit.getPlayerExact(pPlayerName);
        if(tPlayer==null||!tPlayer.isOnline())
            return;
        Log.send(tPlayer,C("MsgYouMailIsFull"));
        BossShop.soundNotifyPlayer(tPlayer);
    }

    public void noticeExistMail(Player pPlayer){
        if(pPlayer==null||!pPlayer.isOnline())
            return;
        int mailCount=this.getMailCount(pPlayer.getName());
        if(mailCount>0){
            Tellraw sendMsg=Tellraw.cHead(Log.getMsgPrefix()+" "+this.mPlugin.C("MsgYouHaveMail","%numb%",mailCount+""));
            this.addCheckAndReciveAllButton(sendMsg);
            sendMsg.sendToPlayer(pPlayer);
            BossShop.soundNotifyPlayer(pPlayer);
        }
    }

    private Tellraw addReciveButton(Tellraw pMsg,String pMailName){
        Tellraw clickE=new Tellraw(C("WordRevive"),Color.blue);
        clickE.getChatStyle().setFormat(Format.underline,Format.bold);
        clickE.getChatStyle().setClickEvent(ClickEvent.Action.run_command,"/bossshop mail recive "+pMailName);
        clickE.getChatStyle().setHoverEvent(HoverEvent.Action.show_text,C("WordRevive"));
        pMsg.addExtra(clickE);
        return pMsg;
    }

    private Tellraw addCheckAndReciveAllButton(Tellraw pMsg){
        Tellraw clickE=new Tellraw(C("WordView"),Color.blue);
        clickE.getChatStyle().setFormat(Format.underline,Format.bold);
        clickE.getChatStyle().setClickEvent(ClickEvent.Action.run_command,"/bossshop mail gui");
        clickE.getChatStyle().setHoverEvent(HoverEvent.Action.show_text,C("WordView"));
        pMsg.addText(", ");
        pMsg.addExtra(clickE);
        clickE=new Tellraw(C("WordRevive")+C("WordAll"),Color.blue);
        clickE.getChatStyle().setFormat(Format.underline,Format.bold);
        clickE.getChatStyle().setClickEvent(ClickEvent.Action.run_command,"/bossshop mail recive");
        clickE.getChatStyle().setHoverEvent(HoverEvent.Action.show_text,C("WordRevive")+C("WordAll"));
        pMsg.addText(", ");
        pMsg.addExtra(clickE);
        return pMsg;
    }

    /**
     * 添加邮件到文件,调用前必须检查isMailFull
     * 
     * @param pUUID
     *            玩家UUID
     * @param pItem
     *            邮件物品
     * @return 添加邮件后的配置节点
     */
    protected CommentedSection addMailToFile(OfflinePlayer pOwner,RewardType pRewardType,Object pItem,String pSource){
        if(pOwner==null){
            return null;
        }
        CommentedSection tUserSec=this.mConfig.getOrCreateSection(pOwner.getName().toLowerCase());
        long time=System.currentTimeMillis()/1000;
        String tItemLabel="item_"+time;
        while(tUserSec.contains(tItemLabel))
            tItemLabel="item_"+(++time);
        CommentedSection tItemSec=tUserSec.createSection(tItemLabel);
        tItemSec.set(SEC_ItemType,pRewardType.name());
        switch(pRewardType){
            case Money:
            case Points:
                tItemSec.set(SEC_ItemContent,pItem);
                break;
            case Item:
                List<ItemStack> tItems=(List<ItemStack>)pItem;
                if(tItems.isEmpty())
                    return null;
                ItemStack item=tItems.get(0);
                ArrayList<String> tItemInfo=new ArrayList<>();
                tItemInfo.add("type:"+item.getType().name());
                tItemInfo.add("durability:"+item.getDurability());
                tItemInfo.add("amount:"+item.getAmount());

                String tNBTData=null;
                try{
                    tNBTData=ByteUtil.byteToBase64(NBTSerializer.serializeNBTToByte(item));
                }catch(NBTSerializeException exp){
                    Log.severe("序列化邮件物品的NBT时发生了错误",exp);
                }
                if(StringUtil.isNotEmpty(tNBTData)){
                    tItemInfo.add("rawnbt:"+tNBTData);
                }
                tItemSec.set(SEC_ItemContent,tItemInfo);
                break;
            default:
                Log.severe(ChatColor.RED+"["+pRewardType.name()+"]");
                Log.severe(C("MsgUnsupportMailItemType"));
                tUserSec.set(tItemLabel,null);
                return null;
        }
        tItemSec.set(SEC_Source,pSource);
        this.saveConfig(null);
        return tItemSec;
    }

    /**
     * 检查玩家邮件
     * 
     * @param pPlayer
     *            玩家
     */
    public void checkMail(Player pPlayer){
        ArrayList<MailItem> tMainItems=this.getPlayerMails(pPlayer.getName(),null);
        if(tMainItems.size()>0){
            Log.send(pPlayer,C("MsgMailMaxSaveNumb").replace("%numb%",tMainItems.size()+"/"+this.mMaxSize));
            Tellraw container=Tellraw.cHead(Log.getMsgPrefix()+" ");
            for(MailItem sMailItem : tMainItems){
                this.addReciveButton(container.clone().addText(sMailItem.getPreview()+"  "),sMailItem.getName()).sendToPlayer(pPlayer);
            }
        }else Log.send(pPlayer,C("MsgYouHaveNoMail"));
    }

    /**
     * 获取该玩家的邮件数量
     * 
     * @param pPlayerName
     *            玩家名称
     * @return 邮件数量
     */
    public int getMailCount(String pPlayerName){
        return this.getPlayerMails(pPlayerName,null).size();
    }

    /**
     * 检查玩家是否有该邮件
     * 
     * @param pPlayer
     *            检查的玩家
     * @param pMailName
     *            邮件机读名
     * @return 是否存在
     */
    public boolean mailExist(Player pPlayer,String pMailName){
        if(pPlayer==null)
            return false;
        return !this.getPlayerMails(pPlayer.getName(),pMailName).isEmpty();
    }

    /**
     * 获取邮件列表
     * 
     * @param pPlayer
     *            返还的玩家
     * @param pMailName
     *            邮件机读名,如果为null,代表获取所有的
     * @return 非null
     */
    public ArrayList<MailItem> getPlayerMails(String pPlayerName,String pMailName){
        ArrayList<MailItem> tMails=new ArrayList<>();
        ArrayList<MailItem> tUserMails=this.mMailItems.get(pPlayerName.toLowerCase());
        if(!CollUtil.isEmpty(tUserMails)){
            for(MailItem sMail : tUserMails){
                if(pMailName==null||sMail.getName().equalsIgnoreCase(pMailName))
                    tMails.add(sMail);
            }
        }
        return tMails;
    }

    /**
     * 获取邮件名字列表
     * 
     * @param pPlayer
     *            返还的玩家
     * @param pMailName
     *            邮件机读名,如果为null,代表获取所有的
     * @return 非null
     */
    public ArrayList<String> getPlayerMailsName(String pPlayerName,String pMailName){
        ArrayList<String> tMailNames=new ArrayList<>();
        for(MailItem sMail : this.getPlayerMails(pPlayerName,pMailName)){
            if(pMailName==null||sMail.getName().equalsIgnoreCase(pMailName))
                tMailNames.add(sMail.getName());
        }
        return tMailNames;
    }

    /**
     * 返还邮件物品
     * 
     * @param pPlayer
     *            返还的玩家
     * @param pMailName
     *            邮件机读名,,如果为null,代表接受所有的
     */
    public void reciveMail(Player pPlayer,String pMailName){
        if(pPlayer==null)
            return;
        String tPlayerName=pPlayer.getName().toLowerCase();
        boolean tHaveItem=false;
        ArrayList<MailItem> tMails=this.getPlayerMails(tPlayerName,pMailName);
        if(tMails.isEmpty())
            return;
        ArrayList<MailItem> tPlayerMails=this.mMailItems.get(tPlayerName);
        CommentedSection tUserMailsSec=this.mConfig.getOrCreateSection(tPlayerName);
        for(MailItem sMail : tMails){
            tPlayerMails.remove(sMail);
            tUserMailsSec.remove(sMail.getName());

            sMail.getItem().giveReward(pPlayer,1);
            Log.send(pPlayer,ChatColor.GREEN+C("WordRevive")+sMail.getPreview());
            if(sMail.getItemType()==RewardType.Item)
                tHaveItem=true;
        }
        ArrayList<MailItem> tAllMail=this.mMailItems.get(tPlayerName);
        if((tAllMail==null||tAllMail.isEmpty())){
            this.mMailItems.remove(tPlayerName);
            if(tUserMailsSec.getChildDirect().isEmpty()){
                this.mConfig.set(tPlayerName,null);
            }
        }
        if(tHaveItem)
            this.mPlugin.getManager(NBTEditManager.class).clearNBT(null);
        this.saveConfig(null);
    }

    public int getMailSendCost(){
        return this.mSendCost;
    }

    @Override
    public void addDefaults(CommentedYamlConfig pConfig){

    }

}
