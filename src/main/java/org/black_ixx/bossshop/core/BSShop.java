package org.black_ixx.bossshop.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.managers.BuyItemHandler;
import org.black_ixx.bossshop.managers.ConfigManager;
import org.black_ixx.bossshop.managers.ItemStackCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import cc.bukkitPlugin.bossshop.lottery.LotteryHolder;
import cc.bukkitPlugin.bossshop.mail.MailManager;
import cc.bukkitPlugin.bossshop.sale.SaleManager;
import cc.bukkitPlugin.bossshop.util.AttributeRemover;
import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.AFileManager;
import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.util.StringUtil;

public class BSShop extends AFileManager<BossShop>{

    private static final Pattern PATTERN=Pattern.compile("[%]([^%]+)[%]");
    public final String mShopName;
    private String sign_text="[BossShop]";
    private String mDisplayname="[BossShop]";
    private boolean needPermToCreateSign=true;
    private boolean mValid=true;
    private boolean mSpecial=false;
    private int mInvSize=9;
    private int mTotalWeight=0;
    /** 请勿保存empty对象 */
    private final HashMap<Integer,ArrayList<BSGoods>> mGoodsPosition=new HashMap<>();
    /***/
    private final TreeMap<String,BSGoods> mGoodses=new TreeMap<>();

    /** 查看商店的人,此表用于更新商店时使用,防止遍历全服时卡顿 */
    private final Map<Player,BSShopHolder> mCustomers=new ConcurrentHashMap<Player,BSShopHolder>();
    private String mEnterMsg="";
    private String mLeaveMsg="";
    private int mRightClickBuyCount=1;
    private boolean mAllowChangeRightClickBuyCount=false;
    private boolean mShowRightClickBuyCount=true;
    public boolean mAllowShiftLeftClickBuy=false;
    private boolean mHasSpace=false;
    private int mHideItemFlag=0;

    /**
     * 新建一个商店
     * 
     * @param fileName
     *            /shops/下的商店配置文件名字
     */
    public BSShop(BossShop pPlugin,String fileName,String shopName){
        super(pPlugin,"shops/"+fileName,"1.2");
        this.mShopName=shopName;
    }

    /**
     * 重载商店配置
     * <p>
     * 请使用{@link BSShop#reloadShop(CommandSender)}来重载商店
     * </p>
     * 
     * @return 是否载入
     */
    @Override
    @Deprecated
    public boolean reloadConfig(CommandSender pSender){
        Log.send(pSender,C("MsgStartReloadShop")+this.mShopName);
        if(!super.reloadConfig(pSender))
            return false;
        synchronized(this){
            this.mGoodsPosition.clear();
            this.mGoodses.clear();
        }
        this.checkUpdate();
        this.addDefaults();
        this.sign_text=this.mConfig.getString("signs.text",this.sign_text);
        this.needPermToCreateSign=this.mConfig.getBoolean("signs.NeedPermissionToCreateSign");
        this.mSpecial=this.mConfig.getBoolean("Setting.ShopTypeSpecial",false);
        this.mValid=true;
        this.mDisplayname=ChatColor.translateAlternateColorCodes('&',this.mConfig.getString("Setting.DisplayName",this.mShopName));
        if(this.mDisplayname.length()>32){
            Log.send(pSender,C("MsgShopNameTooLongOver32"));
            this.mDisplayname=this.mDisplayname.substring(0,32);
        }
        this.mEnterMsg=ChatColor.translateAlternateColorCodes('&',this.mConfig.getString("Setting.EnterMessage",""));
        this.mLeaveMsg=ChatColor.translateAlternateColorCodes('&',this.mConfig.getString("Setting.LeaveMessage",""));
        this.mHideItemFlag=this.mConfig.getInt("Setting.HideItemFlag",this.mHideItemFlag);
        this.mAllowChangeRightClickBuyCount=this.mConfig.getBoolean("Setting.AllowChangeRightClickBuyCount",this.mAllowChangeRightClickBuyCount);
        this.mShowRightClickBuyCount=this.mConfig.getBoolean("Setting.ShowRightClickBuyCount",this.mShowRightClickBuyCount);
        this.mAllowShiftLeftClickBuy=this.mConfig.getBoolean("Setting.AllowShiftLeftClickBuy",this.mAllowShiftLeftClickBuy);
        this.mRightClickBuyCount=this.mConfig.getInt("Setting.RightClickBuyCount",this.mRightClickBuyCount);
        if(this.mRightClickBuyCount<=0){
            this.mRightClickBuyCount=1;
        }
        this.loadItems();
        this.finishedAddingItems();
        { //清理无用的节点
            this.mConfig.set("ShopName",null);
            this.mConfig.set("DisplayName",null);
        }
        if(!this.mPlugin.getManager(SaleManager.class).isSortShop(this)||!this.trySortGoods()){
            this.saveConfig(null);
        }
        return true;
    }

