package cc.bukkitPlugin.bossshop.goods.price;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.PriceType;
import org.bukkit.entity.Player;

public interface IPrice{
    
	//获取Pluging;
	public BossShop getPlugin();
	//获取物品单价
	public Object getPrice();
    /**
     * 获取价格类型
     */
    PriceType getPriceType();
    /**
     * 指定玩家是否有足够的价格
     * <p>所有提示都将会在函数中发出</p>
     * @param pPlayer 玩家
     * @param pMulCount 倍数
     */
    boolean hasPrice(Player pPlayer,double pMulCount);
    
    /**
     * 拿走玩家指定的价格
     * @param pPlayer 玩家
     * @param pMulCount 倍数
     * @return 剩余的数量或null
     */
    String takePrice(Player pPlayer,double pMulCount);
    
    /**
     * 使用指定的Object生成价格实例
     * @param pObjPrice 价格Object主体
     * @return 创建的价格,如果创建失败,则返回null
     */
    boolean initPrice(BossShop pPlugin,Object pObjPrice);
    
    /**
     * 获取价格的描述
     * @param pMulCount 倍数
     * @return 描述
     */
    String getDescription(double pMulCount);
    
    /**
     * 拷贝价格
     */
    IPrice copy();
    
}
