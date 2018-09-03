package cc.bukkitPlugin.bossshop.sale;

import java.io.File;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums;
import org.black_ixx.bossshop.core.BSEnums.RewardType;
import org.black_ixx.bossshop.core.BSGoods;
import org.black_ixx.bossshop.core.BSShop;
import org.black_ixx.bossshop.core.BSShopManager;
import org.black_ixx.bossshop.managers.DefaultCreator;
import org.black_ixx.bossshop.managers.ItemNameManager;
import org.black_ixx.bossshop.managers.WorthHandler;
import org.black_ixx.bossshop.points.PointsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.wulf.xmaterial.IMaterial;
import com.github.wulf.xmaterial.XMaterial;

import cc.bukkitPlugin.bossshop.goods.price.IPrice;
import cc.bukkitPlugin.bossshop.goods.price.PriceMoney;
import cc.bukkitPlugin.bossshop.goods.price.PricePoints;
import cc.bukkitPlugin.bossshop.mail.MailManager;
import cc.bukkitPlugin.bossshop.util.ItemLoader;
import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTSerializer;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.exception.NBTSerializeException;
import cc.bukkitPlugin.commons.plugin.manager.AManager;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.IConfigModel;
import cc.bukkitPlugin.commons.tellraw.Color;
import cc.bukkitPlugin.commons.tellraw.Tellraw;
import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.commentedyaml.CommentedYamlConfig;
import cc.commons.util.ByteUtil;
import cc.commons.util.StringUtil;
import lombok.Getter;

/**
 * 玩家寄售系统
 * 
 * @author 聪聪
 */
public class SaleManager extends AManager<BossShop> implements IConfigModel{

    private static final Pattern PATTERN=Pattern.compile("[%]([^%]+)[%]");
    private final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final static BigInteger mMaxInt=new BigInteger(Integer.MAX_VALUE+"");

    private int mSaleTime=86400;
    private int mPerOneSaleLimit=1;
    @Getter
    private ItemStack moneyItem=new ItemStack(Material.EMERALD);
    @Getter
    private ItemStack pointsItem=new ItemStack(Material.DIAMOND);
    private int mShopMaxNumb=2;
    private int mShopSize=54;
    private int mMoneyPoundage=5;
    private int mPointsPoundage=5;
    private int mUnsaleCost=1000;
    private int mUnsaleCount=3;
    private boolean mSortSaleShop=true;
    private String mShopBaseName="SYSTEM_SALE";
    private String mSaleItemName="&2寄售: %bs_sale_item_sigle_count% %bs_sale_item_name%";
    private final HashMap<Material,HashSet<Integer>> mSaleBlackList=new HashMap<>();
    private final HashMap<String,Integer> mSaleGroupLimit=new HashMap<>();
    private final ArrayList<Pattern> mSaleBlackLoreList=new ArrayList<>();
    private final ArrayList<String> mSaleItemLore=new ArrayList<>();

    public SaleManager(BossShop pPlugin){
        super(pPlugin);
        this.mSaleItemLore.add("&2寄售编号: %bs_sale_item_id%");
        this.mSaleItemLore.add("&3寄售者: %bs_sale_owner_name%");
        this.mSaleItemLore.add("&c价格: %bs_sale_price% %bs_sale_price_type%");
        this.mPlugin.getConfigManager().registerConfigModel(this);
    }

    @Override
    public void setConfig(CommandSender pSender,CommentedYamlConfig pConfig){
        //配置文件
        CommentedYamlConfig tConfig=this.mPlugin.getConfigManager().getConfig();
        CommentedSection sec=tConfig.getSection("SaleSystem");
        if(sec==null)
            sec=tConfig.createSection("SaleSystem");
        this.mShopBaseName=sec.getString("ShopBaseName");

        this.moneyItem=ItemLoader.loadItem("MoneyItem",sec.get("MoneyItem"),this.moneyItem);
        this.pointsItem=ItemLoader.loadItem("PointsItem",sec.get("PointsItem"),this.pointsItem);

        this.mSaleTime=sec.getInt("SaleTime",this.mSaleTime);
        this.mPerOneSaleLimit=sec.getInt("PerOneSaleLimit",this.mPerOneSaleLimit);
        this.mShopSize=sec.getInt("ShopSize",this.mShopSize);
        this.mShopMaxNumb=sec.getInt("ShopMaxNumb",this.mShopMaxNumb);
        this.mMoneyPoundage=sec.getInt("MoneyPoundage",this.mMoneyPoundage);
        this.mPointsPoundage=sec.getInt("PointsPoundage",this.mPointsPoundage);
        this.mUnsaleCount=sec.getInt("UnsaleCount",this.mUnsaleCount);
        this.mUnsaleCost=sec.getInt("UnsaleCost",this.mUnsaleCost);
        this.mSaleItemName=sec.getString("SaleItemName",this.mSaleItemName);
        this.mSortSaleShop=sec.getBoolean("SortSaleShop",this.mSortSaleShop);
        List<String> list=sec.getStringList("SaleItemLore");
        if(list!=null&&!list.isEmpty()){
            this.mSaleItemLore.clear();
            this.mSaleItemLore.addAll(list);
        }
        //权限组物品寄售上限
        this.mSaleGroupLimit.clear();
        CommentedSection tGroupSaleLimitSec=sec.getSection("SaleLimitGroup");
        if(tGroupSaleLimitSec!=null){
            for(String sKey : tGroupSaleLimitSec.getKeys(false)){
                this.mSaleGroupLimit.put(sKey,tGroupSaleLimitSec.getInt(sKey,0));
            }
        }
        //寄售物品黑名单
        this.transformBlackListItem(sec.getStringList("SaleItemBlackList"));
        //寄售物品Lore黑名单
        this.mSaleBlackLoreList.clear();
        list=sec.getStringList("SaleItemBlackLoreList");
        if(list!=null&&!list.isEmpty())
            for(String sLore : list){
                if(StringUtil.isEmpty(sLore))
                    continue;
                this.mSaleBlackLoreList.add(Pattern.compile(sLore));
            }
        this.printBlackList(pSender);
    }