    @Override
    protected boolean checkUpdate(){
        if(!super.checkUpdate())
            return false;
        String tVersion=this.mConfig.getString(SEC_CFG_VERSION,"1.0");
        if(tVersion.compareToIgnoreCase("1.0")==0){// 1.0-->1.1
            tVersion="1.1";
            CommentedSection settingSec=this.mConfig.getSection("Setting");
            if(settingSec==null)
                settingSec=this.mConfig.createSection("Setting");
            settingSec.set("EnterMessage",this.mConfig.removeCommentedValue("EnterMessage"));
            settingSec.set("LeaveMessage",this.mConfig.removeCommentedValue("LeaveMessage"));
            settingSec.set("ShopTypeSpecial",this.mConfig.removeCommentedValue("ShopTypeSpecial"));
            settingSec.set("RightClickBuyCount",this.mConfig.removeCommentedValue("RightClickBuyCount"));
            settingSec.set("ShowRightClickBuyCount",this.mConfig.removeCommentedValue("ShowRightClickBuyCount"));
            settingSec.set("AllowShiftLeftClickBuy",this.mConfig.removeCommentedValue("AllowShiftLeftClickBuy"));
        }
        if(tVersion.compareToIgnoreCase("1.1")==0){// 1.1-->1.2
            tVersion="1.2";
            CommentedSection settingSec=this.mConfig.getSection("Setting");
            if(settingSec==null)
                settingSec=this.mConfig.createSection("Setting");
            settingSec.set("DisplayName",this.mConfig.removeCommentedValue("DisplayName"));
        }
        this.mConfig.set(SEC_CFG_VERSION,this.mVersion);
        return true;
    }

    @Override
    protected void addDefaults(){
        super.addDefaults();
        ConfigManager cfgMan=(ConfigManager)this.mPlugin.getConfigManager();
        this.mConfig.addDefault("ShopName",this.mShopName);
        this.mConfig.addDefault("signs.text","[ExtraShop]");
        this.mConfig.addDefault("signs.NeedPermissionToCreateSign",false);
        this.mConfig.addDefault("Setting.EnterMessage","","进入商店时的消息,如果留空,则不显示消息");
        this.mConfig.addDefault("Setting.LeaveMessage","","离开商店时的消息,如果留空,则不显示消息");
        this.mConfig.addDefault("Setting.ShopTypeSpecial",false,"如果为true,则该商店为抽奖商店");
        this.mConfig.addDefault("Setting.RightClickBuyCount",cfgMan.getRightClickBuyCount(),"右击物品时,商品默认的购买数量","如果商品本身已经配置过该节点,将会参照商品自身的配置");
        this.mConfig.addDefault("Setting.AllowChangeRightClickBuyCount",cfgMan.allowChangeRightClickBuyCount(),"玩家是否可以设置右键购买数量","免费物品强制不能设置","如果商品本身已经配置过该节点,将会参照商品自身的配置");
        this.mConfig.addDefault("Setting.ShowRightClickBuyCount",cfgMan.showRightClickBuyCount(),"当右键购买数量大于1时,显示右键购买数量","物品设置为false可以用来自定义右键购买数量Lore消息","如果商品本身已经配置过该节点,将会参照商品自身的配置");
        this.mConfig.addDefault("Setting.AllowShiftLeftClickBuy",cfgMan.allowShiftLeftClickBuy(),"玩家是否可以使用Shift+左键购买最大数量物品","如果商品本身已经配置过该节点,将会参照商品自身的配置");        
        this.mConfig.addDefault("Setting.HideItemFlag",cfgMan.getHideItemFlag(),"商品属性显示配置,说明参考配置","如果商品本身已经配置过该节点,将会参照商品自身的配置");
        if(this.mConfig.getSection("shop")==null){
            this.mConfig.createSection("shop");
        }
    }

