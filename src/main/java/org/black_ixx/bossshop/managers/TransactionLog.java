package org.black_ixx.bossshop.managers;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums;
import org.black_ixx.bossshop.core.BSEnums.PriceType;
import org.black_ixx.bossshop.core.BSEnums.RewardType;
import org.black_ixx.bossshop.core.BSGoods;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.bossshop.goods.price.IPrice;
import cc.bukkitPlugin.bossshop.goods.reward.IReward;
import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.INeedReload;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.ATimerSaveManager;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.IConfigModel;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.commentedyaml.CommentedYamlConfig;
import cc.commons.util.CollUtil;
import cc.commons.util.FileUtil;

public class TransactionLog extends ATimerSaveManager<BossShop> implements INeedReload,IConfigModel{

    public static final String LogDir="PlayerBuyRecord";
    public static final String CFG_MAIN_SEC="TransactionLog";
    public static final String mFormatTimeOfDay="yyyy-MM-dd";
    private final SimpleDateFormat formatTime=new SimpleDateFormat("HH:mm:ss");

    private boolean mEnableRecord;
    private int mLogSaveDays=30;
    private final HashSet<RewardType> mNoLogGoodsType=new HashSet<>();
    private final HashSet<PriceType> mNoLogPriceType=new HashSet<>();

    private int mLastEditDayOfWeek=-1;

    public TransactionLog(BossShop pPlugin){
        super(pPlugin,getFile(new Date()),"1.0",false);

        this.mTask=Bukkit.getScheduler().runTaskTimer(this.mPlugin,this,200,1200);
        this.mPlugin.registerReloadModel(this);
        this.mPlugin.getConfigManager().registerConfigModel(this);
    }

    @Override
    public void addDefaults(CommentedYamlConfig pConfig){
        CommentedSection tMainSec=pConfig.getOrCreateSection(TransactionLog.CFG_MAIN_SEC,"交易记录设置");
        tMainSec.addDefault("Enable",true,"是否启用交易记录");
        tMainSec.addDefault("LogSaveDays",30,"交易日志保留天数,小于等于0表示无限制");
        tMainSec.addDefault("LogIgnoreGoodsType",new String[]{RewardType.Nothing.name(),RewardType.Shop.name()},"不记录的商品类型");
        tMainSec.addDefault("LogIgnorePriceType",new String[]{PriceType.Nothing.name()},"不记录的价格类型");
    }

    @Override
    public void setConfig(CommandSender pSender,CommentedYamlConfig pConfig){
        CommentedSection tMainSec=pConfig.getOrCreateSection(TransactionLog.CFG_MAIN_SEC);
        Log.info(pSender,"开始载入购买日志配置");
        this.mEnableRecord=tMainSec.getBoolean("Enable",this.mEnableRecord);
        if(this.mEnableRecord){
            this.mLogSaveDays=tMainSec.getInt("LogSaveDays",this.mLogSaveDays);
            this.mNoLogGoodsType.clear();
            for(String sLine : tMainSec.getStringList("LogIgnoreGoodsType")){
                RewardType tType=BSEnums.getRewardType(sLine.trim());
                if(tType==null){
                    Log.info(pSender,sLine+" 不是已知的商品类型");
                }else{
                    this.mNoLogGoodsType.add(tType);
                }
            }
            this.mNoLogPriceType.clear();
            for(String sLine : tMainSec.getStringList("LogIgnorePriceType")){
                PriceType tType=BSEnums.getPriceType(sLine.trim());
                if(tType==null){
                    Log.info(pSender,sLine+" 不是已知的价格类型");
                }else{
                    this.mNoLogPriceType.add(tType);
                }
            }

            Log.info(pSender,"购买日志配置载入完毕");
        }else{
            Log.info(pSender,"未启用购买日志记录");
        }

        this.clearOutDateRecord(pSender);
    }

    @Override
    public boolean reloadConfig(CommandSender pSender){
        if(!super.reloadConfig(pSender))
            return false;

        this.checkUpdate();
        this.saveConfig(null);
        return true;
    }