    private void printBlackList(CommandSender pSender){
        final String tPrefix="§c    - ";
        Log.info(pSender,C("MsgSaleItemBlackList"));
        if(this.mSaleBlackList.isEmpty())
            Log.info(pSender,tPrefix+C("WordNothing"));
        else{
            for(Map.Entry<Material,HashSet<Integer>> sEntry : this.mSaleBlackList.entrySet()){
                String sLine=sEntry.getKey().name()+": ";
                for(Integer sDamage : sEntry.getValue())
                    sLine+=sDamage+",";
                if(sLine.endsWith(","))
                    sLine=sLine.substring(0,sLine.length()-1);
                Log.info(pSender,tPrefix+sLine);
            }
        }
        Log.info(pSender,C("MsgSaleItemBlackLoreList"));
        if(this.mSaleBlackLoreList.isEmpty())
            Log.info(pSender,tPrefix+C("WordNothing"));
        else{
            for(Pattern sLore : this.mSaleBlackLoreList){
                Log.info(pSender,tPrefix+sLore.pattern());
            }
        }
    }

    /**
     * 转换字符串数组为物品
     */
    private void transformBlackListItem(Collection<String> pItemLines){
        this.mSaleBlackList.clear();
        if(pItemLines==null||pItemLines.isEmpty())
            return;
        int i=0;
        for(String sLineItem : pItemLines){
            i++;
            
            String[] itemInfo=sLineItem.split("@",2);
            if(itemInfo.length==0){
                Log.severe("错误的禁止寄售物品["+sLineItem+"],行数"+i);
                continue;
            }
            String mattype = itemInfo[0];
            String dur = "-1";
            if(itemInfo.length==2){           	
            	dur = itemInfo[1];
            }           
            Material tMate;
            if(itemInfo[0].matches("[\\d]+")){
                int tItemId;
                try{
                    tItemId=Integer.parseInt(itemInfo[0]);
                }catch(NumberFormatException nfexp){
                    Log.severe("错误的禁止寄售物品["+sLineItem+"],行数"+i+","+itemInfo[0]+"不是正确的int数字");
                    continue;
                }
                if(tItemId<0){
                    Log.severe("错误的禁止寄售物品["+sLineItem+"],行数"+i+","+itemInfo[0]+"应该大于等于0");
                    continue;
                }
                if(!XMaterial.isDamageable(IMaterial.fromID(tItemId+"")))
                {
                	tMate=IMaterial.fromID(sLineItem);   
                	dur = "-1";
                }else
                {
                	tMate=IMaterial.fromID(mattype); 
                }            
            }else
            {
            	if(!XMaterial.isDamageable(XMaterial.XfromString(mattype.toUpperCase())))
                {
            		tMate=XMaterial.fromString(sLineItem);
            		dur = "-1";
                }else
            	{
                	tMate=XMaterial.fromString(itemInfo[0].toUpperCase());
            	}          	
            }
            if(tMate==null){
                Log.severe("错误的禁止寄售物品["+sLineItem+"],行数"+i+",不存在名字或id为["+itemInfo[0]+"]的物品");
                continue;
            }
            int tChildItemId=-1;
            if(itemInfo.length==2 && !dur.equals("-1")){
                try{
                    tChildItemId=Integer.parseInt(dur);
                }catch(NumberFormatException nfexp){
                    Log.severe("错误的禁止寄售物品["+sLineItem+"],行数"+i+","+itemInfo[1]+"不是正确的int数字");
                    continue;
                }
                if(tChildItemId<-1){
                    Log.severe("错误的禁止寄售物品["+sLineItem+"],行数"+i+","+itemInfo[1]+"应该大于等于-1");
                    continue;
                }
            }
            
            /*
            Material tMate;
            if(itemInfo[0].matches("[\\d]+")){
                int tItemId;
                try{
                    tItemId=Integer.parseInt(itemInfo[0]);
                }catch(NumberFormatException nfexp){
                    Log.severe("错误的禁止寄售物品["+sLineItem+"],行数"+i+","+itemInfo[0]+"不是正确的int数字");
                    continue;
                }
                if(tItemId<0){
                    Log.severe("错误的禁止寄售物品["+sLineItem+"],行数"+i+","+itemInfo[0]+"应该大于等于0");
                    continue;
                }
                //tMate=Material.getMaterial(tItemId);
                tMate=IMaterial.fromID(tItemId+"");
            }else{
                //tMate=Material.getMaterial(itemInfo[0].toUpperCase());
            	tMate=XMaterial.fromString(itemInfo[0].toUpperCase()).parseMaterial();
            }
            if(tMate==null){
                Log.severe("错误的禁止寄售物品["+sLineItem+"],行数"+i+",不存在名字或id为["+itemInfo[0]+"]的物品");
                continue;
            }
            int tChildItemId=-1;
            if(itemInfo.length==2){
                try{
                    tChildItemId=Integer.parseInt(itemInfo[1]);
                }catch(NumberFormatException nfexp){
                    Log.severe("错误的禁止寄售物品["+sLineItem+"],行数"+i+","+itemInfo[1]+"不是正确的int数字");
                    continue;
                }
                if(tChildItemId<-1){
                    Log.severe("错误的禁止寄售物品["+sLineItem+"],行数"+i+","+itemInfo[1]+"应该大于等于-1");
                    continue;
                }
            }*/
            HashSet<Integer> itemDamages=this.mSaleBlackList.get(tMate);
            if(itemDamages==null){
                itemDamages=new HashSet<>();
                this.mSaleBlackList.put(tMate,itemDamages);
            }
            if(!itemDamages.add(tChildItemId)){
                Log.severe(String.format("错误的禁止寄售物品[%s],行数%d,配置重复,id:%d,name:%s,damage:%d",sLineItem,i,tMate.getId(),tMate.name(),tChildItemId));
                continue;
            }
        }
    }

