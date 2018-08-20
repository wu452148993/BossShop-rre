package cc.bukkitPlugin.bossshop.goods.price;

import java.util.HashMap;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums;
import org.black_ixx.bossshop.core.BSEnums.PriceType;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.manager.AManager;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.util.CollUtil;

public class PriceManager extends AManager<BossShop>{

    private final HashMap<PriceType,IPrice> mPrices=new HashMap<>();

    public PriceManager(BossShop pPlugin){
        super(pPlugin);
        this.registerPrice(new PriceMoney());
        this.registerPrice(new PricePoints());
        this.registerPrice(new PriceItem());
        this.registerPrice(new PriceExp());
        this.registerPrice(new PriceFree());
        this.registerPrice(new PriceNothing());
    }

    public boolean isPriceSection(CommentedSection pSec){
        return pSec.get("PriceType")!=null||pSec.get("Price")!=null;
    }

    /**
     * 获取指定价格类型的实例
     * 
     * @param pBuyType
     *            价格类型字符串
     * @param pObj
     *            价格Object主体
     * @return 创建的价格,如果创建失败,则返回null
     */
    public IPrice createPrice(CommentedSection pGoodsSec,String name){
        PriceType tPriceType=null;
        String priceType=pGoodsSec.getString("PriceType");
        if(priceType==null){
            Log.severe("无法创建商品["+name+"],未设置商品价格类型");
            return null;
        }else{
            if((tPriceType=BSEnums.getPriceType(priceType))==null){
                Log.severe("无法创建商品["+name+"],["+priceType+"]不是受支持的价格类型");
                Log.severe("支持的价格类型有: "+CollUtil.asList(PriceType.values()));
                return null;
            }
        }

        return this.createPrice(tPriceType,pGoodsSec.get("Price"));
    }

    /**
     * 获取指定价格类型的实例
     * 
     * @param pPriceType
     *            价格类型字符串
     * @param pObjPrice
     *            价格Object主体
     * @return 创建的价格,如果创建失败,则返回null
     */
    public IPrice createPrice(PriceType pPriceType,Object pObjPrice){
        IPrice tPriceType=this.mPrices.get(pPriceType);
        if(tPriceType==null){
            Log.severe("价格类型为"+pPriceType.getClass().getSimpleName()+"的类未注册模块");
            return null;
        }
        if(!tPriceType.initPrice(this.mPlugin,pObjPrice))
            return null;
        else return tPriceType.copy();
    }

    protected void registerPrice(IPrice pPriceInstance){
        if(pPriceInstance==null)
            throw new IllegalArgumentException("注册价格种类时,价格实例不能为null");
        IPrice registered=this.mPrices.put(pPriceInstance.getPriceType(),pPriceInstance);
        if(registered!=null&&registered.getClass()!=pPriceInstance.getClass()){
            Log.severe("价格类["+pPriceInstance.getClass().getSimpleName()+"]与["+registered.getClass().getSimpleName()+"]使用了相同的注册标签");
        }
    }

}
