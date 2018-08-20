package org.black_ixx.bossshop.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.PriceType;
import org.black_ixx.bossshop.core.BSEnums.RewardType;
import org.black_ixx.bossshop.events.BSPlayerPurchaseEvent;
import org.black_ixx.bossshop.events.BSPlayerPurchasedEvent;
import org.black_ixx.bossshop.managers.ItemNameManager;
import org.black_ixx.bossshop.managers.ItemStackChecker;
import org.black_ixx.bossshop.managers.MultiplierHandler;
import org.black_ixx.bossshop.managers.RecordManager;
import org.black_ixx.bossshop.managers.ShopCustomizer;
import org.black_ixx.bossshop.managers.TransactionLog;
import org.black_ixx.bossshop.managers.WorthHandler;
import org.black_ixx.bossshop.points.PointsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import cc.bukkitPlugin.bossshop.goods.price.IPrice;
import cc.bukkitPlugin.bossshop.goods.price.PriceItem;
import cc.bukkitPlugin.bossshop.goods.price.PriceItem.PriceInfo;
import cc.bukkitPlugin.bossshop.goods.reward.IReward;
import cc.bukkitPlugin.bossshop.goods.reward.RewardEnchantment;
import cc.bukkitPlugin.bossshop.goods.reward.RewardPermission;
import cc.bukkitPlugin.bossshop.lottery.LotteryHolder;
import cc.bukkitPlugin.bossshop.lottery.LotteryStatus;
import cc.bukkitPlugin.bossshop.mail.MailManager;
import cc.bukkitPlugin.bossshop.sale.SaleManager;
import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.hooks.vaultHook.EconomyHook;
import cc.bukkitPlugin.commons.hooks.vaultHook.VaultHook;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.tellraw.ClickEvent;
import cc.bukkitPlugin.commons.tellraw.HoverEvent;
import cc.bukkitPlugin.commons.tellraw.Tellraw;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.util.StringUtil;

public class BSGoods{

    private static long mLastClear=0;
    private static long mClearDis=3600000;
    private static HashMap<UUID,Long> mWait=new HashMap<>();

    private BossShop mPlugin;
    /**
     * 商品数量限制<br />
     * 如果<=-1则为无限制
     */
    private int mNumberLimit=-1;
    /**
     * 商品个人购买限制<br />
     * 如果<=-1则为无限制
     */
    private int mPersonalLimit=-1;
    /** 商品背包中的位置(此为数组位置) */
    private int mLocation;
    /** 商品权重,用于抽奖 */
    private int mWeight=0;
    /** 商品属性显示配置 */
    private int mHideItemFlag=0;
    private boolean perm_is_group=false;
    /** 是否在没有库存的时候隐藏商品 */
    private boolean mHideNoStock=false;
    /** 是否在商品不是购买时间的时候隐藏商品 */
    private boolean mHideNotTime=false;
    /** 购买商品需要的权限 */
    private String mPermission;
    /** 商品机读名 */
    private final String mName;
    /** 购买商品后的消息 */
    private String mMsg="";
    /** 商品所有者,用于寄售物品 */
    private UUID mOwner=null;
    /** 第一个的商品奖励 */
    private IReward mRewardFirst=null;
    /** 所有的商品奖励 */
    private List<IReward> mRewards=new ArrayList<>();
    /** 第一个商品价格 */
    private IPrice mPriceFirst=null;
    /** 所有的价格 */
    private List<IPrice> mPrices=new ArrayList<>();
    /** 菜单物品 */
    private ItemStack mMenuItem;
    /**
     * 商品停止销售的时间<br />
     * 如果为null则无限制
     */
    private Date mStopTime=null;
    /**
     * 商品开始销售的时间<br />
     * 如果为null则无限制
     */
    private Date mStartTime=null;
    /** 商品所在的商店 */
    private BSShop mShop=null;
    /**
     * 商品名字已经Lore中是否存在变量<br />
     * 用于指示商品在显示给玩家时,是否需要遍历商品属性来替换变量值
     */
    private boolean mNeedEdit=true;
    /** 商品右键购买的数量 */
    private int mRightClickBuyCount=1;
    /** 是否显示右键购买数量 */
    public boolean mShowRightClickBuyCount=true;
    /** 是否开启shift+左键全部购买*/
    public boolean mAllowShiftLeftClickBuy=false;
    /** 是否一次性购买多个 */
    private boolean mAllowMulti=true;

