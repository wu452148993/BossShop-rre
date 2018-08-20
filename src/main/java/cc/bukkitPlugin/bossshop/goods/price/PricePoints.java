package cc.bukkitPlugin.bossshop.goods.price;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.PriceType;
import org.black_ixx.bossshop.managers.WorthHandler;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;


public class PricePoints implements IPrice,IRewardAble{

    private int mPrice=0;
    /**该变量只在调用initPrice才会初始化*/
    private  BossShop mPlugin;
    
    @Override
    public BossShop getPlugin(){
        return this.mPlugin;
    }
    
	@Override
	public Integer getPrice() {
		return this.mPrice;
	}
	
    @Override
    public PriceType getPriceType(){
        return PriceType.Points;
    }
    
    @Override
    public void giveReward(OfflinePlayer pPlayer,double pMulCount){
        int tPrice=(int)Math.rint(this.mPrice*pMulCount);
        this.mPlugin.getManager(WorthHandler.class).giveRewardPoints(pPlayer,tPrice);
    }

    @Override
    public boolean hasPrice(Player pPlayer,double pMulCount){
        int tPrice=(int)Math.rint(this.mPrice*pMulCount);
        return this.mPlugin.getManager(WorthHandler.class).hasPoints(pPlayer,tPrice);
    }

    @Override
    public String takePrice(Player pPlayer,double pMulCount){
        int tPrice=(int)Math.rint(this.mPrice*pMulCount);
        return this.mPlugin.getManager(WorthHandler.class).takePoints(pPlayer,tPrice);
    }

    @Override
    public boolean initPrice(BossShop pPlugin,Object pObjPrice){
        this.mPlugin=pPlugin;
        if(pObjPrice==null){
            Log.severe("未配置价格内容,价格["+this.mPlugin.C(this.getPriceType().getNameKey())+"]的内容时必须的");
            return false;
        }
        if(pObjPrice instanceof Integer){
            this.mPrice=((Integer)pObjPrice).intValue();
            return true;
        }
        try{
            this.mPrice=Integer.parseInt(pObjPrice.toString());
        }catch(Exception exp){
            Log.severe("价格类型为点券时,价格必须为数字(Points)");
            return false;
        }
        return true;
    }

    @Override
    public IPrice copy(){
        PricePoints tPrice=new PricePoints();
        tPrice.mPlugin=this.mPlugin;
        tPrice.mPrice=this.mPrice;
        return tPrice;
    }

    @Override
    public String getDescription(double pMulCount){
        return (this.mPrice*pMulCount)+this.mPlugin.C("WordPoints");
    }

}