    /**
     * 检查物品是否在寄售黑名单中
     */
    public boolean isItemInSaleBlackList(Player pPlayer,ItemStack pItem){
        if(pItem==null)
            return false;
        HashSet<Integer> itemDamages=this.mSaleBlackList.get(pItem.getType());
        if(itemDamages!=null&&(itemDamages.contains(-1)||itemDamages.contains(pItem.getDurability()+0)))
            return true;
        if(!pItem.hasItemMeta())
            return false;

        ItemMeta tMeta=pItem.getItemMeta();
        ArrayList<String> tCheckStrs=new ArrayList<>();
        if(tMeta.hasDisplayName())
            tCheckStrs.add(tMeta.getDisplayName());
        if(tMeta.hasLore())
            tCheckStrs.addAll(tMeta.getLore());
        for(String sLore : tCheckStrs){
            if(sLore.isEmpty())
                continue;
            for(Pattern sBlackLore : this.mSaleBlackLoreList)
                if(sBlackLore.matcher(sLore).find())
                    return true;
        }
        return false;
    }

    /**
     * 给予上架物品出售后的收获,如果所有者在线,通知所有者物品被购买
     * 
     * @param pBuy
     *            售出的物品
     * @param pMulCount
     *            倍数
     */
    public void giveSaleReward(BSGoods pBuy,int pMulCount){
        if(pBuy==null)
            return;
        if(!pBuy.hasOwner())
            return;
        OfflinePlayer player=Bukkit.getOfflinePlayer(pBuy.getOwner());
        if(player==null){
            Log.warn(C("MsgUnableFoundSaleOwner")+"["+pBuy.getOwner()+"]");
            return;
        }
        
        for(IPrice sPrice : pBuy.getPrices()){
            switch(sPrice.getPriceType()){
                case Money:
                    ((PriceMoney)sPrice).giveReward(player,pMulCount*(100-this.mMoneyPoundage)/100D);
                    break;
                case Points:
                    ((PricePoints)sPrice).giveReward(player,pMulCount*(100-this.mPointsPoundage)/100D);
                    break;
                default:
                    Log.severe(C("MsgUnknowSalePriceType")+"["+sPrice.getPriceType().getClass().getName()+"]");
                    break;
            }
        }
        Player onlinePlayer=player.getPlayer();
        if(onlinePlayer!=null){
            Tellraw chatMsg=new Tellraw(BossShop.replaceParam(onlinePlayer,C("MsgYouSaleHasSell").replace("%numb%",pMulCount+"")),Color.green);
            chatMsg.addExtra(pBuy.getPersonalChatMenuItem(onlinePlayer));
            chatMsg.sendToPlayer(onlinePlayer);
            BossShop.soundNotifyPlayer(onlinePlayer);
        }
    }