    public int getHideItemFlag(){
        return this.mHideItemFlag;
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

    public boolean allowShiftLeftClickBuy(){
        return this.mAllowShiftLeftClickBuy;
    }
    
    /** 获取商店机读名字,文件名字 */
    public String getShopName(){
        return mShopName;
    }

    /** 获取商店显示名字 */
    public String getDisplayName(){
        return mDisplayname;
    }

    /** 获取进入商店时的消息 */
    public String getEnterMessage(){
        return this.mEnterMsg;
    }

    /** 获取离开商店时的消息 */
    public String getLeaveMessage(){
        return this.mLeaveMsg;
    }

    /** 获取创建牌子商店时,显示在牌子上的商店名字 */
    public String getSignText(){
        return sign_text;
    }

    public boolean needPermToCreateSign(){
        return needPermToCreateSign;
    }

    public int getInventorySize(){
        return mInvSize;
    }

    /** 获取商店的所有物品+位置,请注意使用同步块 */
    public Map<Integer,ArrayList<BSGoods>> getGoodsesPosition(){
        return Collections.unmodifiableMap(this.mGoodsPosition);
    }

    /** 获取商店第一层的物品 */
    public TreeMap<String,BSGoods> getTopGoodses(){
        TreeMap<String,BSGoods> tTopGoodses=new TreeMap<>();
        BSGoods tGoods;
        for(ArrayList<BSGoods> sGoods : this.mGoodsPosition.values()){
            if(!sGoods.isEmpty()){
                tGoods=sGoods.get(0);
                tTopGoodses.put(tGoods.getName(),tGoods);
            }
        }
        return tTopGoodses;
    }

    public Set<BSGoods> getAllGoods(){
        synchronized(this){
            HashSet<BSGoods> t=new HashSet<>(this.mGoodses.values());
            return t;
        }
    }

    /**
     * 获取商店下一个可用的上架位置,从1开始,<=0为无位置
     */
    public int getNextAvailableLoc(){
        Set<Integer> tPos=this.getItemPositions();
        for(int i=0;i<81;i++){
            if(!tPos.contains(i))
                return i+1;
        }
        return 0;
    }

    @Deprecated
    public BSGoods getGoods(int pPostion){
        synchronized(this){
            ArrayList<BSGoods> tGoodss=this.mGoodsPosition.get(pPostion);
            if(tGoodss==null||tGoodss.isEmpty())
                return null;
            return tGoodss.get(0);
        }
    }

    /**
     * 根据物品名(此为配置文件节点名,非显示的名字),获取商店物品
     * 
     * @param pName
     *            物品名
     * @return 如果不存在返回null
     */
    public BSGoods getGoods(String pName){
        if(StringUtil.isEmpty(pName))
            return null;

        synchronized(this){
            return this.mGoodses.get(pName);
        }
    }

    /**
     * 获取商店物品位置Set集合的拷贝
     */
    public Set<Integer> getItemPositions(){
        synchronized(this){
            Set<Integer> pos=new HashSet<Integer>();
            pos.addAll(this.mGoodsPosition.keySet());
            return pos;
        }
    }

    /**
     * 添加商品到商店
     * 
     * @param tGoods
     *            商品奖励
     * @param tMenuItem
     *            菜单物品
     * @param pUpdateInv
     *            是否更新打开该商店的玩家的背包
     */
    public void addShopItem(BSGoods tGoods,boolean pUpdateInv){
        boolean tNeedReedit=false;
        // 检查物品属性中是否存在变量
        ItemStack tMenuItem=tGoods.getRawMenuItem();
        if(tMenuItem.hasItemMeta()){
            ItemMeta tMeta=tMenuItem.getItemMeta();
            if(tMeta instanceof SkullMeta){
                SkullMeta skullMeta=(SkullMeta)tMeta;
                if(skullMeta.hasOwner())
                    tNeedReedit=PATTERN.matcher(skullMeta.getOwner()).find();
            }
            if(tMeta.hasDisplayName()){
                if(!tNeedReedit)
                    tNeedReedit=PATTERN.matcher(tMeta.getDisplayName()).find();
            }
            if(tMeta.hasLore()){
                List<String> tList=tMeta.getLore();
                List<String> tNewLore=new ArrayList<String>();
                for(String sLine : tList){
                    tNewLore.add(tGoods.transformMessage(sLine,1));
                    if(!tNeedReedit)
                        tNeedReedit=PATTERN.matcher(sLine).find();
                }
                tMeta.setLore(tNewLore);
            }
            if(!BukkitUtil.isItemMetaEmpty(tMeta)){
                tMenuItem.setItemMeta(tMeta);
            }
        }

        if(!tNeedReedit){
            tNeedReedit|=tGoods.hasLimit()||tGoods.hasBuyTime()||tGoods.hasPersonalLimit();
            tNeedReedit|=tGoods.showRightClickBuyCount()&&(tGoods.getRightClickBuyCount(null)>1);
            tNeedReedit|=tGoods.allowShiftLeftClickBuy();
        }
        tGoods.setNeedEdit(tNeedReedit);
        tMenuItem=AttributeRemover.hideAttributes(tMenuItem,tGoods.getHideItemFlag());
        int location=tGoods.getInventoryLocation();
        synchronized(this){
            ArrayList<BSGoods> tGoodss=this.mGoodsPosition.get(location);
            if(tGoodss==null){
                tGoodss=new ArrayList<>();
                this.mGoodsPosition.put(location,tGoodss);
            }
            tGoodss.add(tGoods);
            this.mGoodses.put(tGoods.getName(),tGoods);
            this.mTotalWeight+=tGoods.getWeight()>0?tGoods.getWeight():0;
            if(pUpdateInv){
                for(BSShopHolder sHolder : this.mCustomers.values()){
                    sHolder.addGoods(tGoods);
                }
            }
        }
    }

    /**
     * 重新设置商店背包大小,在每次重载商店后调用
     */
    private void resetInventorySize(){
        synchronized(this){
            int highest=0;
            for(Integer pos : this.mGoodsPosition.keySet()){
                if(pos>highest)
                    highest=pos;
            }
            highest++;
            this.mInvSize=BSShop.getSuitableSize(highest);
        }
    }

    public void finishedAddingItems(){
        this.resetInventorySize();
    }

    /**
     * 获取合适的背包大小
     */
    public static int getSuitableSize(int pSize){
        if(pSize>72)
            return 81;
        int m=pSize/9;
        if(m*9<pSize)
            pSize=(m+1)*9;
        else pSize=m*9;
        return pSize;
    }

    public int getTotalWeight(){
        return this.mTotalWeight;
    }

    /**
     * 为特定的玩家加载定制的背包
     * <p>
     * 外部勿直接调用该函数,请通过{@link BSShopManager#openShop(Player, BSShop)}来打开商店<br />
     * 所有的信息均存储在holder中
     * </p>
     * 
     * @param pPlayer
     *            玩家
     */
    public void openInventory(Player pPlayer){
        if(!this.mValid)
            return;

        BSShopHolder holder=null;
        if(this.mSpecial)
            holder=new LotteryHolder((BossShop)this.mPlugin,pPlayer,this);
        else holder=new BSShopHolder((BossShop)this.mPlugin,pPlayer,this);
        Inventory inventory=Bukkit.createInventory(holder,this.mInvSize,this.mDisplayname);
        holder.setInventory(inventory);
        holder.updateInventory(false);
        pPlayer.openInventory(inventory);
        this.mCustomers.put(pPlayer,holder);
        if(this.mHasSpace&&this.mPlugin.getManager(SaleManager.class).isSortShop(this)){
            this.trySortGoods();
        }
    }

    public void addCustomer(Player pPlayer,BSShopHolder pInvHolder){
        this.mCustomers.put(pPlayer,pInvHolder);
    }

    public BSShopHolder removeCustomer(Player pPlayer){
        return this.mCustomers.remove(pPlayer);
    }

    /**
     * 获取打开当前商店所有玩家的拷贝Set
     */
    public Set<Player> getCustomers(){
        HashSet<Player> t=new HashSet<>(this.mCustomers.keySet());
        return t;
    }

    /**
     * 获取打开当前商店所有玩家背包Holder的拷贝Set
     */
    public Set<BSShopHolder> getCustomersHolder(){
        HashSet<BSShopHolder> t=new HashSet<>(this.mCustomers.values());
        return t;
    }

    /**
     * 为指定玩家更新当前打开的背包
     * 
     * @param player
     *            指定的玩家
     */
    public void updateInventory(Player player,boolean pReset){
        InventoryView tView;
        Inventory tInv;

        if((tView=player.getOpenInventory())!=null&&(tInv=tView.getTopInventory())!=null){
            this.updateInventory(tInv,player,pReset);
        }

    }

    /**
     * 为指定玩家更新指定的背包,如果商店已经无效,将会清空玩家背包并关闭
     * 
     * @param inv
     *            指定的背包
     * @param player
     *            指定的玩家
     */
    public void updateInventory(Inventory inv,Player player,boolean pReset){
        if(!(inv.getHolder() instanceof BSShopHolder))
            return;
        BSShopHolder tHolder=(BSShopHolder)inv.getHolder();
        tHolder.updateInventory(pReset);
        if(this.mHasSpace&&this.mPlugin.getManager(SaleManager.class).isSortShop(this)){
            this.trySortGoods();
        }
    }

    /**
     * 为所有玩家更新打开此商店的背包,如果商店已经无效,将会清空玩家背包并关闭
     */
    public void updateAllInventory(boolean pReset){
        // 由于更新背包时,一些背包会关闭,所以选择复制玩家列表
        for(Player sPlayer : this.getCustomers()){
            this.updateInventory(sPlayer,pReset);
        }
    }

    /**
     * 检查商店是否特殊类型的商店
     */
    public boolean isShopSpecial(){
        return this.mSpecial;
    }

    /**
     * 检查商店是否合法,比如重载后文件不存在
     */
    public boolean isShopValid(){
        return this.mValid;
    }

    public void loadItems(){
        CommentedSection shop_sec=this.mConfig.getSection("shop");
        if(shop_sec==null)
            shop_sec=this.mConfig.createSection("shop");
        for(String key : shop_sec.getKeys(false)){
            this.loadItem(shop_sec,shop_sec.getSection(key),false);
        }
    }

    /**
     * 加载商店指定节点的物品
     * 
     * @param pShopSection
     *            商店节点,可以为null
     * @param pItemSection
     *            物品节点,不能为null
     * @param pUpdateInv
     *            是否更新打开该商店的玩家的背包
     * @return 加载成功的物品或null
     */
    public BSGoods loadItem(CommentedSection pShopSection,CommentedSection pItemSection,boolean pUpdateInv){
        if(pItemSection==null)
            return null;
        if(pShopSection==null){
            pShopSection=this.mConfig.getSection("shop");
            if(pShopSection==null){
                pShopSection=this.mConfig.createSection("shop");
                return null;
            }
        }
        List<String> menusec=pItemSection.getStringList("MenuItem");
        if(menusec==null){
            Log.severe(this.C("MsgFailCreateGoods").replace("%goods%",pItemSection.getName())+","+this.C("MsgMenuItemConfigNotFound"));
            return null;
        }
        ItemStack menu=this.mPlugin.getManager(ItemStackCreator.class).createItemStackS(menusec,true);
        if(menu==null)
            return null;
        BSRewardParameter tParam=this.mPlugin.getManager(BuyItemHandler.class).createItemParam(this,pItemSection);
        if(tParam==null)
            return null;;

        BSGoods items=new BSGoods(this.mPlugin,menu,tParam);
        if(items.hasOwner()){
            if(items.getLimit()==0){
                pShopSection.set(pItemSection.getName(),null);
                return null;
            }
            if(!items.isBuyTime()){
                ((BossShop)this.mPlugin).getManager(MailManager.class).addMail(items,this.mPlugin.C("MsgFromOutDateSale"));
                pShopSection.set(pItemSection.getName(),null);
                return null;
            }
        }
        this.addShopItem(items,pUpdateInv);
        return items;
    }

    /**
     * 删除商店配置文件中的商品
     * 
     * @param pName
     *            商品名字
     * @param pSave
     *            是否保存配置
     * @param pUpdate
     *            是否更新玩家背包
     */
    public CommentedSection removeItem(String pName,boolean pSave,boolean pUpdate){
        CommentedSection sec=this.mConfig.getSection("shop."+pName);
        this.mConfig.set("shop."+pName,null);

        synchronized(this){
            BSGoods tGoods=this.getGoods(pName);
            if(tGoods==null)
                return null;

            int location=tGoods.getInventoryLocation();
            ArrayList<BSGoods> tGoodses=this.mGoodsPosition.get(location);
            tGoodses.remove(tGoods);
            if(tGoodses.isEmpty()){
                this.mGoodsPosition.remove(location);
            }
            this.mGoodses.remove(pName);

            if(pSave)
                this.saveConfig(null);
            if(pUpdate){
                if(this.mPlugin.getManager(SaleManager.class).isSortShop(this)){
                    this.trySortGoods();
                }
                for(BSShopHolder sHolder : this.mCustomers.values()){
                    sHolder.removeGoods(tGoods);
                }
            }else{
                mHasSpace=true;
            }
        }
        return sec;
    }

    /**
     * 重新载入商店
     * <p>
     * 重载商店使用异步方法
     * </p>
     * 
     * @return
     */
    public boolean reloadShop(CommandSender pSender){
        if(!this.mFile.isFile()){
            Log.send(pSender,C("MsgShopFileNotExist")+"["+this.mShopName+"]");
            this.mValid=false;
            this.mGoodsPosition.clear();
            this.mConfig.set("shop",null);
            return false;
        }
        return this.reloadConfig(pSender);
    }

    /**
     * 返回商店中寄售商品之间是否存在空的格子
     */
    public boolean hasSpace(){
        return this.mHasSpace;
    }

    /**
     * 尝试重新排序商品
     * <p>
     * 只排序寄售的物品
     * </p>
     * 
     * @return 是否排序了
     */
    public boolean trySortGoods(){
        this.mHasSpace=false;
        // 判断需不需要排序
        synchronized(this){
            int maxInvLoc=-1,tSaleNumb=0;
            for(Map.Entry<Integer,ArrayList<BSGoods>> sEntry : this.mGoodsPosition.entrySet()){
                ArrayList<BSGoods> tGoodss=sEntry.getValue();
                if(tGoodss.isEmpty()||!tGoodss.get(0).hasOwner())
                    continue;
                maxInvLoc=Math.max(maxInvLoc,sEntry.getKey());
                tSaleNumb++;
            }
            if(maxInvLoc==tSaleNumb-1)
                return false;

            HashSet<Integer> noMoveLoc=new HashSet<>();
            TreeMap<Integer,ArrayList<BSGoods>> newOrder=new TreeMap<>();
            int handleLoc=-1;
            // 查找排序位置
            for(Map.Entry<Integer,ArrayList<BSGoods>> sEntry : this.mGoodsPosition.entrySet()){
                ArrayList<BSGoods> tGoodss=sEntry.getValue();
                if(tGoodss.isEmpty())
                    continue;
                Integer newLoc=sEntry.getKey();
                if(!tGoodss.get(0).hasOwner()){
                    noMoveLoc.add(sEntry.getKey());
                }else{
                    while(noMoveLoc.contains(++handleLoc));
                    newLoc=handleLoc;
                }
                newOrder.put(newLoc,sEntry.getValue());
            }
            // 执行调整
            for(Map.Entry<Integer,ArrayList<BSGoods>> sEntry : newOrder.entrySet()){
                ArrayList<BSGoods> tGoodss=sEntry.getValue();
                if(tGoodss.get(0).getInventoryLocation()==sEntry.getKey())
                    continue;

                for(BSGoods sGoods : tGoodss){
                    sGoods.getConfigurationSection().set("InventoryLocation",sEntry.getKey()+1);
                    sGoods.setInventoryLocation(sEntry.getKey());
                }
            }
            this.mGoodsPosition.clear();
            this.mGoodsPosition.putAll(newOrder);
            this.saveConfig(null);
        }
        return true;
    }

}
