package cc.bukkitPlugin.bossshop.goods.reward;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.RewardType;
import org.black_ixx.bossshop.managers.WorthHandler;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;


public class RewardTimeCommand implements IReward{

    IdentityHashMap<String,Long> mReward;
    /**该变量只在调用initPrice才会初始化*/
    protected  BossShop mPlugin;
    
    @Override
    public RewardType getRewardType(){
        return RewardType.TimeCommand;
    }

    @Override
    public void giveReward(Player pPlayer,int pMulCount){
        WorthHandler worthMan=this.mPlugin.getManager(WorthHandler.class);
        for(int i=0;i<pMulCount;i++)
            worthMan.giveRewardTimeCommand(pPlayer,this.mReward);
    }

    @Override
    public boolean initReward(BossShop pPlugin,Object pObjReward){
        this.mPlugin=pPlugin;
        if(pObjReward==null){
            Log.severe("未配置商品奖励,商品["+this.mPlugin.C(this.getRewardType().getNameKey())+"]的奖励是必须的");
            return false;
        }
        this.mReward=new IdentityHashMap<>();
        ArrayList<String> lines=new ArrayList<>();
        if(pObjReward instanceof List<?>){
            for(Object sLine : (List<?>)pObjReward){
                if(!(sLine instanceof String)){
                    Log.severe("商品类型为时限命令时,商品配置必须为List<String>或String");
                    return false;
                }
                lines.add(sLine.toString());
            }
        }else if(pObjReward instanceof String){
            lines.add(pObjReward.toString());
        }else{
            Log.severe("商品类型为时限命令时,商品配置必须为List<String>或String");
            return false;
        }
        for(String sLine : lines){
            String[] parts=sLine.split(":",2);
            if(parts.length!=2){
                Log.severe("["+pObjReward+"]不是一个正确的(TimeCommand)类型商品,正确的格式应该是: <时间>:<command> 例如 600:ban %player_name%");
                return false;
            }
            try{
                this.mReward.put(parts[1].trim(),Long.parseLong(parts[0].trim()));
            }catch(NumberFormatException nfexp){
                Log.severe("["+pObjReward+"]不是一个正确的(TimeCommand)类型商品,正确的格式应该是: <时间>:<command> 例如 600:ban %player_name%");
                return false;
            }
        }
        return true;
    }

    @Override
    public IReward copy(){
        RewardTimeCommand tReward=new RewardTimeCommand();
        tReward.mPlugin=this.mPlugin;
        tReward.mReward=new IdentityHashMap<>();
        for(Map.Entry<String,Long> entry : this.mReward.entrySet()){
            tReward.mReward.put(entry.getKey(),entry.getValue());
        }
        return tReward;
    }

    @Override
    public String getDescription(int pMulCount){
        StringBuilder description=new StringBuilder();
        for(Map.Entry<String,Long> entry : this.mReward.entrySet()){
            description.append(entry.getValue()).append("|").append(entry.getKey()).append(", ");
        }
        if(description.length()>2)
            description.delete(description.length()-2,description.length());
        return description.toString();
    }

    @Override
    public IdentityHashMap<String,Long> getReward(int pMulCount){
        return this.mReward;
    }

    @Override
    public boolean allowMultiple(){
        return true;
    }

}
