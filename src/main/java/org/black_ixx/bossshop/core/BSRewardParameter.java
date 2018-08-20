package org.black_ixx.bossshop.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import cc.bukkitPlugin.bossshop.goods.price.IPrice;
import cc.bukkitPlugin.bossshop.goods.reward.IReward;

public class BSRewardParameter{
    /**商品数量限制*/
    public int numberLimit=-1;
    /**商品个人数量限制*/
    public int personalLimit=-1;
    /**商品位置<br />此位置为数组位置*/
    public int location;
    /**商品权重*/
    public int weight=0;
    /**商品属性隐藏值*/
    public int hideItemFlag=0;
    /**商品是否在无库存是隐藏*/
    public boolean hideNoStock=false;
    /**商品是否在不是售卖的时间时隐藏*/
    public boolean hideNotTime=false;
    /**购买商品需要的权限*/
    public String permission;
    /**商品机读名*/
    public String name;
    /**购买商品后的消息*/
    public String msg="undefine";
    /**商品所有者*/
    public UUID owner=null;
    /**商品奖励*/
    public List<IReward> reward=new ArrayList<>();
    /**商品价格*/
    public List<IPrice> price=new ArrayList<>();
    /**商品开始售卖的时间<br />如果为null,则无限制*/
    public Date stopTime=null;
    /**商品结束售卖的时间<br />如果为null,则无限制*/
    public Date startTime=null;
    /**商品所在商店*/
    public BSShop shop=null;
    
    public int rightClickBuyCount=1;
    
    public boolean allowChangeRightClickBuyCount=false;
    
    public boolean allowShiftLeftClickBuy=false;
    
    public boolean showRightClickBuyCount=true;

}