    @Override
    public boolean checkUpdate(){
        File tOldLogFile=new File(this.mPlugin.getDataFolder(),"TransactionLog.yml");
        if(tOldLogFile.isFile()){
            CommentedYamlConfig tLoader=CommentedYamlConfig.loadFromFileS(tOldLogFile,false);
            String tversion=tLoader.getString(SEC_CFG_VERSION,"1.0");
            if(tversion.equalsIgnoreCase("1.0")){
                if(!tLoader.getChildDirect().isEmpty()){
                    tLoader.saveToFile(new File(tOldLogFile.getAbsolutePath()+".bak"));
                    tLoader.clear();
                }
            }else{
                CommentedSection tSec=tLoader.getOrCreateSection("PlayerBuyRecord");
                CommentedYamlConfig tSaver=new CommentedYamlConfig();
                SimpleDateFormat tDayFormat=new SimpleDateFormat(TransactionLog.mFormatTimeOfDay);
                for(String sDays : tSec.getKeys(false)){
                    CommentedSection tRecords=tSec.getSection(sDays);
                    if(tRecords==null||tRecords.getChildDirect().isEmpty())
                        continue;
                    Date tDay=null;
                    try{
                        tDay=tDayFormat.parse(sDays.trim());
                    }catch(ParseException exp){
                        continue;
                    }
                    tSaver.clear();
                    File tSaveFile=new File(this.mPlugin.getDataFolder(),TransactionLog.getFile(tDay));
                    if(tSaveFile.isFile()){
                        tSaver.loadFromFile(tSaveFile);
                    }
                    tSaver.getChildDirect().putAll(tRecords.getChildDirect());
                    tSaver.saveToFile(tSaveFile);
                }
                tOldLogFile.delete();
            }
        }
        return false;
    }

    /**
     * 添加购买记录到文件
     * @param pPlayer 购买的玩家
     * @param pReward 购买的物品
     * @param pCount 购买的数量
     */
    public void addTransaction(Player pPlayer,BSGoods pReward,int pCount){
        if(!this.mEnableRecord)
            return;
        
        boolean tNolog=true;
        for(IReward sReward : pReward.getRewards()){
            if(!this.mNoLogGoodsType.contains(sReward.getRewardType())){
                tNolog=false;
                break;
            }
        }
        if(tNolog) return;
        for(IPrice sPrice : pReward.getPrices()){
            if(!this.mNoLogPriceType.contains(sPrice.getPriceType())){
                tNolog=false;
                break;
            }
        }
        if(tNolog) return;
        this.updateEditTime();

        Date nowDate=new Date();
        long time=System.currentTimeMillis();
        String tRecordLabel="record_"+time;
        while(this.mConfig.contains(tRecordLabel))
            tRecordLabel="item_"+(++time);
        CommentedSection recordSection=this.mConfig.createSection(tRecordLabel);
        recordSection.set(this.mPlugin.C("YmalNodePlayerName"),pPlayer.getName());
        recordSection.set(this.mPlugin.C("YmalNodeBuyShop"),pReward.getShopName());
        recordSection.set(this.mPlugin.C("YmalNodeBuyItemName"),pReward.getName());
        recordSection.set(this.mPlugin.C("YmalNodeBuyNumb"),pCount);
        recordSection.set(this.mPlugin.C("YmalNodeBuyTime"),this.formatTime.format(nowDate));
        this.markChanged();
    }

    public static String getFile(Date pTime){
        return TransactionLog.LogDir+File.separator+new SimpleDateFormat(TransactionLog.mFormatTimeOfDay).format(pTime)+".yml";
    }

    protected void updateEditTime(){
        Date tNowTime=new Date();
        if(tNowTime.getDay()!=this.mLastEditDayOfWeek){
            this.mLastEditDayOfWeek=tNowTime.getDay();
            this.clearOutDateRecord(null);
            if(this.mChanged){
                this.saveConfig(null);
            }
            this.updateConfigFile(TransactionLog.getFile(tNowTime));
            this.reloadConfig(null);
        }
    }

    public void clearOutDateRecord(CommandSender pSender){
        if(this.mLogSaveDays<=0)
            return;

        File tLogDir=new File(this.mPlugin.getDataFolder(),"PlayerBuyRecord"+File.separator);
        File[] tChildFiles=tLogDir.listFiles();
        if(CollUtil.isNotEmpty(tChildFiles)){
            int tClearCount=0;
            Calendar tCalendar=Calendar.getInstance();
            tCalendar.setTimeInMillis(System.currentTimeMillis());
            tCalendar.add(Calendar.DAY_OF_YEAR,-this.mLogSaveDays);
            tCalendar.set(tCalendar.get(Calendar.YEAR),tCalendar.get(Calendar.MONTH),tCalendar.get(Calendar.DAY_OF_MONTH));
            Date tOldestTime=tCalendar.getTime();
            SimpleDateFormat tFileFormat=new SimpleDateFormat(TransactionLog.mFormatTimeOfDay);
            for(File sFile : tChildFiles){
                if(sFile.isFile()&&sFile.getName().toLowerCase().endsWith(".yml")){
                    Date tDate=null;
                    try{
                        tDate=tFileFormat.parse(sFile.getName().substring(0,sFile.getName().length()-4));
                    }catch(ParseException exp){
                        continue;
                    }
                    if(tOldestTime.after(tDate)){
                        tClearCount++;
                            FileUtil.deleteFile(sFile);
                    }
                }
            }
            if(tClearCount>0){
                Log.info(pSender,C("MsgClearedTransactionLog").replace("%day%",tClearCount+""));
            }
        }
    }
}
