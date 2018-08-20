package org.black_ixx.bossshop.managers;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.bossshop.goods.price.PriceItem.MatchKind;
import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.TConfigManager;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.commentedyaml.CommentedValue;
import cc.commons.util.CollUtil;
import cc.commons.util.StringUtil;

public class ConfigManager extends TConfigManager<BossShop>{

    private boolean mEnableSigns;
    private boolean mHideNoPermitItem;
    private boolean mAllowUnsafeEnch;
    private String mMainshop;
    private int mCoolTime=5;
    private int mHideItemFlag=63;
    private int mShopOpenDelay=0;
    private boolean mAllowChangeRightClickBuyCount=false;
    private boolean mShowRightClickBuyCount=true;
    private int mRightClickBuyCount=1;
    private boolean mAllowShiftLeftClickBuy=false;
    private Sound mNotifySound=null;
    private HashSet<MatchKind> mPriceItemDefMatchKinds=new HashSet<>();

    private BossShop plugin;

    public ConfigManager(BossShop pplugin){
        super(pplugin,"1.9");
    }

    @Override
    public boolean reloadConfig(CommandSender pSender){
        if(!super.reloadConfig(pSender)){
            Log.severe(pSender,C("MsgErrorHappendWhenReloadConfig"));
            return false;
        }
        this.checkUpdate();
        this.addDefaults();

        this.mEnableSigns=this.mConfig.getBoolean("signs.enabled")||this.mConfig.getBoolean("EnableSigns");
        this.mMainshop=this.mConfig.getString("MainShop","Menu");
        this.mHideNoPermitItem=this.mConfig.getBoolean("HideItemsPlayersDoNotHavePermissionsFor",false);
        this.mAllowUnsafeEnch=this.mConfig.getBoolean("AllowUnsafeEnchantments",false);
        this.mCoolTime=this.mConfig.getInt("CoolTime",5);
        this.mHideItemFlag=this.mConfig.getInt("HideItemFlag",63);
        this.mShopOpenDelay=this.mConfig.getInt("OpenShopDelay",0);
        this.mShowRightClickBuyCount=this.mConfig.getBoolean("ShopDefaultSetting.ShowRightClickBuyCount",this.mShowRightClickBuyCount);
        this.mAllowChangeRightClickBuyCount=this.mConfig.getBoolean("ShopDefaultSetting.AllowChangeRightClickBuyCount",this.mAllowChangeRightClickBuyCount);
        this.mAllowShiftLeftClickBuy=this.mConfig.getBoolean("ShopDefaultSetting.AllowShiftLeftClickBuy",this.mAllowShiftLeftClickBuy);
        this.mRightClickBuyCount=this.mConfig.getInt("ShopDefaultSetting.RightClickBuyCount",this.mRightClickBuyCount);
        if(this.mRightClickBuyCount<=0){
            this.mRightClickBuyCount=1;
        }
        this.mShopOpenDelay/=50;

        this.mNotifySound=null;
        String[] notifySounds=this.mConfig.getString("NotifySound").split("[|]");
        if(notifySounds.length==0){
            this.mNotifySound=null;
        }else{
            for(String sSound : notifySounds){
                try{
                    this.mNotifySound=Sound.valueOf(sSound);
                    break;
                }catch(Throwable exp){
                    // ingnore
                }
            }

            if(this.mNotifySound==null){
                Log.severe(pSender,"通知声音设置错误,声音"+CollUtil.asList(notifySounds)+"均不存在");
            }

        }
        this.mPriceItemDefMatchKinds.clear();
        this.mPriceItemDefMatchKinds.addAll(MatchKind.getKinds(this.mConfig.getString("PriceItemDefMatchKinds","")));
        if(this.mPriceItemDefMatchKinds.isEmpty()){
            this.mPriceItemDefMatchKinds.add(MatchKind.All);
        }

        this.reloadModles(pSender);

        boolean result=this.saveConfig(null);
        Log.info(pSender,C("MsgConfigReloaded"));
        Log.info(pSender,C("MsgSetMainMenu").replace("%name%",this.mMainshop));
        return result;
    }

