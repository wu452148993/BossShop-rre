package org.black_ixx.bossshop.managers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSRewardParameter;
import org.black_ixx.bossshop.core.BSShop;

import cc.bukkitPlugin.bossshop.goods.price.IPrice;
import cc.bukkitPlugin.bossshop.goods.price.PriceManager;
import cc.bukkitPlugin.bossshop.goods.reward.IReward;
import cc.bukkitPlugin.bossshop.goods.reward.RewardManager;
import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.manager.AManager;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.util.StringUtil;

/**
 * 用于从商品节点上创建菜单物品和回报物品
 * 
 * @author 聪聪
 */
public class BuyItemHandler extends AManager<BossShop>{

    private final SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public BuyItemHandler(BossShop pPlugin){
        super(pPlugin);
    }

    private Date getDate(String str){
        if(str==null)
            return null;
        Date date=null;
        try{
            date=this.sdf.parse(str);
        }catch(ParseException pexp){
        }
        return date;
    }

    public BSRewardParameter createItemParam(BSShop pShop,CommentedSection pSectionGoods){
        String stage="Basic Data";
        String name=pSectionGoods.getName();
        try{
            BSRewardParameter parameter=new BSRewardParameter();
            parameter.name=name;
            parameter.shop=pShop;
            parameter.msg=pSectionGoods.getString("Message");
            if(parameter.weight<0)
                parameter.weight=0;
            String str_uuid=pSectionGoods.getString("Owner");
            if(str_uuid!=null){
                try{
                    parameter.owner=UUID.fromString(str_uuid);
                }catch(Exception exp){
                    Log.severe("配置文件"+pShop.getConfigFilename()+"存在错误, "+str_uuid+" 不是一个合法的UUID");
                }
            }
            parameter.hideNoStock=pSectionGoods.getBoolean("HideNoStock",false);
            parameter.hideNotTime=pSectionGoods.getBoolean("HideNotTime",false);
            //物品属性显示配置
            if(pSectionGoods.contains("HideItemFlag"))
                parameter.hideItemFlag=pSectionGoods.getInt("HideItemFlag");
            else parameter.hideItemFlag=pShop.getHideItemFlag();
            //物品右键购买配置
            if(pSectionGoods.contains("RightClickBuyCount"))
                parameter.rightClickBuyCount=pSectionGoods.getInt("RightClickBuyCount");
            else parameter.rightClickBuyCount=pShop.getRightClickBuyCount();
            if(parameter.rightClickBuyCount<=0){
                parameter.rightClickBuyCount=1;
            }
            if(pSectionGoods.contains("AllowChangeRightClickBuyCount"))
                parameter.allowChangeRightClickBuyCount=pSectionGoods.getBoolean("AllowChangeRightClickBuyCount");
            else parameter.allowChangeRightClickBuyCount=pShop.allowChangeRightClickBuyCount();
            if(pSectionGoods.contains("ShowRightClickBuyCount"))
                parameter.showRightClickBuyCount=pSectionGoods.getBoolean("ShowRightClickBuyCount");
            else parameter.showRightClickBuyCount=pShop.showRightClickBuyCount();
            if(pSectionGoods.contains("AllowShiftLeftClickBuy"))
                parameter.allowShiftLeftClickBuy=pSectionGoods.getBoolean("AllowShiftLeftClickBuy");
            else parameter.allowShiftLeftClickBuy=pShop.allowShiftLeftClickBuy();
            //权限设置
            parameter.permission=pSectionGoods.getString("ExtraPermission");
            parameter.weight=pSectionGoods.getInt("Weight",0);
            if(StringUtil.isEmpty(parameter.permission))
                parameter.permission=null;
            parameter.numberLimit=pSectionGoods.getInt("NumberLimit",-1);
            parameter.personalLimit=pSectionGoods.getInt("PersonalLimit",-1);
            CommentedSection time=pSectionGoods.getSection("TimeLimit");
            if(time!=null){
                parameter.startTime=this.getDate(time.getString("start"));
                parameter.stopTime=this.getDate(time.getString("stop"));
            }
            parameter.location=pSectionGoods.getInt("InventoryLocation")-1;
            if(parameter.location<0){
                parameter.location=0;
                Log.severe("商店物品["+name+"]的背包位置["+parameter.location+"]不正确,已经设置为1号位置");
            }

            stage="获取商品";
            RewardManager tRewardMan=this.mPlugin.getManager(RewardManager.class);
            IReward tReward=null;
            boolean tError=false;
            if(tRewardMan.isRewardSection(pSectionGoods)){
                tReward=tRewardMan.createReward(pSectionGoods,pSectionGoods.getName());
                parameter.reward.add(tReward);
                tError=tReward==null;
            }

            if(!tError){
                CommentedSection tSecRewars=pSectionGoods.getSection("Rewards");
                if(tSecRewars!=null){
                    for(String sKey : tSecRewars.getKeys(false)){
                        CommentedSection tSec=tSecRewars.getSection(sKey);
                        if(tSec==null||!tRewardMan.isRewardSection(tSec)) continue;

                        tError|=(tReward=tRewardMan.createReward(tSec,pSectionGoods.getName()))==null;
                        if(!tError){
                            parameter.reward.add(tReward);
                        }else{
                            break;
                        }
                    }
                }
            }

            if(tError||parameter.reward.isEmpty())
                return this.error("无法创建商品["+name+"],商品奖励配置错误");

            stage="获取价格";
            PriceManager tPriceMan=this.mPlugin.getManager(PriceManager.class);
            IPrice tPrice=null;
            tError=false;
            if(tPriceMan.isPriceSection(pSectionGoods)){
                tPrice=tPriceMan.createPrice(pSectionGoods,pSectionGoods.getName());
                parameter.price.add(tPrice);
                tError=tPrice==null;
            }

            if(!tError){
                CommentedSection tSecPrices=pSectionGoods.getSection("Prices");
                if(tSecPrices!=null){
                    for(String sKey : tSecPrices.getKeys(false)){
                        CommentedSection tSec=tSecPrices.getSection(sKey);
                        if(tSec==null||!tPriceMan.isPriceSection(tSec)) continue;

                        tError|=(tPrice=tPriceMan.createPrice(tSec,pSectionGoods.getName()))==null;
                        if(!tError){
                            parameter.price.add(tPrice);
                        }else{
                            break;
                        }
                    }
                }
            }

            if(tError||parameter.price.isEmpty())
                return this.error("无法创建商品["+name+"],商品价格配置错误");

            return parameter;
        }catch(Throwable exp){
            Log.severe("商品 "+name+"于步骤 '"+stage+"'阶段时创建出错",exp);
            return null;
        }
    }

    private BSRewardParameter error(String pMsg){
        Log.severe(pMsg);
        return null;
    }

}