    private String C(String pNode){
        return this.mPlugin.getLangManager().getNode(pNode);
    }

    public int getShopMaxCount(){
        return this.mShopMaxNumb;
    }

    public String getShopBaseName(){
        return this.mShopBaseName;
    }

    public int getUnsaleCount(){
        return this.mUnsaleCount;
    }

    public int getUnSaleCost(){
        return this.mUnsaleCost;
    }

    public boolean unsaleGoods(BSGoods pReward){
        if(pReward==null)
            return false;
        if(!pReward.hasOwner())
            return false;

        pReward.getShop().removeItem(pReward.getName(),true,true);
        this.mPlugin.getManager(MailManager.class).addMail(pReward,C("MsgFromHandUnsale"));
        return true;
    }

    public BSGoods getSaleItem(String pName){
        for(int i=1;i<this.mShopMaxNumb;i++){
            BSShop tShop=this.mPlugin.getManager(BSShopManager.class).getShop(this.mShopBaseName+i);
            if(tShop==null)
                continue;
            BSGoods tGoods=tShop.getGoods(pName);
            if(tGoods==null)
                continue;
            if(!tGoods.hasOwner())
                continue;
            else return tGoods;
        }
        return null;
    }

    /**
     * 获取所有在寄售物品的编号
     */
    public ArrayList<String> getSaleItemsName(){
        return this.getSaleItemsName(null);
    }

    /**
     * 获取指定玩家所有在寄售物品编号
     * <p>
     * 如果玩家为null,将返回所有在寄售物品编号
     * </p>
     * 
     * @param pPlayer
     *            玩家,可以为null
     */
    public ArrayList<String> getSaleItemsName(Player pPlayer){
        ArrayList<String> allSale=new ArrayList<>();
        for(int i=1;i<this.mShopMaxNumb;i++){
            BSShop tShop=this.mPlugin.getManager(BSShopManager.class).getShop(this.mShopBaseName+i);
            if(tShop==null)
                continue;
            for(BSGoods sGoods : tShop.getAllGoods()){
                if(!sGoods.hasOwner())
                    continue;
                if(pPlayer==null||sGoods.getOwner().equals(pPlayer.getUniqueId()))
                    allSale.add(sGoods.getName());
            }
        }
        return allSale;
    }

    public boolean isSaleShop(BSShop pShop){
        return pShop!=null&&pShop.getShopName().matches(this.mShopBaseName+"\\d+");
    }

    public boolean isSortShop(BSShop pShop){
        return this.mSortSaleShop&&this.isSaleShop(pShop);
    }

    /**
     * 获取玩家寄售数量
     * 
     * @param pPlayer
     *            玩家
     */
    public int getForSaleNumb(Player pPlayer){
        return this.getSaleItemsName(pPlayer).size();
    }

    /**
     * 获取下一个可用的商店,并同时获取商店放置位置
     * <p>
     * 如果shop为null,说明无可用的商店用于上架
     * </p>
     */
    public SaleParameter getAvailableShop(SaleParameter pParam){
        BSShop taregt_shop=null;
        BSShopManager shopMan=this.mPlugin.getManager(BSShopManager.class);
        BSShop frontShop=shopMan.getShop(this.mPlugin.getConfigManager().getMainShop());
        int target_loc=0,i;
        for(i=1;i<=this.mShopMaxNumb;i++){
            String tShopname=this.mShopBaseName+i;
            taregt_shop=shopMan.getShop(tShopname);
            if(taregt_shop!=null){
                target_loc=taregt_shop.getNextAvailableLoc();
                if(target_loc<=0||target_loc>this.mShopSize-2){
                    frontShop=taregt_shop;
                    continue;
                }
                break;
            }else{
                taregt_shop=this.createSaleShop(frontShop,tShopname,i);
                target_loc=1;
                break;
            }
        }
        if(i<=this.mShopMaxNumb){
            pParam.shop=taregt_shop;
            pParam.location=target_loc;
        }
        return pParam;
    }

