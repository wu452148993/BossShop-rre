package cc.bukkitPlugin.bossshop.goods.reward;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.RewardType;
import org.black_ixx.bossshop.managers.ItemNameManager;
import org.black_ixx.bossshop.managers.ItemStackCreator;
import org.black_ixx.bossshop.managers.WorthHandler;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import cc.bukkitPlugin.commons.Log;


public class RewardItem implements IReward{

    private IdentityHashMap<ItemStack,Integer> mRewardItems;
    /**该变量只在调用initPrice才会初始化*/
    private  BossShop mPlugin;
    
    @Override
    public RewardType getRewardType(){
        return RewardType.Item;
    }

    @Override
    public void giveReward(Player pPlayer,int pMulCount){
        this.mPlugin.getManager(WorthHandler.class).giveRewardItem(pPlayer,this.mulItem(pMulCount));
    }

    @Override
    public boolean initReward(BossShop pPlugin,Object pObjReward){
        this.mPlugin=pPlugin;
        if(pObjReward==null){
            Log.severe("未配置商品奖励,商品["+this.mPlugin.C(this.getRewardType().getNameKey())+"]的奖励是必须的");
            return false;
        }
        if(!(pObjReward instanceof List<?>)){
            Log.severe("商品类型为物品时,商品配置必须为List");
            return false;
        }
        List<?> l=(List<?>)pObjReward;
        boolean mul=false;
        for(Object o : l){
            if(o instanceof List<?>){
                mul=true;
                break;
            }
        }
        this.mRewardItems=new IdentityHashMap<>();
        ItemStackCreator itemCteator=this.mPlugin.getManager(ItemStackCreator.class);
        try{
            List<List<String>> itemInfos;
            if(mul) itemInfos=(List<List<String>>)pObjReward;
            else{
                itemInfos=new ArrayList<>();
                itemInfos.add((List<String>)pObjReward);
            }
            for(List<String> s : itemInfos){
                ItemStack tItem=itemCteator.createItemStackS(s,false);
                if(tItem==null) return false;
                int count=tItem.getAmount();
                tItem.setAmount(1);
                this.mRewardItems.put(tItem,count);
            }
        }catch(Exception exp){
            return false;
        }
        return !this.mRewardItems.isEmpty();
    }

    @Override
    public IReward copy(){
        RewardItem tReward=new RewardItem();
        tReward.mPlugin=this.mPlugin;
        tReward.mRewardItems=new IdentityHashMap<>();
        for(Map.Entry<ItemStack,Integer> entry : this.mRewardItems.entrySet()){
            tReward.mRewardItems.put(entry.getKey().clone(),entry.getValue());
        }
        return tReward;
    }

    @Override
    public String getDescription(int pMulCount){
        ArrayList<ItemStack> tItems=this.mulItem(pMulCount);
        ItemNameManager nameMan=this.mPlugin.getManager(ItemNameManager.class);
        StringBuilder description=new StringBuilder();
        String wordCount=this.mPlugin.C("WordCount");
        for(ItemStack sItem : tItems){
            description.append(sItem.getAmount()).append(wordCount).append(nameMan.getDisplayName(sItem)).append(", ");
        }
        if(description.length()>2)
            description.delete(description.length()-2,description.length());
        return description.toString();
    }
    
    /**
     * 此处物品使用了拷贝
     */
    private ArrayList<ItemStack> mulItem(int pMulCount){
        ArrayList<ItemStack> tItems=new ArrayList<>();
        for(Map.Entry<ItemStack,Integer> entry : this.mRewardItems.entrySet()){
            ItemStack tItem=entry.getKey().clone();
            tItem.setAmount((int)(entry.getValue()*pMulCount));
            tItems.add(tItem);
        }
        return tItems;
    }

    @Override
    public ArrayList<ItemStack> getReward(int pMulCount){
        return this.mulItem(pMulCount);
    }

    @Override
    public boolean allowMultiple(){
        return true;
    }
    
}