    public BSGoods(BossShop pPlugin,ItemStack pMenuItem,BSRewardParameter parameter){
        this.mPlugin=pPlugin;

        this.mMenuItem=pMenuItem;

        this.mShop=parameter.shop;
        this.mRewardFirst=parameter.reward.iterator().next();
        this.mRewards.addAll(parameter.reward);
        this.mPriceFirst=parameter.price.iterator().next();
        this.mPrices.addAll(parameter.price);
        this.mName=parameter.name;
        this.mMsg=StringUtil.isEmpty(parameter.msg)?"":ChatColor.translateAlternateColorCodes('&',parameter.msg);
        this.mOwner=parameter.owner;
        this.mLocation=parameter.location;
        this.mNumberLimit=parameter.numberLimit;
        this.mPersonalLimit=parameter.personalLimit;
        this.mStartTime=parameter.startTime;
        this.mStopTime=parameter.stopTime;
        this.mHideNoStock=parameter.hideNoStock;
        this.mHideNotTime=parameter.hideNotTime;
        this.mHideItemFlag=parameter.hideItemFlag&0xFFFFFFFF;
        this.mWeight=parameter.weight;

        for(IReward sReward : this.mRewards)
            mAllowMulti&=sReward.allowMultiple();

        if(mAllowMulti){
            if(parameter.numberLimit!=-1)
                this.mRightClickBuyCount=Math.min(parameter.rightClickBuyCount,parameter.numberLimit);
            else this.mRightClickBuyCount=parameter.rightClickBuyCount;
        }else{
            this.mRightClickBuyCount=1;
        }
        this.mShowRightClickBuyCount=parameter.showRightClickBuyCount;
        this.mAllowShiftLeftClickBuy=parameter.allowShiftLeftClickBuy;
        
        if(StringUtil.isNotEmpty(parameter.permission)){
            mPermission=parameter.permission;
            if(mPermission.startsWith("[")&&mPermission.endsWith("]")&&mPermission.length()>2){
                String group=mPermission.substring(1,mPermission.length()-1);
                if(group!=null){
                    this.mPermission=group;
                    perm_is_group=true;
                }
            }
        }
    }

    /**
     * 检查是否太快的点击了商店
     * <p>
     * 如果点击很快,函数内会发出提示
     * </p>
     * 
     * @param pPlayer
     *            玩家
     * @return 是否点击很快
     */
    public static boolean isClickTooQuick(Player pPlayer){
        BossShop tPlugin=BossShop.getInstance();
        Long time=BSGoods.mWait.put(pPlayer.getUniqueId(),System.currentTimeMillis());
        if(time!=null&&(time+tPlugin.getConfigManager().getCoolTime())>System.currentTimeMillis()){
            Log.send(pPlayer,tPlugin.getLangManager().getNode("MsgYouClickTooQuick"));
            return true;
        }
        if((BSGoods.mLastClear+BSGoods.mClearDis)<=System.currentTimeMillis()){
            for(Iterator<Entry<UUID,Long>> it=BSGoods.mWait.entrySet().iterator();it.hasNext();){
                if((it.next().getValue()+tPlugin.getConfigManager().getCoolTime()*1000)<System.currentTimeMillis())
                    it.remove();
            }
            BSGoods.mLastClear=System.currentTimeMillis();
        }
        return false;
    }

    /**
     * 获取指定玩家右键购买物品的数量
     * <p>
     * 如果指定的玩家为null,将返回默认的右键购买数量
     * </p>
     * 
     * @param pPlayer
     *            指定的玩家
     * @return 右键购买的数量
     */
    public int getRightClickBuyCount(Player pPlayer){
        return Math.min(this.mRightClickBuyCount,this.getMaxBuyCount(pPlayer));
    }