    /**
     * 创建一个寄售商店
     * 
     * @param pShopname
     *            商店名
     * @param pIndex
     *            寄售商店索引
     * @return 创建好的商店
     */
    private BSShop createSaleShop(BSShop pFrontShop,String pShopname,int pIndex){
        File tFile=new File(this.mPlugin.getDataFolder().getAbsolutePath()+"/shops/"+pShopname+".yml");
        CommentedYamlConfig tConfig=new CommentedYamlConfig();
        tConfig.loadFromFile(tFile);
        String tname=C("WordSaleShop")+" "+pIndex;
        DefaultCreator.setSettings(tConfig,tname,tname);
        //设置返回键
        if(pIndex==1){
            String tMainShop=this.mPlugin.getConfigManager().getMainShop();
            DefaultCreator.setGuidItem(tConfig,this.mShopSize-1,348,tMainShop,"RETURN_TO_FROUNT");
        }else{
            DefaultCreator.setGuidItem(tConfig,this.mShopSize-1,348,this.mShopBaseName+(pIndex-1),"RETURN_TO_FROUNT");
            if(pFrontShop!=null){
                CommentedYamlConfig tFrontConfig=pFrontShop.getConfig();
                DefaultCreator.setGuidItem(tFrontConfig,this.mShopSize,331,pShopname,tname,"GOTO_TO_NEXT");
                pFrontShop.saveConfig(null);
                pFrontShop.reloadShop(null);
            }
        }
        //设置下一个按键
        BSShopManager shopMan=this.mPlugin.getManager(BSShopManager.class);
        if(pIndex!=this.mShopMaxNumb&&shopMan.getShop(this.mShopBaseName+(pIndex+1))!=null)
            DefaultCreator.setGuidItem(tConfig,this.mShopSize,331,this.mShopBaseName+(pIndex+1),"GOTO_TO_NEXT");
        tConfig.saveToFile(tFile);
        BSShop tshop=new BSShop(this.mPlugin,pShopname+".yml",pShopname);
        tshop.reloadConfig(null);
        shopMan.addShop(tshop);
        return tshop;
    }

    public int getSaleLimit(Player pPlayer){
        if(pPlayer==null)
            return 0;
        if(pPlayer.hasPermission(this.mPlugin.getName()+".sale.unlimited"))
            return Integer.MAX_VALUE;

        int saleLimit=this.mPerOneSaleLimit;
        for(Map.Entry<String,Integer> sGroup : this.mSaleGroupLimit.entrySet()){
            if(!pPlayer.hasPermission(this.mPlugin.getName()+".sale.group."+sGroup.getKey()))
                continue;
            if(sGroup.getValue()>saleLimit)
                saleLimit=sGroup.getValue();
        }
        return saleLimit;
    }

    /**
     * 物品上架接口
     * 
     * @param pPlayer
     *            命令发送人
     * @param pArgs
     *            上架参数,长度必须为5,否则将不报任何错误而返回
     * @return 是否成功上架,错误消息会立刻反馈给玩家
     */
    public boolean sale(Player pPlayer,String[] pArgs){
        if(pArgs.length!=5)
            return false;
        //检查寄售上限
        int tSaleLimit=this.getSaleLimit(pPlayer);
        if(this.getForSaleNumb(pPlayer)>=tSaleLimit)
            return Log.send(pPlayer,C("MsgSaleReachPreOneLimit")+"("+tSaleLimit+")");
        SaleParameter param=new SaleParameter();
        //获取可用的商店
        this.getAvailableShop(param);
        if(param.shop==null)
            return Log.send(pPlayer,C("MsgSaleShopIsFull"));
        //格式化参数
        this.formatSale(param,pPlayer,pArgs);
        if(param.errorInfo!=null)
            return Log.send(pPlayer,ChatColor.RED+param.errorInfo);
        ItemStack itemInHand=pPlayer.getItemInHand();
        if(param.saleType==RewardType.Item){
            if((itemInHand==null||itemInHand.getType()==Material.AIR)){
                return Log.send(pPlayer,C("MsgYouShouldTakeItemInHand"));
            }
            if(this.isItemInSaleBlackList(pPlayer,itemInHand)){
                return Log.send(pPlayer,C("MsgYouCannotSaleThisItem"));
            }
        }
        //是否有足够的资本
        if(!this.enough(param))
            return false;
        //查找可用的物品名字节点
        CommentedSection sectionShop=param.shop.getConfig().getOrCreateSection("shop");
        CommentedSection sectionItem;
        long time=System.currentTimeMillis()/1000;
        String tSecItemName=param.owner.getName()+"_"+time;
        do{
            tSecItemName=param.owner.getName()+"_"+time;
            time++;
        }while(sectionShop.contains(tSecItemName));
        sectionItem=sectionShop.createSection(tSecItemName);
        param.mID=tSecItemName;
        //替换文本变量
        this.replaceInfoParam(param);
        //设置上架物品到文件,并同时收取价格
        //设置菜单物品到上架参数中,用于发送全服公告
        this.setConfigSection(sectionItem,param);
        BSGoods saleGoods=param.shop.loadItem(sectionShop,sectionItem,true);
        param.shop.finishedAddingItems();
        Log.send(pPlayer,C("MsgForSaleSuccessAndTisID")+tSecItemName);
        Log.info(this.mPlugin.C("MsgPlayerSaleItem",new String[]{"%player%","%item%"},param.owner.getName(),saleGoods.getFirstReward().getDescription(param.partNumb)));
        //全服公告
        this.sendSaleAnnoun(param);
        param.shop.saveConfig(null);
        return true;
    }

