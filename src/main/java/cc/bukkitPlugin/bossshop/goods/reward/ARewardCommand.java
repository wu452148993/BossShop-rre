package cc.bukkitPlugin.bossshop.goods.reward;

import java.util.ArrayList;
import java.util.List;

import org.black_ixx.bossshop.BossShop;


public abstract class ARewardCommand implements IReward{
    
    protected ArrayList<String> mReward;
    /**该变量只在调用initPrice才会初始化*/
    protected  BossShop mPlugin;

    @Override
    public boolean initReward(BossShop pPlugin,Object pObjReward){
        this.mPlugin=pPlugin;
        this.mReward=new ArrayList<>();
        if(!(pObjReward instanceof List<?>)){
            if(!(pObjReward instanceof String))
                return false;
            this.mReward.add((String)pObjReward);
        }else{
            for(Object sEle : (List<?>)pObjReward){
                if(!String.class.isInstance(sEle))
                    return false;
                this.mReward.add(sEle.toString());
            }
        }
        return true;
    }

    protected <T extends ARewardCommand> T setContent(T pReward){
        pReward.mPlugin=this.mPlugin;
        pReward.mReward=new ArrayList<>();
        for(String sCmd : this.mReward)
            pReward.mReward.add(sCmd);
        return pReward;
    }

    @Override
    public String getDescription(int pMulCount){
        return this.mReward.toString();
    }
    
    @Override
    public ArrayList<String> getReward(int pMulCount){
        return this.mReward;
    }

}