    protected boolean checkUpdate(){
        String tVersion=this.mConfig.getString(SEC_CFG_VERSION,"1.0");
        if(tVersion.compareToIgnoreCase(this.mVersion)>=0)
            return false;
        if(tVersion.compareToIgnoreCase("1.0")==0){// 1.0-->1.1
            tVersion="1.1";
            Object obj=this.mConfig.get("SaleSystem.OutDateItemNumbLimit");
            if(obj!=null)
                this.mConfig.addDefault("MailSystem.MaxSize",obj);
            this.mConfig.remove("SaleSystem.OutDateItemNumbLimit");
            this.mConfig.remove("SaleSystem.AutoClearNBT");
        }
        if(tVersion.compareToIgnoreCase("1.1")==0){// 1.1-->1.2
            tVersion="1.2";
            if(this.mConfig.contains("CoolTime")){
                int coolTime=this.mConfig.getInt("CoolTime",1);
                this.mConfig.set("CoolTime",coolTime*1000);
            }
        }
        if(tVersion.compareToIgnoreCase("1.2")==0){// 1.2-->1.3
            tVersion="1.3";
            if(this.mConfig.contains("BindMenuItem")){
                int item=this.mConfig.getInt("BindMenuItem");
                this.mConfig.remove("BindMenuItem");
                this.mConfig.addDefault("OpenMainShop.BindItem",item);
            }
        }
        if(tVersion.compareToIgnoreCase("1.3")==0){// 1.3-->1.4
            tVersion="1.4";
            this.mConfig.remove("AutoClearNBT");
            this.mConfig.remove("DisableUpdateNotifications");
        }
        if(tVersion.compareToIgnoreCase("1.4")<=0){// 1.4-->1.6
            tVersion="1.6";
            if(this.mConfig.contains("ItemNameLang")){
                this.mConfig.setComments("ItemNameLang","物品名字翻译语言,用于寄售物品和邮件 时候使用","如果翻译不存在,会在线下载");
            }
            if(this.mConfig.contains("SaleSystem.SaleItemName")){
                this.mConfig.setComments("SaleSystem.SaleItemName","设置寄售物品的显示格式","如果一行中有空格的话需要用单引号把整句括起来","可用的变量,SaleItemLore中也可以使用这些变量","%bs_sale_owner_name%        寄售者的名字","%bs_sale_item_id%           寄售物品的编号","%bs_sale_item_sigle_count%  寄售物品单份数量","%bs_sale_item_part_count%   寄售物品份数","%bs_sale_item_type%         寄售物品类型,[金币,点券,物品],如果要修改这几个请转到语言列表的Word节点","%bs_sale_item_name%         寄售物品名字,[金币,点券,物品英文名],前两项同上,最后一项为物品翻译名字","%bs_sale_price%             寄售物品价格,这个只显示数字","%bs_sale_price_type%        寄售物品类型,[金币,点券],要修改同上上");
            }
        }
        if(tVersion.compareToIgnoreCase("1.6")<=0){// 1.6-->1.7
            tVersion="1.7";
            if(this.mConfig.contains("PointsPlugin")){
                this.mConfig.setComments("PointsPlugin","可选[PlayerPoints,PointsAPI,EnjinMinecraftPlugin,CommandPoints,DDMLibPro2]","如果是[auto-detect],将会自动选择");
            }
            if(this.mConfig.contains("SaleSystem.SaleItemBlackLoreList")){
                this.mConfig.setComments("SaleSystem.SaleItemBlackLoreList","禁止寄售Lore或者自定义的物品名字中包含以下字符串的物品","支持正则表达式,特殊字符请注意转义");
            }
        }
        if(tVersion.compareToIgnoreCase("1.7")<=0){// 1.7-->1.8
            tVersion="1.8";
            this.mConfig.remove("LogPrefix");
            this.mConfig.remove("AutoClear.BuyRecordExpiredDays");
            CommentedValue tOldValue=this.mConfig.removeCommentedValue("EnableTransactionLog");
            if(tOldValue!=null){//TransactionLog.CFG_MAIN_SEC
                this.mConfig.set(TransactionLog.CFG_MAIN_SEC+".Enable",tOldValue);
            }
        }
        if(tVersion.compareToIgnoreCase("1.8")<=0){// 1.8-->1.9
            tVersion="1.9";
            this.mConfig.clearComments("NumbKey");
            CommentedSection tSection=this.mConfig.getOrCreateSection("NumbKey");
            for(String sKey : tSection.getKeys(false)){
                String tValue=tSection.getString(sKey);
                if(StringUtil.isNotEmpty(tValue)&&
                        (tValue.compareToIgnoreCase("ADD_RIGHT_CLICK_BUY_COUNT")==0||tValue.compareToIgnoreCase("REDUCE_RIGHT_CLICK_BUY_COUNT")==0)){
                    tSection.remove(sKey);
                }
            }

            this.mConfig.remove("ShopDefaultSetting.AllowChangeRightClickBuyCount");
        }
        this.mConfig.set(SEC_CFG_VERSION,this.mVersion);
        return true;
    }