    /**
     * 发送全服公告
     */
    private void sendSaleAnnoun(SaleParameter param){
        if(!param.owner.hasPermission(this.mPlugin.getName()+".sale.announce.other"))
            return;
        BSGoods saleGoods=param.shop.getGoods(param.location-1);

        Tellraw tExtra=Tellraw.cHead(Log.getMsgPrefix()+" "+BossShop.replaceParam(param.owner,C("MsgPlayerSaleItem")));
        for(Player sPlayer : BukkitUtil.getOnlinePlayers()){
            tExtra.clone().replace("%item%",saleGoods.getPersonalChatMenuItem(sPlayer)).sendToPlayer(sPlayer);
        }
    }

    /**
     * 根据上架参数,添加物品到商店配置文件中,并同时收取价格
     * 
     * @param sec
     *            要设置的节点
     * @param param
     *            上架参数
     */
    private void setConfigSection(CommentedSection sec,SaleParameter param){
        ArrayList<String> item_info=new ArrayList<>();
        String lore_prefix="&2";
        for(String sStr : param.mLore)
            lore_prefix+="#"+sStr;
        item_info.add("name:"+param.mDisplayName);
        item_info.add("lore:"+lore_prefix);
        ItemStack item=null;
        ItemMeta meta=null;
        WorthHandler wHandler=this.mPlugin.getManager(WorthHandler.class);
        switch(param.saleType){
            case Money:
                item=this.getMoneyItem().clone();
                meta=item.getItemMeta();
                wHandler.takeMoney(param.owner,param.singleNumb*param.partNumb);
                meta.setDisplayName(param.singleNumb+" "+C("WordMoney"));
                item_info.add("type:"+this.moneyItem.getType().name());
                item_info.add("amount:1");
                sec.set("MenuItem",item_info);
                sec.set("Reward",param.singleNumb);
                break;
            case Points:
                item=this.getPointsItem().clone();
                meta=item.getItemMeta();
                wHandler.takePoints(param.owner,param.singleNumb*param.partNumb);
                meta.setDisplayName(param.singleNumb+" "+C("WordPoints"));
                item_info.add("type:"+this.pointsItem.getType().name());
                item_info.add("amount:1");
                sec.set("MenuItem",item_info);
                sec.set("Reward",param.singleNumb);
                break;
            case Item:
                item=param.owner.getItemInHand().clone();
                ItemStack itemTake=item.clone();
                itemTake.setAmount(param.singleNumb*param.partNumb);
                ArrayList<ItemStack> listItem=new ArrayList<>();
                listItem.add(itemTake);
                wHandler.takeItems(param.owner,listItem,false);
                
                int lastId = item_info.size()-1;
                String addLore = "";
                if(item.getItemMeta().hasLore() && !item.getItemMeta().getLore().get(0).equals(""))
                {
                	addLore += "#&2描述：#";
                	for(String sStr : item.getItemMeta().getLore())
                	{
                		addLore += "&e" + sStr + "#";
                	}     
                }
                item_info.set(lastId,"lore:"+ addLore +lore_prefix);
               // item_info.set(lastId,addLore + "# #" + item_info.get(lastId));
                
                item_info.add("type:"+item.getType().name());
                item_info.add("durability:"+item.getDurability());
                item_info.add("amount:"+param.singleNumb);
                String tNBTData=null;
                try{
                    tNBTData=ByteUtil.byteToBase64(NBTSerializer.serializeNBTToByte(item));
                }catch(NBTSerializeException exp){
                    Log.severe("序列化寄售物品的NBT时发生了错误",exp);
                }
                if(!StringUtil.isEmpty(tNBTData))
                    item_info.add("rawnbt:"+tNBTData);
                sec.set("MenuItem",item_info);
                item_info=new ArrayList<>();
                if(item.getItemMeta().hasDisplayName())
                    item_info.add("name:"+item.getItemMeta().getDisplayName());
                item_info.add("type:"+item.getType());
                item_info.add("durability:"+item.getDurability());
                item_info.add("amount:"+param.singleNumb);
                if(tNBTData!=null)
                    item_info.add("rawnbt:"+tNBTData);
                sec.set("Reward",item_info);
                break;
            default:
                break;
        }
        if(meta==null) meta=item.getItemMeta();
        List<String> tLores=meta.getLore();
        if(tLores==null)
            tLores=new ArrayList<>();
        tLores.add("&2"+C("WordSaleID")+":"+param.mID);
        Date date=new Date(System.currentTimeMillis()+this.mSaleTime*1000);
        sec.set("TimeLimit.stop",sdf.format(date));
        sec.set("NumberLimit",param.partNumb);
        tLores.add("&2"+C("WordPartNumb")+":"+param.partNumb);
        sec.set("InventoryLocation",param.location);
        sec.set("RewardType",param.saleType.toString());
        sec.set("PriceType",param.priceType.toString());
        sec.set("Price",param.price);
        tLores.add("&2"+C("WordPrice")+":"+param.price+(param.priceType==RewardType.Money?C("WordMoney"):C("WordPoints")));
        sec.set("Message",C("WordYouBought")+"%reward%");
        sec.set("Owner",param.owner.getUniqueId().toString());
        sec.set("OwnerOnlyForLook",param.owner.getName());
        meta.setLore(tLores);
        item.setItemMeta(meta);
    }

