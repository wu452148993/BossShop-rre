package cc.bukkitPlugin.bossshop.goods.price;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.PriceType;
import org.black_ixx.bossshop.managers.WorthHandler;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;


public class PriceMoney implements IPrice,IRewardAble{

    private double mPrice=0;
    /**该变量只在调用initPrice才会初始化*/
    private  BossShop mPlugin;

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
        double tPrice=this.mPrice*pMulCount;
        return this.mPlugin.getManager(WorthHandler.class).hasMoney(pPlayer,tPrice);
    }

    @Override
    public String takePrice(Player pPlayer,double pMulCount){
        double tPrice=this.mPrice*pMulCount;
        return this.mPlugin.getManager(WorthHandler.class).takeMoney(pPlayer,tPrice);
    }

    @Override
    public void giveReward(OfflinePlayer pPlayer,double pMulCount){
        double tPrice=this.mPrice*pMulCount;
        this.mPlugin.getManager(WorthHandler.class).giveRewardMoney(pPlayer,tPrice);
    }

    @Override
    public boolean initPrice(BossShop pPlugin,Object pObjPrice){
        this.mPlugin=pPlugin;
        if(pObjPrice==null){
            Log.severe("未配置价格内容,价格["+this.mPlugin.C(this.getPriceType().getNameKey())+"]的内容时必须的");
            return false;
        }
        double tPrice=0D;
        try{
            tPrice=Double.parseDouble(pObjPrice.toString());
        }catch(Exception exc){
            Log.severe("价格类型为金币时,价格必须为数字(Money)");
            return false;
        }
        this.mPrice=tPrice;
        return true;
    }

    @Override
    public PriceType getPriceType(){
        return PriceType.Money;
    }

    @Override
    public PriceMoney copy(){
        PriceMoney tPrice=new PriceMoney();
        tPrice.mPlugin=this.mPlugin;
        tPrice.mPrice=this.mPrice;
        return tPrice;
    }

    @Override
    public String getDescription(double pMulCount){
        String tMoney=this.mPrice*pMulCount+"";
        if(tMoney.indexOf('.')!=-1&&tMoney.charAt(tMoney.length()-1)!='0'){
            int pos=tMoney.length()-1;
            while(pos>0&&tMoney.charAt(pos)=='0'){
                pos--;
            }
            if(pos>0&&tMoney.charAt(pos)=='.')
                pos--;
            tMoney=tMoney.substring(0,pos+1);
        }
        return tMoney+" "+this.mPlugin.C("WordMoney");
    }

}
