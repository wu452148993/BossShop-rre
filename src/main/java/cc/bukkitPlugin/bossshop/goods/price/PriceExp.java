package cc.bukkitPlugin.bossshop.goods.price;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.PriceType;
import org.black_ixx.bossshop.managers.WorthHandler;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;


public class PriceExp implements IPrice{

    private double mPrice=0;
    /**该变量只在调用initPrice才会初始化*/
    private  BossShop mPlugin;
    
    @Override
    public PriceType getPriceType(){
        return PriceType.Exp;
    }
    
    @Override
    public BossShop getPlugin(){
        return this.mPlugin;
    }
    
	@Override
	public Double getPrice() {
		return this.mPrice;
	}
    
    @Override
    public boolean hasPrice(Player pPlayer,double pMulCount){
        int tPrice=(int)Math.rint(this.mPrice*pMulCount);
        return this.mPlugin.getManager(WorthHandler.class).hasExp(pPlayer,tPrice);
    }

    @Override
    public String takePrice(Player pPlayer,double pMulCount){
        int tPrice=(int)Math.rint(this.mPrice*pMulCount);
        return this.mPlugin.getManager(WorthHandler.class).takeExp(pPlayer,tPrice);
    }

    @Override
    public boolean initPrice(BossShop pPlugin,Object pObjPrice){
        this.mPlugin=pPlugin;
        if(pObjPrice==null){
            Log.severe("未配置价格内容,价格["+this.mPlugin.C(this.getPriceType().getNameKey())+"]的内容是必须的");
            return false;
        }
        double tPrice=0D;
        try{
            tPrice=Double.parseDouble(pObjPrice.toString());
        }catch(Exception exc){
            Log.severe("价格类型为经验时,价格必须是数字(Exp)");
            return false;
        }
        this.mPrice=tPrice;
        return true;
    }

    @Override
    public String getDescription(double pMulCount){
        return this.mPrice+this.mPlugin.C("WordExp");
    }

    @Override
    public IPrice copy(){
        PriceExp tPrice=new PriceExp();
        tPrice.mPlugin=this.mPlugin;
        tPrice.mPrice=this.mPrice;
        return tPrice;
    }

}