    /**
     * 返回玩家最大允许的购买数量
     * <p>
     * 如果无限制,则返回{@link Integer#MAX_VALUE}
     * </p>
     * 
     * @param pPlayer
     *            玩家,可以为null
     * @return 可购买的数量
     */
    public int getMaxBuyCount(Player pPlayer){
        int canBuyCount=Integer.MAX_VALUE;
        if(this.hasLimit()){
            canBuyCount=this.getLimit();
        }

        boolean tAllFree=true;
        for(IPrice sPrice : this.mPrices){
            if(sPrice.getPriceType()!=PriceType.Free){
                tAllFree=false;
                break;
            }
        }

        if(!this.mAllowMulti||tAllFree){
            canBuyCount=Math.min(1,canBuyCount);
        }
        if(this.hasPersonalLimit()&&pPlayer!=null){
            int alreadyBuyCount=this.mPlugin.getManager(RecordManager.class).getBuyRecord(this.mShop.getShopName(),this.mName,pPlayer.getUniqueId());
            if(alreadyBuyCount>=this.getPersonalLimit())
                canBuyCount=0;
            else canBuyCount=Math.min(canBuyCount,this.getPersonalLimit()-alreadyBuyCount);
        }
        return canBuyCount<0?0:canBuyCount;
    }

    public boolean showRightClickBuyCount(){
        return this.mShowRightClickBuyCount;
    }

    public boolean allowShiftLeftClickBuy(){
        return this.mAllowShiftLeftClickBuy;
    }
    
    public int getWeight(){
        return this.mWeight;
    }

    /**
     * 获取物品如何显示属性的配置
     */
    public int getHideItemFlag(){
        return this.mHideItemFlag;
    }

    /**
     * 获取该商品的所有者,如果不存在则为null
     * 
     * @return
     */
    public UUID getOwner(){
        return this.mOwner;
    }

    public boolean hasOwner(){
        return this.mOwner!=null;
    }

    /** 获取菜单物品的拷贝 */
    public ItemStack getMenuItem(){
        return this.mMenuItem.clone();
    }

    /** 获取菜单物品 */
    @Deprecated
    public ItemStack getRawMenuItem(){
        return this.mMenuItem;
    }

    public IReward getFirstReward(){
        return this.mRewardFirst;
    }

    public List<IReward> getRewards(){
        return this.mRewards;
    }

    public IPrice getFirstPrice(){
        return this.mPriceFirst;
    }

    public List<IPrice> getPrices(){
        return this.mPrices;
    }

    public boolean isHideNoStock(){
        return this.mHideNoStock;
    }

    public boolean isHideNotTime(){
        return this.mHideNotTime;
    }

    public String getShopName(){
        return this.mShop.getShopName();
    }

    public BSShop getShop(){
        return this.mShop;
    }

    public String getMessage(){
        return this.mMsg;
    }

    public int getInventoryLocation(){
        return this.mLocation;
    }

    public String getName(){
        return this.mName;
    }

    public boolean isBuyTime(){
        Date now=new Date();
        if(this.mStartTime!=null&&this.mStartTime.after(now))
            return false;
        if(this.mStopTime!=null&&this.mStopTime.before(now))
            return false;
        return true;
    }

    public boolean hasBuyTime(){
        return this.mStartTime!=null||this.mStopTime!=null;
    }

    public Date getStartTime(){
        return this.mStartTime;
    }

    public Date getStopTime(){
        return this.mStopTime;
    }

    /**
     * 获取个人购买数量限制,如果不限制则为-1
     * 
     * @return
     */
    public int getPersonalLimit(){
        return this.mPersonalLimit;
    }

    public boolean hasPersonalLimit(){
        return this.mPersonalLimit>-1;
    }

    public String C(String pNode){
        return this.mPlugin.getLangManager().getNode(pNode);
    }

    /**
     * 获取物品的配置节点
     */
    public CommentedSection getConfigurationSection(){
        return ((BSShop)mShop).getConfig().getSection("shop."+mName);
    }

    public boolean hasPermission(Player p,boolean msg){
        if(StringUtil.isEmpty(mPermission))
            return true;
        if(perm_is_group){
            boolean no_group=true;
            for(String group : this.mPlugin.getManager(VaultHook.class).getPermission().getPlayerGroups(p)){
                no_group=false;
                if(group.equalsIgnoreCase(mPermission)){
                    return true;
                }
            }
            if(no_group&&mPermission.equalsIgnoreCase("default")){
                return true;
            }
            if(msg){
                Log.send(p,C("MsgNoPermitToBuyThisItem"));
            }
            return false;
        }
        if(p.hasPermission(mPermission))
            return true;
        if(msg){
            Log.send(p,C("MsgNoPermitToBuyThisItem"));
        }
        return false;
    }

    public boolean isExtraPermissionExisting(){
        if(StringUtil.isEmpty(mPermission))
            return false;
        return true;
    }

