package org.black_ixx.bossshop.core;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.managers.DefaultCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.bossshop.nbt.NBTEditManager;
import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.INeedReload;
import cc.bukkitPlugin.commons.plugin.manager.AManager;

public class BSShopManager extends AManager<BossShop> implements INeedReload{

    private long mLastReloadTime=0;
    private File folder;
    private final Map<String,BSShop> mShops=new ConcurrentHashMap<String,BSShop>();

    /**
     * 商店管理器
     * @param pPlugin
     */
    public BSShopManager(BossShop pPlugin){
        super(pPlugin);
        this.folder=new File(pPlugin.getDataFolder().getAbsolutePath()+"/shops/");
        this.mPlugin.registerReloadModel(this);
    }

    /**
     * 重载或载入所有商店
     */
    @Override
    public boolean reloadConfig(CommandSender pSender){
        this.mLastReloadTime=System.currentTimeMillis();
        // 一定要在调用清理函数之前调用时间
        if(!folder.isDirectory()){
            createDefaults();
        }
        Iterator<Map.Entry<String,BSShop>> it=this.mShops.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String,BSShop> entry=it.next();
            BSShop shop=entry.getValue();
            if(!shop.reloadShop(pSender)){
                Log.debug(pSender,this.mPlugin.C("MsgRemoveShopDueToFileNotExist").replace("%shop%",shop.mShopName));
                it.remove();
            }
        }
        for(File file : folder.listFiles()){
            if(file==null||!file.isFile())
                continue;
            String name=file.getName();
            if(name.toLowerCase().endsWith(".yml"))
                this.loadShop(pSender,file);
        }
        Log.info(pSender,this.mPlugin.C("MsgShopLoaded").replace("%numb%",this.mShops.size()+""));

        this.mPlugin.getManager(NBTEditManager.class).clearNBT(pSender);
        this.updataAllInventory(true);
        return true;
    }

    public long getLastReloadTime(){
        return this.mLastReloadTime;
    }

    /**
     * 重载所有由BS插件生成的背包
     * <p>商店配置重载后会自动调用该函数</p>
     */
    public void updataAllInventory(boolean pReset){
        for(BSShop sShop : this.mShops.values()){
            sShop.updateAllInventory(pReset);
        }
    }

    /**
     * 检查根据机读名称(文件名)对应的商店是否存在
     * @param name 商店机读名称,忽略大小写
     * @return 是否存在
     */
    public boolean isShopExist(String name){
        return !(this.getShop(name)==null);
    }

    /**
     * 检查指定的商店是否存在(多用于配置重载后的商店数据检验)
     * @param shop 商店
     * @return 是否存在
     */
    public boolean isShopExist(BSShop shop){
        return this.mShops.containsValue(shop);
    }

    /**
     * 根据商店机读名称(文件名)获取指定的商店,不存在则返回null
     * @param name 商店机读名称,忽略大小写
     * @return 商店
     */
    public BSShop getShop(String name){
        for(Map.Entry<String,BSShop> entry : this.mShops.entrySet()){
            if(entry.getKey().equalsIgnoreCase(name))
                return entry.getValue();
        }
        return null;
    }

    /**
     * 增加一个商店到管理器中
     */
    public void addShop(BSShop pShop){
        this.mShops.put(pShop.getShopName(),pShop);
    }

    public BSShop removeShop(BSShop pShop){
        return this.removeShop(pShop.getShopName());
    }

    /**
     * 根据指定的文件载入所需的商店
     * <p>如果商店已经载入,则重新载入但是商店对象不变,如果商店文件不存在,则去除该商店</p>
     * @param file 要载入商店的文件
     * @return 载入后的商店,自动添加到商店列表
     */
    private BSShop loadShop(CommandSender pSender,File file){
        String fileName=file.getName();
        String shopName=getName(fileName);
        BSShop shop=this.getShop(shopName);
        if(shop!=null)
            return shop;
        else{
            shop=new BSShop(this.mPlugin,fileName,shopName);
            shop.reloadShop(pSender);
            this.mShops.put(shopName,shop);
        }
        return shop;
    }

    /**
     * 根据文件名生成商店名
     * @param filename 文件名
     * @return 去掉后缀的字符串
     */
    private static String getName(String filename){
        int pos=filename.lastIndexOf('.');
        if(pos!=-1)
            return filename.substring(0,pos);
        else return filename;
    }

    /**
     * 根据机读名字移除指定的商店
     * @param name 机读名字
     * @return 被移除的商店,如果不存在则为null
     */
    private BSShop removeShop(String name){
        Iterator<Map.Entry<String,BSShop>> it=this.mShops.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String,BSShop> entry=it.next();
            if(entry.getKey().equalsIgnoreCase(name)){
                it.remove();
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 根据机读名称为玩家打开指定的商店
     * @param player 玩家
     * @param name 商店机读名
     */
    public void openShop(Player player,String name){
        BSShop shop=this.getShop(name);
        if(shop==null){
            Log.send(player,this.mPlugin.getLangManager().getNode("MsgNoShopFound").replace("%shop%",name));
            return;
        }
        this.openShop(player,shop);
    }

    /**
     * 为玩家打开指定的商店
     * @param player 玩家
     * @param shop 商店
     */
    public void openShop(Player player,BSShop shop){
        shop.openInventory(player);
        Log.send(player,shop.getEnterMessage());
    }

    public Map<String,BSShop> getShops(){
        return this.mShops;
    }

    /**
     * 创建默认的商店
     */
    public void createDefaults(){
        new DefaultCreator(this.mPlugin).addAllExamples();
    }
}