    public void addDefaults(){
        super.addDefaults();
        this.mConfig.addDefault("EnableSigns",true,"启用贴牌创建BossShop商店入口");
        this.mConfig.addDefault("MainShop","Menu","BossShop主商店");
        this.mConfig.addDefault("ItemNameLang","zh_CN","物品名字翻译语言,用于寄售物品和邮件 时候使用","如果翻译不存在,会在线下载");
        this.mConfig.addDefault("OpenMainShop.BindItem","CLOCK","打开BossShop主商店的物品");
        this.mConfig.addDefault("OpenMainShop.SneakNeed",true,"使用物品打开商店时是否需要潜行");
        this.mConfig.addDefaultComments("OpenMainShop","物品打开主商店的配置","玩家需要有BossShop.open.menu权限才能打开");
        this.mConfig.addDefault("OpenShopDelay",0,"命令打开商店的延迟","用来兼容例如时钟命令等插件");
        this.mConfig.addDefault("HideItemsPlayersDoNotHavePermissionsFor",false,"隐藏玩家没有权限购买的物品");
        this.mConfig.addDefault("AllowUnsafeEnchantments",false,"允许不安全的附魔");
        this.mConfig.addDefault("PriceItemDefMatchKinds","all","默认的物品类型价格需要匹配的内容项目,多个内容使用逗号隔开","可用的内容匹配项有: "+MatchKind.values(),"可在物品价格配置下加上matchkinds节点,为该物品价格配置独立的匹配项");
        this.mConfig.addDefault("NotifySound","LEVEL_UP|ENTITY_PLAYER_LEVELUP","邮件和寄售物品卖出后使用该声音提醒玩家","如果未设置则不通知","多个声音使用竖线分隔,此时会选取第一个存在的声音");
        this.mConfig.addDefault("MultiplierGroups.Enabled",false);
        this.mConfig.addDefault("MultiplierGroups.List",new String[]{"Permission.Node:<type>:<multiplier>","BossShop.PriceMultiplier.Points1:points:0.75","BossShop.PriceMultiplier.Points1:points:0.5","BossShop.PriceMultiplier.Money1:money:0.75","BossShop.PriceMultiplier.Money2:money:0.5","BossShop.PriceMultiplier.MoneyNegative:money:2.0","BossShop.PriceMultiplier.Exp:exp:0.8",});
        this.mConfig.addDefault("SaleSystem.SortSaleShop",false,"是否自动排序寄售商店,排序只针对单独的商店(不建议开启)");
        this.mConfig.addDefault("SaleSystem.ShopBaseName","SYSTEM_SALE","寄售系统主配置");
        this.mConfig.addDefault("SaleSystem.ShopSize",54,"寄售商店大小","必须为9的整数,不超过81,而且商店最后两格需要留空用来制作导航按钮","一旦设置过大小并创建了寄售商店后,就不要改这个值了,除非同时修改商店文件,不然导航功能会失效");
        this.mConfig.addDefault("SaleSystem.ShopMaxNumb",2,"寄售商店最大数量");
        this.mConfig.addDefault("SaleSystem.MoneyPoundage",5,"金币收入手续费,百分比0-99");
        this.mConfig.addDefault("SaleSystem.PointsPoundage",5,"点券收入手续费,百分比0-99");
        this.mConfig.addDefault("SaleSystem.MoneyItem","EMERALD","金币寄售默认显示的物品,只支持数值,不支持12:3的类型");
        this.mConfig.addDefault("SaleSystem.PointsItem","DIAMOND","点券寄售默认显示的物品,只支持数值,不支持12:3的类型");
        this.mConfig.addDefault("SaleSystem.SaleTime",86400,"默认寄售时长(秒),这里是一天");
        this.mConfig.addDefault("SaleSystem.UnsaleCost",1000,"下架需要花费的金币数量,默认没有下架权限","权限BossShop.unsale.user: 下架自己物品的权限","权限BossShop.unsale.admin: 下架所有人物品的权限,并且不要支付下架花费");
        this.mConfig.addDefault("SaleSystem.UnsaleCount",3,"每天的下架数量限制,拥有BossShop.unsale.admin权限的人可以无视该限制","下架记录保存在buyRecord.yml文件中");
        this.mConfig.addDefault("SaleSystem.PerOneSaleLimit",3,"个人允许最多同时寄售物品数量的上限,如果玩家有BossShop.sale.unlimited权限可以无限寄售");
        this.mConfig.addDefault("SaleSystem.SaleItemName","&2寄售: %bs_sale_item_sigle_count% %bs_sale_item_name%","设置寄售物品的显示格式","如果一行中有空格的话需要用单引号把整句括起来","可用的变量,SaleItemLore中也可以使用这些变量","%bs_sale_owner_name%        寄售者的名字","%bs_sale_item_id%           寄售物品的编号","%bs_sale_item_sigle_count%  寄售物品单份数量","%bs_sale_item_part_count%   寄售物品份数","%bs_sale_item_type%         寄售物品类型,[金币,点券,物品],如果要修改这几个请转到语言列表的Word节点","%bs_sale_item_name%         寄售物品名字,[金币,点券,物品英文名],前两项同上,,最后一项为物品翻译名字","%bs_sale_price%             寄售物品价格,这个只显示数字","%bs_sale_price_type%        寄售物品类型,[金币,点券],要修改同上上");
        this.mConfig.addDefault("SaleSystem.SaleItemLore",new String[]{"&3寄售者: %bs_sale_owner_name%","&2寄售编号: %bs_sale_item_id%","&c价格: %bs_sale_price% %bs_sale_price_type%"},"设置寄售物品的lore格式,时间限制和数量限制为默认lore,如果需要修改请转到语言文件","lore最好加上颜色,不然默认是斜体紫色,丑!","lore默认显示在物品属性下面,过期信息,数量限制等信息的上面");
        this.mConfig.addDefault("SaleSystem.SaleItemBlackList",Arrays.asList(new String[]{"CLOCK"}),"禁止寄售的物品","可以为 `WATCH@0`  或者  `347@0`","如果没有填写@后面的部分,那么该id物品下所有子id都不能寄售");
        this.mConfig.addDefault("SaleSystem.SaleItemBlackLoreList",Arrays.asList(new String[]{"灵魂","绑定"}),"禁止寄售Lore或者自定义的物品名字中包含以下字符串的物品","支持正则表达式,特殊字符请注意转义");
        if(!this.mConfig.contains("SaleSystem.SaleLimitGroup")){
            CommentedSection tSection=this.mConfig.createSection("SaleSystem.SaleLimitGroup");
            this.mConfig.setComments("SaleSystem.SaleLimitGroup","不同权限,玩家的物品寄售上限","SaleLimitGroup:","  default: 3","例如以上的,如果玩家拥有BossShop.sale.group.default权限","那么,拥有该权限的玩家的寄售上限至少是3","但是如果PerOneSaleLimit节点设置的数量大于3,那么玩家的寄售上限就是PerOneSaleLimit节点设置的值");
            tSection.set("default",3);
            tSection.set("sale_vip1",5);
        }
        this.mConfig.addDefault("MailSystem.MaxSize",10,"邮箱大小,超过此大小的物品将不会保存");
        this.mConfig.addDefault("MailSystem.SendCost",1000,"发送邮件的花费(金币)");
        this.mConfig.addDefault("LotterySystem.ReflashBaseTicks",5,"基础Ticks,多少Ticks快速刷新一次背包");
        this.mConfig.addDefault("LotterySystem.ReflashLowMultiple",4,"预览抽奖时,过了多少倍ReflashBaseTicks后刷新一次背包");
        this.mConfig.addDefault("LotterySystem.RefalshFastCount",20,"进行抽奖时,刷新背包多少次");
        this.mConfig.addDefault("LotterySystem.ShowPrecent",34,"抽奖以及未抽奖时,物品显示比例,最高50,交替显示(50的话就只有两个显示样子了)");
        this.mConfig.addDefault("AutoClear.NBTExpiredDays",7,"NBT过期天数,超过该天数未被引用而且是自动创建的NBT会被清理");
        this.mConfig.addDefault("HideItemFlag",63,"隐藏寄售物品的攻击效果等属性"," 1|0.HIDE_ENCHANTS           附魔"," 2|1.HIDE_ATTRIBUTES         前缀"," 4|2.HIDE_UNBREAKABLE        不可破坏"," 8|3.HIDE_DESTROYS,          受损度","16|4.HIDE_PLACED_ON,         放置","32|5.HIDE_POTION_EFFECTS     药水效果","如何隐藏物品的状态??如果想隐藏相应的属性,只要把前面的数字加起来就可以了,如果要全部隐藏,就是63(默认)","注意此项设置只在1.8.3及其以后的版本有效,低版本的只有设置0和非零来开启全部或关闭全部显示,不能选择关闭什么","更低版本的可能不支持关闭","上面的表也可能存在错误就是前面的数字对应的不是这个属性的隐藏");
        this.mConfig.addDefault("PointsPlugin","auto-detect","可选[PlayerPoints,PointsAPI,EnjinMinecraftPlugin,CommandPoints,DDMLibPro2]","如果是[auto-detect],将会自动选择");
        this.mConfig.addDefault("CoolTime",200,"玩家点击商品的间隔(毫秒),无论是否购买成功,都会重复点击会重新计时");
        this.mConfig.addDefaultComments("NumbKey","数字键盘功能键(1-9),当前可用的功能: ","LEFT_CLICK_BUY_GOODS 模拟左键购买物品","RIGHT_CLICK_BUY_GOODS 模拟右键购买物品","UNSALE_GOODS 下架物品");
        this.mConfig.addDefault("NumbKey.NumbKey@1","LEFT_CLICK_BUY_GOODS","此处将数字键盘和功能绑定NumbKey@数字 ,数字就是在按下该数字时,执行对应的功能 ");
        this.mConfig.addDefault("NumbKey.NumbKey@2","UNSALE_GOODS");
        this.mConfig.addDefaultComments("DownloadLink","资源下载链接","除非你知道如何修改,否则请勿修改");
        this.mConfig.addDefault("ShopDefaultSetting.RightClickBuyCount",1,"右击物品时,商品默认的购买数量","用于创建商店的时候提供默认值");
        this.mConfig.addDefault("ShopDefaultSetting.ShowRightClickBuyCount",true,"当右键购买数量大于1时,显示右键购买数量","物品或商店设置为false可以用来自定义右键购买数量Lore消息","用于创建商店的时候提供默认值");
        this.mConfig.addDefault("ShopDefaultSetting.AllowShiftLeftClickBuy",false,"当为true时,开启shift+左键购买功能并显示","用于创建商店的时候提供默认值");
        this.mConfig.addDefault("DownloadLink.Versions","http://bmclapi2.bangbang93.com/version/%version%/json","各版本Json下载链接","官方:https://s3.amazonaws.com/Minecraft.Download/versions/%version%/%version%.json","国内:http://bmclapi2.bangbang93.com/version/%version%/json");
        this.mConfig.addDefault("DownloadLink.AssetIndex","http://bmclapi2.bangbang93.com/","对应版本的asset文件索引","官方:https://launchermeta.mojang.com","国内:http://bmclapi2.bangbang93.com/");
        this.mConfig.addDefault("DownloadLink.Lang","http://bmclapi2.bangbang93.com/assets/","lang语言文件下载链接","官方:http://resources.download.minecraft.net/","国内:http://bmclapi2.bangbang93.com/assets/");
    }