    public boolean alreadyBought(Player pPlayer){
        for(IReward sReward : this.mRewards){
            if(sReward.getRewardType()==RewardType.Permission){
                for(String sPer : ((RewardPermission)sReward).getReward(1)){ // Loop durch Perms
                    if(!pPlayer.hasPermission(sPer)){ // Sobald ein Spieler eine Perm nicht
                        return false;
                    }
                }
                return true; // Hat ein Spieler alle Perms wird true returned
            }
        }
        return false;
    }

    public Tellraw getPersonalChatMenuItem(Player pPlayer){
        ShopCustomizer shopCusMan=this.mPlugin.getManager(ShopCustomizer.class);

        ItemStack perItem=shopCusMan.createPersonalMenuItem(pPlayer,this,this.getMenuItem());
        Tellraw itemMsg=new Tellraw(this.mPlugin.getManager(ItemNameManager.class).getDisplayName(perItem));
        itemMsg.getChatStyle().setHoverEvent(HoverEvent.Action.show_item,NMSUtil.getItemTellrawJson(perItem));
        itemMsg.getChatStyle().setClickEvent(ClickEvent.Action.run_command,"/bossshop open "+this.mShop.getShopName());
        return itemMsg;
    }

    /**
     * 玩家购买物品
     * 
     * @param pPlayer
     *            玩家
     * @param pRightClick
     *            是否右击购买
     * @return 购买是否成功
     */
/*    public boolean buyGoods(Player pPlayer,boolean pRightClick){
        Inventory tInv=pPlayer.getOpenInventory().getTopInventory();
        if(!(tInv.getHolder() instanceof BSShopHolder))
            return false;
        return this.buyGoods(pPlayer,(BSShopHolder)tInv.getHolder(),pRightClick);
    }*/
    
    public boolean buyGoods(Player pPlayer,ClickType pTypeClick){
        Inventory tInv=pPlayer.getOpenInventory().getTopInventory();
        if(!(tInv.getHolder() instanceof BSShopHolder))
            return false;
        return this.buyGoods(pPlayer,(BSShopHolder)tInv.getHolder(),pTypeClick);
    }

    /**
     * 玩家购买物品
     * 
     * @param pPlayer
     *            玩家
     * @param pHolder
     *            商店背包的Holder
     * @param pRightClick
     *            是否右击购买
     * @return 购买是否成功
     */
    public boolean buyGoods(Player pPlayer,BSShopHolder pHolder,ClickType pTypeClick){
        if(BSGoods.isClickTooQuick(pPlayer))
            return false;
        int buyCount=1;
        if(pTypeClick == ClickType.RIGHT)
        {
            buyCount=this.getRightClickBuyCount(pPlayer);
        }else if(pTypeClick == ClickType.SHIFT_LEFT && this.mAllowShiftLeftClickBuy)        	
        {
        	//return buyMaxGoods(pPlayer,pHolder);
            if(!this.getAvailableBuy(pPlayer,pHolder))
                return false;
        	do
        	{
    	        //this.stockOut(pPlayer,pHolder,buyCount);
        		buyCount++;
        	}
        	while((this.getAvailableBuyForMaxCount(pPlayer,pHolder,buyCount))>0);
        	buyCount--;
        }
        buyCount=this.getAvailableBuyCount(pPlayer,pHolder,buyCount);
        if(buyCount<=0)
            return false;
        this.stockOut(pPlayer,pHolder,buyCount);
        return true;
    }
/*    
    public boolean buyMaxGoods(Player pPlayer,BSShopHolder pHolder){
    	int buyCount=1;
       // buyCount=this.getAvailableBuyCount(pPlayer,pHolder,buyCount);  	   	
        if(!this.getAvailableBuy(pPlayer,pHolder))
            return false;
    	do
    	{
	        //this.stockOut(pPlayer,pHolder,buyCount);
    		buyCount++;
    	}
    	while((this.getAvailableBuyForMaxCount(pPlayer,pHolder,buyCount))>0);
        //重复检测以防万一 同时刷新购买数量。
        buyCount=this.getAvailableBuyCount(pPlayer,pHolder,buyCount-1);
        if(buyCount<=0)
            return false;
    	this.stockOut(pPlayer,pHolder,buyCount);
    	return true;
    }*/