    /**
     * 玩家是否有足够的资本上架,如果不足会立刻发送错误消息给玩家
     */
    private boolean enough(SaleParameter param){
        WorthHandler wHandler=this.mPlugin.getManager(WorthHandler.class);
        switch(param.saleType){
            case Money:
                return wHandler.hasMoney(param.owner,param.singleNumb*param.partNumb);
            case Points:
                return wHandler.hasPoints(param.owner,param.singleNumb*param.partNumb);
            case Item:
                ItemStack item=param.owner.getItemInHand().clone();
                item.setAmount(param.singleNumb*param.partNumb);
                ArrayList<ItemStack> items=new ArrayList<>();
                items.add(item);
                return wHandler.hasItems(param.owner,items,false);
            default:
                break;
        }
        return false;
    }

    /**
     * 从命令参数中生成上架物品的参数
     * <p>
     * 如果参数错误,错误消息存储在SaleParameter.errorInfo中, 如果SaleParameter.errorInfo为空说明没有错误
     * </p>
     * 
     * @param pPlayer
     *            玩家
     * @param args
     *            参数
     * @return 上架物品参数
     */
    private boolean formatSale(SaleParameter pParam,Player pPlayer,String[] args){
        pParam.owner=pPlayer;
        if(!this.handleGoodsTypeInput(pParam,pPlayer,args[0]))
            return false;
        //单次的数量
        if(!this.handleSingleNumbInput(pParam,pPlayer,args[1]))
            return false;
        //几份
        if(!this.handlePartNumbInput(pParam,pPlayer,args[2]))
            return false;
        //价格类型
        if(!this.handlePriceTypeInput(pParam,pPlayer,args[3]))
            return false;
        //价格
        if(!this.handlePriceInput(pParam,pPlayer,args[4]))
            return false;
        return true;
    }

    protected boolean handleGoodsTypeInput(SaleParameter pParam,Player pPlayer,String pInput){
        pParam.saleType=BSEnums.getRewardType(pInput);
        if(pParam.saleType==null){
            pParam.errorInfo=C("MsgUnknowSaleItemType")+"["+pInput+"]";
            return false;
        }
        if(pParam.saleType!=RewardType.Item&&pParam.saleType!=RewardType.Money&&pParam.saleType!=RewardType.Points){
            pParam.errorInfo=C("MsgUnsupportSaleItemType")+"["+pParam.saleType.toString()+"]";
            return false;
        }
        if(!pPlayer.hasPermission(this.mPlugin.getName()+".sale.type."+pParam.saleType.name().toLowerCase())){
            pParam.errorInfo=C("MsgYouCannotSaleThisType").replace("%type%",C(pParam.saleType.getNameKey()));
            return false;
        }
        return true;
    }

    protected boolean handleSingleNumbInput(SaleParameter pParam,Player pPlayer,String pInput){
        try{
            pParam.singleNumb=Integer.parseInt(pInput);
        }catch(NumberFormatException nfexp){
            pParam.errorInfo=ChatColor.RED+C("WordSigleNumb")+" "+C("MsgMustBeNumb");
            return false;
        }
        if(pParam.singleNumb<=0){
            pParam.errorInfo=ChatColor.RED+C("WordSigleNumb")+" "+C("MsgMustAboveZero");
            return false;
        }
        return true;
    }

    protected boolean handlePartNumbInput(SaleParameter pParam,Player pPlayer,String pInput){
        try{
            pParam.partNumb=Integer.parseInt(pInput);
        }catch(NumberFormatException nfexp){
            pParam.errorInfo=ChatColor.RED+C("WordPartNumb")+" "+C("MsgMustBeNumb");
            return false;
        }
        if(pParam.partNumb<=0){
            pParam.errorInfo=ChatColor.RED+C("WordPartNumb")+" "+C("MsgMustAboveZero");
            return false;
        }
        BigInteger tTotalCount=new BigInteger(pParam.singleNumb+"").multiply(new BigInteger(pParam.partNumb+""));
        if(tTotalCount.compareTo(SaleManager.mMaxInt)>0){
            pParam.errorInfo=ChatColor.RED+C("MsgYouTotalGoodsNumbIsTooBig");
            return false;
        }
        return true;
    }

