package org.black_ixx.bossshop;

import org.black_ixx.bossshop.api.BossShopAPI;
import org.black_ixx.bossshop.core.BSShopHolder;
import org.black_ixx.bossshop.core.BSShopManager;
import org.black_ixx.bossshop.events.BSReloadedEvent;
import org.black_ixx.bossshop.listeners.InventoryListener;
import org.black_ixx.bossshop.listeners.PlayerListener;
import org.black_ixx.bossshop.listeners.SignListener;
import org.black_ixx.bossshop.managers.BuyItemHandler;
import org.black_ixx.bossshop.managers.ClassManager;
import org.black_ixx.bossshop.managers.ConfigManager;
import org.black_ixx.bossshop.managers.ItemNameManager;
import org.black_ixx.bossshop.managers.ItemStackChecker;
import org.black_ixx.bossshop.managers.ItemStackCreator;
import org.black_ixx.bossshop.managers.LangManager;
import org.black_ixx.bossshop.managers.MultiplierHandler;
import org.black_ixx.bossshop.managers.RecordManager;
import org.black_ixx.bossshop.managers.ShopCustomizer;
import org.black_ixx.bossshop.managers.TimeHandler;
import org.black_ixx.bossshop.managers.TransactionLog;
import org.black_ixx.bossshop.managers.WorthHandler;
import org.black_ixx.bossshop.points.PointsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import cc.bukkitPlugin.bossshop.command.CommandExc;
import cc.bukkitPlugin.bossshop.goods.price.PriceManager;
import cc.bukkitPlugin.bossshop.goods.reward.RewardManager;
import cc.bukkitPlugin.bossshop.gui.GuiFactory;
import cc.bukkitPlugin.bossshop.lottery.LotteryManager;
import cc.bukkitPlugin.bossshop.mail.MailManager;
import cc.bukkitPlugin.bossshop.nbt.NBTEditManager;
import cc.bukkitPlugin.bossshop.numbkey.NumbKeyManager;
import cc.bukkitPlugin.bossshop.sale.SaleListener;
import cc.bukkitPlugin.bossshop.sale.SaleManager;
import cc.bukkitPlugin.bossshop.util.AttributeRemover;
import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.hooks.vaultHook.VaultHook;
import cc.bukkitPlugin.commons.plugin.ABukkitPlugin;
import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.bukkitPlugin.commons.util.Statistics;
import me.clip.placeholderapi.PlaceholderAPI;

public class BossShop extends ABukkitPlugin<BossShop>{

    public static boolean RE=false;
    private CommandExc mCommander;
    private SaleListener mSaleLis;
    private static long mLastNoticeTime=0;
    private Statistics mStatistics;
    private BossShopAPI mAPI;

    @Override
    public void onEnable(){
        // 此处注册管理器模块,注意注册的顺序
        this.setLangManager(new LangManager(this));
        this.registerManager(this.getLangManager());
        this.setConfigManager(new ConfigManager(this));
        this.registerManager(this.getConfigManager());
        this.registerManager(new PriceManager(this)); //不需要初始化
        this.registerManager(new RewardManager(this)); //不需要初始化
        this.registerManager(new BuyItemHandler(this)); //不需要初始化
        this.registerManager(new ItemStackCreator(this)); //不需要初始化
        this.registerManager(new ItemStackChecker(this)); //不需要初始化
        this.registerManager(new ShopCustomizer(this)); //不需要初始化
        this.registerManager(new VaultHook<BossShop>(this,true,true,false)); //不需要初始化
        this.registerManager(new WorthHandler(this)); //不需要初始化
        this.registerManager(new ClassManager(this)); //不需要初始化
        this.registerManager(new RecordManager(this)); //需要初始化,无依赖
        this.registerManager(new TimeHandler(this)); //需要初始化,无依赖
        this.registerManager(new TransactionLog(this)); //需要初始化,无依赖
        this.registerManager(new ItemNameManager(this)); //需要初始化,需要主配置
        this.registerManager(new NBTEditManager(this)); //需要初始化,需要主配置
        this.registerManager(new MultiplierHandler(this)); //需要初始化,需要主配置
        this.registerManager(new PointsManager(this)); //需要初始化,需要主配置
        this.registerManager(new SaleManager(this)); //需要初始化,需要主配置
        this.registerManager(new NumbKeyManager(this)); //需要初始化,需要主配置
        this.registerManager(new MailManager(this)); //需要初始化,需要主配置,需要NBT,Rewar,Price模块
        this.registerManager(new BSShopManager(this)); //需要初始化,需要NBT,Rewar,Price模块
        this.registerManager(GuiFactory.getInstance());
        new LotteryManager(this); // 这个管理器无依赖,不需要注册
        // 初始化API
        AttributeRemover.init();
        this.mAPI=new BossShopAPI(this);
        // 注册监听器
        new InventoryListener(this);
        new SignListener(this);
        new PlayerListener(this);
        this.mSaleLis=new SaleListener(this);
        // 绑定命令管理器
        this.mCommander=new CommandExc(this);
        // 载入配置
        this.reloadPlugin(null);
    }

