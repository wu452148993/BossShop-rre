package cc.bukkitPlugin.bossshop.goods.reward;

import java.util.ArrayList;
import java.util.List;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.RewardType;
import org.black_ixx.bossshop.managers.WorthHandler;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;


public class RewardPermission implements IReward{

    private ArrayList<String> mReward;
    /**该变量只在调用initPrice才会初始化*/
    private  BossShop mPlugin;
    
    @Override
    public RewardType getRewardType(){
        return RewardType.Permission;
    }

    @Override
    public void giveReward(Player pPlayer,int pMulCount){
        WorthHandler worthMan=this.mPlugin.getManager(WorthHandler.class);
        for(int i=0;i<pMulCount;i++)
            worthMan.giveRewardPermission(pPlayer,this.mReward);
    }

    @Override
    public boolean initReward(BossShop pPlugin,Object pObjReward){
        this.mPlugin=pPlugin;
        if(pObjReward==null){
            Log.severe("未配置商品奖励,商品["+this.mPlugin.C(this.getRewardType().getNameKey())+"]的奖励是必须的");
            return false;
        }
        this.mReward=new ArrayList<>();
        if(!(pObjReward instanceof List<?>)){
            if(!(pObjReward instanceof String)){
                Log.severe("商品类型为权限时,商品配置必须为String或List<String>");
                return false;
            }
            this.mReward.add((String)pObjReward);
        }else{
            for(Object sEle : (List<?>)pObjReward){
                if(!String.class.isInstance(sEle)){
                    Log.severe("商品类型为权限时,商品配置必须为String或List<String>");
                    return false;
                }
                this.mReward.add(sEle.toString());
            }
        }
        return true;
    }

    @Override
    public IReward copy(){
        RewardPermission tReward=new RewardPermission();
        tReward.mPlugin=this.mPlugin;
        tReward.mReward=new ArrayList<>();
        for(String sCmd : this.mReward)
            tReward.mReward.add(sCmd);
        return tReward;
    }

    @Override
    public String getDescription(int pMulCount){
        return this.mReward.toString();
    }

    @Override
    public ArrayList<String> getReward(int pMulCount){
        return this.mReward;
    }

    @Override
    public boolean allowMultiple(){
        return false;
    }

}
