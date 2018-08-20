package cc.bukkitPlugin.bossshop.sale;

import java.util.ArrayList;

import org.black_ixx.bossshop.core.BSEnums.RewardType;
import org.black_ixx.bossshop.core.BSShop;
import org.bukkit.entity.Player;

class SaleParameter{
    /**当前寄售步奏*/
    public SaleStep mStep;
    /**寄售物品的编号*/
    public String mID;
    /**所有者*/
    public Player owner;
    /**寄售物品类型*/
    public RewardType saleType;
    /**价格类型*/
    public RewardType priceType;
    /**价格*/
    public int price;
    /**单份售卖数量*/
    public int singleNumb=0;
    /**多少份*/
    public int partNumb=0;
    /**错误消息,如果无错误,此值为空*/
    public String errorInfo=null;
    /**寄售的商店,如果为空说明无位置可用*/
    public BSShop shop=null;
    /**商品放置位置,从1开始到81-2,剩余两个格子用作导航*/
    public int location=1;
    /**寄售物品显示的名字*/
    public String mDisplayName;
    /**寄售物品的lore*/
    public ArrayList<String> mLore;
    
    public SaleParameter(){
        this.mStep=SaleStep.GoodsType;
    }
    
}