    /**
     * 处理价格类型输入
     * <p>
     * 处理失败,失败结果会存储在{@link SaleParameter#errorInfo}中<br />
     * 函数内同时会处理寄售类型和价格类型相同的情况
     * </p>
     * 
     * @param pParam
     *            已经处理的参数
     * @param pPlayer
     *            输入者
     * @param pInput
     *            输入字符
     * @return 是否处理成功
     */
    protected boolean handlePriceTypeInput(SaleParameter pParam,Player pPlayer,String pInput){
        pParam.priceType=BSEnums.getRewardType(pInput);
        if(pParam.priceType==null){
            pParam.errorInfo=C("MsgUnknowPriceType")+"["+pInput+"]";
            return false;
        }
        if(pParam.priceType!=RewardType.Money&&pParam.priceType!=RewardType.Points){
            pParam.errorInfo=C("MsgUnsupportPriceType")+"["+pParam.priceType.name()+"]";
            return false;
        }

        if(!pPlayer.hasPermission(this.mPlugin.getName()+".sale.pricetype."+pParam.priceType.name().toLowerCase())){
            pParam.errorInfo=C("MsgYouCannotSaleThisPriceType").replace("%type%",C(pParam.priceType.getNameKey()));
            return false;
        }

        //点券插件是否启用
        if(pParam.priceType==RewardType.Points){
            if(this.mPlugin.getManager(PointsManager.class)==null){
                pParam.errorInfo=C("MsgServerDisablePoints");
                return false;
            }
        }
        //不允许寄售物品和价格类型相同
        if(pParam.priceType.name().equals(pParam.saleType.name())){
            pParam.errorInfo=C("MsgSaleItemShouldNotSameWithPrice");
            return false;
        }
        return true;
    }

    protected boolean handlePriceInput(SaleParameter pParam,Player pPlayer,String pInput){
        try{
            pParam.price=Integer.parseInt(pInput);
        }catch(NumberFormatException nfexp){
            pParam.errorInfo=ChatColor.RED+"<"+C("WordPrice")+">"+C("MsgMustBeNumb");
            return false;
        }
        if(pParam.price<=0){
            pParam.errorInfo=ChatColor.RED+"<"+C("WordPrice")+">"+C("MsgMustAboveZero");
            return false;
        }
        return true;
    }

    /**
     * 将寄售模板中的字符串变量替换之后放到寄售参数中
     */
    public void replaceInfoParam(SaleParameter pParam){
        pParam.mDisplayName=this.formatString(this.mSaleItemName,pParam);
        pParam.mLore=new ArrayList<>();
        for(String sStr : this.mSaleItemLore)
            pParam.mLore.add(this.formatString(sStr,pParam));
    }

    /**
     * 替换字符串中的变量,所有支持的变量:<br/>
     * %bs_sale_owner_name%<br/>
     * %bs_sale_item_id%<br/>
     * %bs_sale_item_sigle_count%<br/>
     * %bs_sale_item_part_count%<br/>
     * %bs_sale_item_type%<br/>
     * %bs_sale_item_name%<br/>
     * %bs_sale_price%<br/>
     * %bs_sale_price_type%<br/>
     */
    protected String formatString(String pStr,SaleParameter pSaleParam){
        Matcher tMatch=PATTERN.matcher(pStr);
        ItemNameManager nameMan=this.mPlugin.getManager(ItemNameManager.class);
        while(tMatch.find()){
            String tParam=tMatch.group(1);
            String value=tParam;
            if(tParam.equalsIgnoreCase("bs_sale_owner_name")){
                value=pSaleParam.owner.getName();
            }else if(tParam.equalsIgnoreCase("bs_sale_item_id")){
                value=pSaleParam.mID;
            }else if(tParam.equalsIgnoreCase("bs_sale_item_sigle_count")){
                value=pSaleParam.singleNumb+"";
            }else if(tParam.equalsIgnoreCase("bs_sale_item_part_count")){
                value=pSaleParam.partNumb+"";
            }else if(tParam.equalsIgnoreCase("bs_sale_item_type")){
                value=C(pSaleParam.saleType.getNameKey());
            }else if(tParam.equalsIgnoreCase("bs_sale_item_name")){
                String tName;
                if(pSaleParam.saleType==RewardType.Item){
                    ItemStack tItem=pSaleParam.owner.getPlayer().getItemInHand();
                    tName=nameMan.getDisplayName(tItem);
                }else tName=C(pSaleParam.saleType.getNameKey());
                value=tName;
            }else if(tParam.equalsIgnoreCase("bs_sale_price")){
                value=pSaleParam.price+"";
            }else if(tParam.equalsIgnoreCase("bs_sale_price_type")){
                value=C(pSaleParam.priceType.getNameKey());
            }else Log.warn(C("MsgUnsupportSaleItemParam"));
            if(!tParam.equals(value))
                pStr=pStr.replace("%"+tParam+"%",value);
        }
        return pStr;
    }

    @Override
    public void addDefaults(CommentedYamlConfig pConfig){}

}