    /**
     * 检查该用户是否能够购买该物品<br />
     * 如果不能购买,失败原因会立刻在本函数中显示给玩家
     * <p>
     * 所有检查项目包括:<br />
     * 购买事件是否通过<br />
     * 是否是购买自己的物品(只有寄售物品存在这条)<br />
     * 是否有足够的库存<br />
     * 是否已经达到个人购买上限<br />
     * 是否在购买时间<br />
     * 是否有足够的钱<br />
     * 是否已经购买过了(例如权限)<br />
     * 是否购买的物品合法(例如不适合的附魔)<br />
     * </p>
     * 
     * @return 可购买的数量,<0则表示不能购买
     */
    public int getAvailableBuyCount(Player pPlayer,BSShopHolder pHolder,int pBuyCount){
        // 抽奖检查
        if(pHolder instanceof LotteryHolder){
            LotteryHolder lHolder=(LotteryHolder)pHolder;
            LotteryStatus tStatus=lHolder.getStatus();
            if(tStatus==LotteryStatus.WAIT){
                // 此两句多余,因为getShopItem里面已经做过判断
                if(this.getFirstReward().getRewardType()!=RewardType.Lottery)
                    return 0;
                if(this.getWeight()>0)
                    return 0;
            }else if(tStatus==LotteryStatus.RESULT){
            }else return 0;// 其他情况不允许点击抽奖背包
        }
        // 权限检查
        if(!this.hasPermission(pPlayer,true))
            return 0;
        // 是不是购买自己的物品,op排除在外
        if(this.hasOwner()&&!pPlayer.isOp()){
            if(this.mOwner.equals(pPlayer.getUniqueId())){
                Log.send(pPlayer,C("MsgCannotBuyYourselfGoods"));
                return 0;
            }
        }
        // 库存数量是否够用
        if(this.mNumberLimit==0){
            Log.send(pPlayer,C("MsgAlreadySoldOut"));
            return 0;
        }else if(this.mNumberLimit>0){
            pBuyCount=Math.min(pBuyCount,this.mNumberLimit);
        }
        // 是否已经达到个人购买上限
        if(this.mPersonalLimit>-1){
            int alreadyBuyCount=this.mPlugin.getManager(RecordManager.class).getBuyRecord(this.mShop.getShopName(),this.mName,pPlayer.getUniqueId());
            if(alreadyBuyCount>=this.mPersonalLimit){
                Log.send(pPlayer,C("MsgOutOfPersonalLimit"));
                return 0;
            }else pBuyCount=Math.min(pBuyCount,this.mPersonalLimit-alreadyBuyCount);
        }
        // 是否在购买时间
        Date date=new Date();
        if(this.mStartTime!=null&&date.before(this.mStartTime)){
            Log.send(pPlayer,C("MsgGoodsNotArriveTime"));
            return 0;
        }
        if(this.mStopTime!=null&&date.after(this.mStopTime)){
            Log.send(pPlayer,C("MsgGoodsOutOfDate"));
            return 0;
        }
        // 是否已经购买过了
        if(this.alreadyBought(pPlayer)){
            Log.send(pPlayer,C("MsgAlreadyBought"));
            return 0;
        }
        // 购买的物品是否合法
        for(IReward sReward : this.mRewards){
            if(sReward.getRewardType()==RewardType.Enchantment){
                RewardEnchantment tReward=(RewardEnchantment)sReward;
                ItemStack tItem=pPlayer.getItemInHand();
                if((tItem==null||tItem.getType()==Material.AIR)||(!ItemStackChecker.isValidEnchantment(tItem,tReward.getEnchantType(),tReward.getEnchantLevel())&&!this.mPlugin.getConfigManager().allowUnsafeEnchantments())){
                    Log.send(pPlayer,C("MsgEnchantmentInvalid"));
                    return 0;
                }
            }
        }
        // 是否有足够的钱
        if(!this.hasPrice(pPlayer,pBuyCount))
            return 0;
        // 产生购买事件
        BSPlayerPurchaseEvent e1=new BSPlayerPurchaseEvent(pPlayer,this,pBuyCount);// Custom
        Bukkit.getPluginManager().callEvent(e1);
        if(e1.isCancelled())
            return 0;
        return pBuyCount;
    }
    