    public boolean signsEnabled(){
        return mEnableSigns;
    }

    public String getMainShop(){
        return mMainshop;
    }

    public boolean hideNoPermitItem(){
        return mHideNoPermitItem;
    }

    public boolean allowUnsafeEnchantments(){
        return mAllowUnsafeEnch;
    }

    public int getCoolTime(){
        return this.mCoolTime;
    }

    public int getHideItemFlag(){
        return this.mHideItemFlag;
    }

    /**
     * 商店打开延迟,单位为tick
     */
    public int getShopOpenDelay(){
        return this.mShopOpenDelay<0?0:this.mShopOpenDelay;
    }

    /**
     * 获取通知声音
     * 
     * @return 声音或null
     */
    public Sound getNotifySound(){
        return this.mNotifySound;
    }

    public int getRightClickBuyCount(){
        return this.mRightClickBuyCount;
    }

    public boolean allowChangeRightClickBuyCount(){
        return this.mAllowChangeRightClickBuyCount;
    }

    public boolean showRightClickBuyCount(){
        return this.mShowRightClickBuyCount;
    }
    public boolean allowShiftLeftClickBuy() {
    	return this.mAllowShiftLeftClickBuy;
    }

    public Collection<MatchKind> getPriceItemDefMatchKinds(){
        return this.mPriceItemDefMatchKinds;
    }

}