    @Override
    public void onDisable(){
        for(Player sPlayer : BukkitUtil.getOnlinePlayers()){
            Inventory tInv=sPlayer.getOpenInventory().getTopInventory();
            if(tInv!=null&&tInv.getHolder() instanceof BSShopHolder){
                tInv.clear();
                sPlayer.closeInventory();
            }
        }
        super.onDisable();
    }

    public ClassManager getClassManager(){
        return this.getManager(ClassManager.class);
    }

    public static BossShop getInstance(){
        return ABukkitPlugin.getInstance(BossShop.class);
    }

    public BossShopAPI getAPI(){
        return this.mAPI;
    }

    public CommandExc getCommandExc(){
        return this.mCommander;
    }

    public ConfigManager getConfigManager(){
        return (ConfigManager)super.getConfigManager();
    }

    public LangManager getLangManager(){
        return (LangManager)super.getLangManager();
    }

    public SaleListener getSaleListener(){
        return this.mSaleLis;
    }

    public void reloadPlugin(CommandSender pSender){
        BossShop.isEnablePlaceholderAPI();
        super.reloadPlugin(pSender);
        BSReloadedEvent event=new BSReloadedEvent(this);
        Bukkit.getPluginManager().callEvent(event);
    }

    /**
     * 替换占位符为实际字符串
     * @return
     */
    public static String replaceParam(Player pPlayer,String pStr){
        if(pStr==null||pStr.isEmpty())
            return pStr;
        if(!BossShop.isEnablePlaceholderAPI())
            return ChatColor.translateAlternateColorCodes('&',pStr);
        try{
            pStr=PlaceholderAPI.setPlaceholders(pPlayer,pStr);
        }catch(Throwable exp){
            if(BossShop.getInstance()!=null)
                Log.severe("在调用PlaceHolderAPI是发生了异常,请确保你的插件和PlaceHolderAPI模块版本匹配",exp);
        }
        return pStr;
    }

    private static boolean isEnablePlaceholderAPI(){
        if(BossShop.RE)
            return true;
        if(!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")){
            BossShop tPlugin=BossShop.getInstance();
            if(mLastNoticeTime+600000<System.currentTimeMillis()&&tPlugin!=null){
                Log.severe("未发现PlaceholderAPI插件,BossShop插件可能需要该插件来完善功能");
                Log.severe("请前往https://www.spigotmc.org/resources/placeholderapi.6245 下载");
                mLastNoticeTime=System.currentTimeMillis();
            }
            return false;
        }else{
            BossShop.RE=true;
            return true;
        }
    }

    public static void soundNotifyPlayer(Player pPlayer){
        if(pPlayer==null||BossShop.getInstance()==null)
            return;
        Sound tNotifySound=BossShop.getInstance().getConfigManager().getNotifySound();
        if(tNotifySound!=null)
            pPlayer.playSound(pPlayer.getLocation(),tNotifySound,1F,1F);
    }

}
