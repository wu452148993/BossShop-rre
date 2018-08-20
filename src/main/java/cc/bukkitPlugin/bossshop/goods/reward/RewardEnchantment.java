package cc.bukkitPlugin.bossshop.goods.reward;

import java.util.HashMap;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.RewardType;
import org.black_ixx.bossshop.managers.ItemNameManager;
import org.black_ixx.bossshop.managers.WorthHandler;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import com.github.wulf.xmaterial.IEnchantment;

import cc.bukkitPlugin.commons.Log;

public class RewardEnchantment implements IReward{

    public static final String ENCHANT_TYPE="enchant_type";
    public static final String ENCHANT_LEVEL="enchant_level";

    private Enchantment mEnchantType;
    private int mEnchantLevel;
    /** 该变量只在调用initPrice才会初始化 */
    private BossShop mPlugin;

    
    public Enchantment getEnchantType(){
        return this.mEnchantType;
    }
    
    public int getEnchantLevel(){
        return this.mEnchantLevel;
    }
    
    @Override
    public RewardType getRewardType(){
        return RewardType.Enchantment;
    }

    @Override
    public void giveReward(Player pPlayer,int pMulCount){
        WorthHandler worthMan=this.mPlugin.getManager(WorthHandler.class);
        worthMan.giveRewardEnchantment(pPlayer,this.mEnchantType,this.mEnchantLevel);
    }

    @Override
    public boolean initReward(BossShop pPlugin,Object pObjReward){
        this.mPlugin=pPlugin;
        if(pObjReward==null){
            Log.severe("未配置商品奖励,商品["+this.mPlugin.C(this.getRewardType().getNameKey())+"]的奖励是必须的");
            return false;
        }
        if(!(pObjReward instanceof String)){
            Log.severe("商品类型为附魔时,商品配置必须为String");
            return false;
        }
        String line=(String)pObjReward;
        String parts[]=line.split("#",2);
        if(parts.length!=2){
            Log.severe("错误的附魔格式,正确格式为: <附魔类型/附魔id>#<附魔等级>");
            return false;
        }
        String partName=parts[0].trim();
        String partLevel=parts[1].trim();
        int lvl=0;
        try{
            lvl=Integer.parseInt(partLevel);
        }catch(Exception e){
            Log.severe("错误的附魔等级["+partLevel+"]");
            return false;
        }
        Enchantment enchantment=null;
        try{
            //enchantment=Enchantment.getById(Integer.parseInt(partName));
        	enchantment=IEnchantment.fromID(Integer.parseInt(partName));
        }catch(Exception e){
            enchantment=Enchantment.getByName(partName);
        }
        if(enchantment==null){
            Log.severe("未找到["+partName+"]的附魔");
            return false;
        }
        this.mEnchantType=enchantment;
        this.mEnchantLevel=lvl;
        return true;
    }

    @Override
    public IReward copy(){
        RewardEnchantment tReward=new RewardEnchantment();
        tReward.mPlugin=this.mPlugin;
        tReward.mEnchantType=this.mEnchantType;
        tReward.mEnchantLevel=this.mEnchantLevel;
        return tReward;
    }

    @Override
    public String getDescription(int pMulCount){
        return this.mPlugin.getManager(ItemNameManager.class).getDisplayName(this.mEnchantType,this.mEnchantLevel);
    }

    @Override
    public HashMap<String,Object> getReward(int pMulCount){
        HashMap<String,Object> t=new HashMap<>();
        t.put(ENCHANT_TYPE,this.mEnchantType);
        t.put(ENCHANT_LEVEL,this.mEnchantLevel);
        return t;
    }

    @Override
    public boolean allowMultiple(){
        return false;
    }

}
