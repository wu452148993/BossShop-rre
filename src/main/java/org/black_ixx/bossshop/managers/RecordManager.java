package org.black_ixx.bossshop.managers;

import java.util.Calendar;
import java.util.UUID;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.plugin.INeedReload;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.ATimerSaveManager;
import cc.commons.commentedyaml.CommentedSection;

/**
 * 用于记录玩家购买限制数量物品的记录
 * @author 聪聪
 *
 */
public class RecordManager extends ATimerSaveManager<BossShop> implements INeedReload{

    public static final String SEC_UNSALE="_UnsaleRecord_";
    public static final String SEC_BUY="_BuyRecord_";
    public static final String SEC_UNSALE_COUNT="Count";
    public static final String SEC_UNSALE_TIME="Time";

    /**
     * 无初始化限制条件
     * @param mPlugin
     */
    public RecordManager(BossShop pplugin){
        super(pplugin,"buyRecord.yml","1.2");

        this.mConfig.options().enabelComment(false);
        this.mPlugin.registerReloadModel(this);
    }

    @Override
    public boolean reloadConfig(CommandSender pSender){
        if(!super.reloadConfig(pSender))
            return false;
        this.checkUpdate();
        this.addDefaults();
        this.saveConfig(null);
        return true;
    }

    protected boolean checkUpdate(){
        boolean update=super.checkUpdate();
        if(!update)
            return false;
        String tVersion=this.mConfig.getString(SEC_CFG_VERSION,"1.0");
        if(tVersion.equalsIgnoreCase("1.0")){
            tVersion="1.1";
            for(String shop_key : this.mConfig.getKeys(false)){
                CommentedSection shop_sec=this.mConfig.getSection(shop_key);
                if(shop_sec==null)
                    continue;
                for(String item_key : shop_sec.getKeys(false)){
                    CommentedSection item_sec=shop_sec.getSection(item_key);
                    if(item_sec==null)
                        continue;
                    for(String player_key : item_sec.getKeys(false)){
                        int numb=item_sec.getInt(player_key);
                        item_sec.set(player_key,null);
                        if(numb<=0)
                            continue;
                        Player tPlayer=Bukkit.getPlayer(player_key);
                        if(tPlayer==null)
                            continue;
                        item_sec.set(tPlayer.getUniqueId().toString(),numb);
                    }
                    if(item_sec.getKeys(false).size()==0)
                        shop_sec.set(item_key,null);
                }
                if(shop_sec.getKeys(false).size()==0)
                    this.mConfig.set(shop_key,null);
            }
        }
        if(tVersion.equalsIgnoreCase("1.1")){
            for(String shop_key : this.mConfig.getKeys(false)){
                if(shop_key.equalsIgnoreCase(SEC_BUY))
                    continue;
                CommentedSection shop_sec=this.mConfig.getSection(shop_key);
                this.mConfig.set(shop_key,null);
                this.mConfig.set(SEC_BUY+"."+shop_key,shop_sec);
            }
        }
        this.mConfig.set(SEC_CFG_VERSION,this.mVersion);
        return true;
    }

    /**
     * 增加一次玩家购买该物品的次数
     * @param shopname 商店名
     * @param buyname 物品名
     * @param player 用户名
     */
    public void addBuyRecord(String shopname,String buyname,UUID pUUID,int pCount){
        String path=(shopname+"."+buyname+"."+pUUID.toString()).toLowerCase();
        int buy=this.mConfig.getInt(path,0);
        this.mConfig.set(path,buy+pCount);
        this.markChanged();
    }

    /**
     * 获取玩家购买该物品的次数
     * @param shopname 商店名
     * @param buyname 物品名
     * @param player 用户名
     */
    public int getBuyRecord(String shopname,String buyname,UUID pUUID){
        String path=(shopname+"."+buyname+"."+pUUID.toString()).toLowerCase();
        return this.mConfig.getInt(path,0);
    }

    public int addUnsaleRecord(UUID pUUID){
        String path=SEC_UNSALE+"."+pUUID.toString();
        CommentedSection tSec=this.mConfig.getSection(path);
        int count=0;
        if(tSec!=null){
            long time=this.mConfig.getLong(path+"."+SEC_UNSALE_TIME,System.currentTimeMillis());
            if(dxDays(time)==0){
                count=this.mConfig.getInt(path+"."+SEC_UNSALE_COUNT,0);
                count++;

            }
        }else count=1;
        this.mConfig.set(path+"."+SEC_UNSALE_COUNT,count);
        this.mConfig.set(path+"."+SEC_UNSALE_TIME,System.currentTimeMillis());
        this.markChanged();
        return count;
    }

    public int getUnsaleRecord(UUID pUUID){
        String path=SEC_UNSALE+"."+pUUID.toString();
        CommentedSection tSec=this.mConfig.getSection(path);
        if(tSec==null)
            return 0;
        long time=this.mConfig.getLong(path+"."+SEC_UNSALE_TIME,System.currentTimeMillis());
        if(dxDays(time)!=0)
            return 0;
        return this.mConfig.getInt(path+"."+SEC_UNSALE_COUNT,0);
    }

    /**
     * 计算所给时间和现在的相差的天数,结果肯定>=0
     * @param date 给定的时间
     * @return 相差的天数
     */
    public static int dxDays(long date){
        return dxDays(System.currentTimeMillis(),date);
    }

    /**
     * 计算所给连个时间相差的天数,结果肯定>=0
     * @param data 给定的时间
     * @return 相差的天数
     */
    public static int dxDays(long dateb,long datef){
        int ptime=Calendar.getInstance().getTimeZone().getRawOffset();
        int ndays=(int)((dateb+ptime)/86400000);
        int tdays=(int)((datef+ptime)/86400000);
        return Math.abs(ndays-tdays);
    }

}
