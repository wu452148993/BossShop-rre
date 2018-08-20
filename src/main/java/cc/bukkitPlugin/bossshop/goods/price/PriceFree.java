package cc.bukkitPlugin.bossshop.goods.price;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.PriceType;


public class PriceFree extends APriceFree{

    @Override
    public PriceType getPriceType(){
        return PriceType.Free;
    }

    @Override
    public IPrice copy(){
        return new PriceFree();
    }

	@Override
	public BossShop getPlugin() {
		return null;
	}

	@Override
	public Object getPrice() {
		return null;
	}

}