    /**
     * 为循环购买检测检查该用户是否能够购买该物品,无视数量<br />
     * <p>
     * 所有检查项目包括:<br />
     * 是否是购买自己的物品(只有寄售物品存在这条)<br />
     * 是否在购买时间<br />
     * 是否已经购买过了(例如权限)<br />
     * 是否购买的物品合法(例如不适合的附魔)<br />
     * </p>
     * 
     * @return 是否满足购买条件,无视数量
     */
    public boolean getAvailableBuy(Player pPlayer,BSShopHolder pHolder){
        // 抽奖检查
        if(pHolder instanceof LotteryHolder){
            LotteryHolder lHolder=(LotteryHolder)pHolder;
            LotteryStatus tStatus=lHolder.getStatus();
            if(tStatus==LotteryStatus.WAIT){
                // 此两句多余,因为getShopItem里面已经做过判断
                if(this.getFirstReward().getRewardType()!=RewardType.Lottery)
                    return false;
                if(this.getWeight()>0)
                    return false;
            }else if(tStatus==LotteryStatus.RESULT){
            }else return false;// 其他情况不允许点击抽奖背包
        }
        // 权限检查
        if(!this.hasPermission(pPlayer,true))
            return false;
        // 是不是购买自己的物品,op排除在外
        if(this.hasOwner()&&!pPlayer.isOp()){
            if(this.mOwner.equals(pPlayer.getUniqueId())){
                return false;
            }
        }
        // 是否在购买时间
        Date date=new Date();
        if(this.mStartTime!=null&&date.before(this.mStartTime)){
            return false;
        }
        if(this.mStopTime!=null&&date.after(this.mStopTime)){
            return false;
        }
        // 是否已经购买过了
        if(this.alreadyBought(pPlayer)){
            return false;
        }
        // 购买的物品是否合法
        for(IReward sReward : this.mRewards){
            if(sReward.getRewardType()==RewardType.Enchantment){
                RewardEnchantment tReward=(RewardEnchantment)sReward;
                ItemStack tItem=pPlayer.getItemInHand();
                if((tItem==null||tItem.getType()==Material.AIR)||(!ItemStackChecker.isValidEnchantment(tItem,tReward.getEnchantType(),tReward.getEnchantLevel())&&!this.mPlugin.getConfigManager().allowUnsafeEnchantments())){
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * 用于循环检查该用户是否能够购买最大数量物品<br />
     * 用于getAvailableBuyCount 为了简化减少了重复检测项目。
     * 删除了log显示
     * <p>
     * 所有检查项目包括:<br />
     * 购买事件是否通过<br />
     * 是否已经达到个人购买上限<br />
     * 是否有足够的钱<br />
     * </p>
     * 
     * @return 可购买的数量,<0或=0则表示不能购买
     */
    public int getAvailableBuyForMaxCount(Player pPlayer,BSShopHolder pHolder,int pBuyCount){
        // 库存数量是否够用
        if(this.mNumberLimit==0){
            return 0;
        }else if(this.mNumberLimit>0){
            pBuyCount=Math.min(pBuyCount,this.mNumberLimit);
        }
        // 是否已经达到个人购买上限
        if(this.mPersonalLimit>-1){
            int alreadyBuyCount=this.mPlugin.getManager(RecordManager.class).getBuyRecord(this.mShop.getShopName(),this.mName,pPlayer.getUniqueId());
            if(alreadyBuyCount>=this.mPersonalLimit){
                return 0;
            }else pBuyCount=Math.min(pBuyCount,this.mPersonalLimit-alreadyBuyCount);
        }
        // 是否有足够的钱
        if(!this.hasPriceForMax(pPlayer,pBuyCount))
            return 0;
        // 背包格子满了，避免炸服
        if(pPlayer.getInventory().firstEmpty()==-1){ 
        	return 0;
        }
        return pBuyCount;
    }
    
    /**
     * 商品出货一件,此处进行相关库存管理工作,寄售的工作
     * <p>
     * 所有的工作项目包括:<br />
     * 库存的调整<br />
     * 个人购买数量的记录<br />
     * 价格的收取<br />
     * 商品的发放<br />
     * 购买消息的发送<br />
     * 寄售物品报酬的给予<br />
     * </p>
     * 
     * @param pPlayer
     *            出货的对象
     */
    public void stockOut(Player pPlayer,BSShopHolder pHolder,int pBuyCount){
        // 收取价格
        this.takePrice(pPlayer,pBuyCount);
        // 给予商品
        if(pHolder instanceof LotteryHolder){
            LotteryHolder tHoler=(LotteryHolder)pHolder;
            if(this.getFirstReward().getRewardType()==RewardType.Lottery){
                // 如果是乐透商品,则开始抽奖
                tHoler.startLottery();
            }else{
                // 如果是普通商品,在获取商品后,关闭结果界面并回到WAIT状态
                tHoler.resetLottery();
            }
        }
        // 如果存在数量限制,库存减1
        if(this.hasLimit()){
            if(this.mNumberLimit==0)
                throw new IllegalStateException("库存为0时,不能进行出货函数的调用,这可能是程序bug,请通知作者");
            this.mNumberLimit-=pBuyCount;
            if(this.hasOwner()&&this.mNumberLimit==0){
                this.mShop.removeItem(this.mName,true,true);
                this.mShop.trySortGoods();
            }else{
                CommentedSection sec=getConfigurationSection();
                if(sec!=null){
                    sec.set("NumberLimit",this.getLimit());
                    this.mShop.saveConfig(null);
                }
                this.mShop.updateAllInventory(false);
            }
        }else{
            mShop.updateInventory(pHolder.getInventory(),pPlayer,false);
        }
        // 如果存在个人购买数量限制,增加购买记录到文件
        if(this.hasPersonalLimit()){
            this.mPlugin.getManager(RecordManager.class).addBuyRecord(this.mShop.getShopName(),this.mName,pPlayer.getUniqueId(),pBuyCount);
        }
        // 购买后消息的生成
        if(StringUtil.isNotEmpty(this.mMsg)){
            String tMsg=BossShop.replaceParam(pPlayer,this.mMsg);
            tMsg=this.transformMessage(tMsg,pBuyCount);
            Log.send(pPlayer,tMsg);
        }
        // 购买记录的记录(非正式)
        this.mPlugin.getManager(TransactionLog.class).addTransaction(pPlayer,this,pBuyCount);
        // 最后再给奖励防止出错导致无限刷物品
        this.giveReward(pPlayer,pBuyCount);
        // 给予寄售者的寄售的利润
        if(this.hasOwner())
            this.mPlugin.getManager(SaleManager.class).giveSaleReward(this,pBuyCount);
        // 事件产生
        BSPlayerPurchasedEvent e2=new BSPlayerPurchasedEvent(pPlayer,this,pBuyCount);// Custom
        Bukkit.getPluginManager().callEvent(e2);// Custom Event end
    }

    /**
     * 玩家是否满足显示该商品的条件
     * <p>
     * 如果商品是寄售商品,且已经卖完或或者不在售卖时间,将会调用{@link BSShop #removeItem(String, boolean)}
     * </p>
     * 
     * @param pPlayer
     *            玩家
     * @return 是否显示
     */
    public boolean disaplyToPlayer(Player pPlayer){
        if(!this.hasPermission(pPlayer,false)&&this.mPlugin.getConfigManager().hideNoPermitItem())
            return false;
        if(this.mNumberLimit==0){
            if(this.hasOwner()){
                this.mShop.removeItem(this.getName(),true,false); // 防止无意义的递归
                return false;
            }
            if(this.isHideNoStock())
                return false;
        }
        if(!this.isBuyTime()){
            if(this.hasOwner()){
                this.mShop.removeItem(this.getName(),true,false); // 防止无意义的递归
                String tMsg=BossShop.replaceParam(pPlayer,C("MsgFromOutDateSale"));
                this.mPlugin.getManager(MailManager.class).addMail(this,tMsg);
                return false;
            }
            if(this.isHideNotTime())
                return false;
        }
        return true;
    }

    /**
     * 获取BSBuy库存
     * 
     * @return
     */
    public int getLimit(){
        return this.mNumberLimit;
    }

    public boolean hasLimit(){
        return this.mNumberLimit>-1;
    }

    /**
     * 是否有足够的需求
     * 
     * @param pPlayer
     *            玩家
     * @param pMulCount
     *            倍数
     */
    public boolean hasPrice(Player pPlayer,int pMulCount){
        MultiplierHandler multipMan=this.mPlugin.getManager(MultiplierHandler.class);

        for(IPrice sPrice : this.getPrices()){
            double disCount=1D;
            if(!this.hasOwner())
                disCount=multipMan.calculateWithMultiplier(pPlayer,sPrice.getPriceType());
            if(!sPrice.hasPrice(pPlayer,pMulCount*disCount))
                return false;
        }
        return true;
    }

    /**
     * 循环检测是否有足够的需求
     * 
     * @param pPlayer
     *            玩家
     * @param pMulCount
     *            倍数
     */
    public boolean hasPriceForMax(Player pPlayer,int pMulCount){
        MultiplierHandler multipMan=this.mPlugin.getManager(MultiplierHandler.class);

        for(IPrice sPrice : this.getPrices()){
            double disCount=1D;
            if(!this.hasOwner())
                disCount=multipMan.calculateWithMultiplier(pPlayer,sPrice.getPriceType());
            switch(sPrice.getPriceType()) {
            	case Money:
            		double tPrice=((double)sPrice.getPrice())*pMulCount*disCount;
            		//sPrice.getPlugin().getManager(WorthHandler.class).hasMoney(pPlayer,tPrice);            		            		
                    EconomyHook<?> ecoHook=this.mPlugin.getManager(VaultHook.class).getEconomy();
                    if((!ecoHook.hasAccount(pPlayer.getName())) || (ecoHook.getBalance(pPlayer.getName())<tPrice)){
                        return false;
                    }
                    return true;
            	case Item:
                    ArrayList<PriceInfo> tMulPrice=new ArrayList<>();
                    for(PriceInfo sInfo : ((PriceItem)sPrice).getPrice()){
                        sInfo=sInfo.clone();
                        sInfo.mAmount=(int)Math.rint(sInfo.mAmount*pMulCount);
                        sInfo.mPriceItem=WorthHandler.transformRewardItem(sInfo.mPriceItem,pPlayer);
                        tMulPrice.add(sInfo);
                    }                 
                    for(PriceInfo sItem : tMulPrice){
                        if(!ItemStackChecker.inventoryContainsItem(pPlayer,sItem)){
                            return false;
                        }
                    }
                    return true;                   
            	case Points:
                    int tPrice1=(int)Math.rint(((int)sPrice.getPrice())*pMulCount*disCount);                   
                    if(this.mPlugin.getManager(PointsManager.class).getPoints(pPlayer)<tPrice1){                       
                        return false;
                    }
                    return true;
            	case Exp:
            		int tPrice2=(int)Math.rint(((int)sPrice.getPrice())*pMulCount*disCount);   
        	        if((pPlayer.getLevel()<(Integer)tPrice2)){
        	            return false;
        	        }
        	        return true; 
            	default: 
            		return false;
            }           
        }
        return false;
    }
    
    /**
     * 拿走相应的需求
     * 
     * @param pPlayer
     *            玩家
     * @param pMulCount
     *            倍数
     * @return 剩余数量或null
     */
    public void takePrice(Player pPlayer,int pMulCount){
        MultiplierHandler multipMan=this.mPlugin.getManager(MultiplierHandler.class);

        for(IPrice sPrice : this.getPrices()){
            double disCount=1D;
            if(!this.hasOwner())
                disCount=multipMan.calculateWithMultiplier(pPlayer,sPrice.getPriceType());
            sPrice.takePrice(pPlayer,pMulCount*disCount);
        }
    }

    public void giveReward(Player pPlayer,int pMulCount){
        for(IReward sReward : this.mRewards){
            sReward.giveReward(pPlayer,pMulCount);
        }
    }

    /**
     * 翻译BSBuy物品的属性
     */
    public String transformMessage(String pMsg,int pBuyCount){
        if(StringUtil.isEmpty(pMsg))
            return "";

        StringBuilder tSBuilder=new StringBuilder();
        if(pMsg.indexOf("%price%")!=-1){
            this.getPrices().forEach((sPrice)->tSBuilder.append(sPrice.getDescription(pBuyCount)).append(','));
            if(tSBuilder.length()>1) tSBuilder.setLength(tSBuilder.length()-1);
            pMsg=pMsg.replace("%price%",tSBuilder.toString());
        }

        if(pMsg.indexOf("%reward%")!=-1){
            tSBuilder.setLength(0);
            this.getRewards().forEach((sReward)->tSBuilder.append(sReward.getDescription(pBuyCount)).append(','));
            if(tSBuilder.length()>1) tSBuilder.setLength(tSBuilder.length()-1);
            pMsg=pMsg.replace("%reward%",tSBuilder.toString());
        }

        return pMsg;
    }

    @Deprecated
    public void setInventoryLocation(int i){
        mLocation=i;
    }

    public void setNeedEdit(boolean enable){
        this.mNeedEdit=enable;
    }

    public boolean isNeedEdit(){
        return this.mNeedEdit;
    }

}
