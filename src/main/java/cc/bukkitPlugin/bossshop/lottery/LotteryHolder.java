package cc.bukkitPlugin.bossshop.lottery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.RewardType;
import org.black_ixx.bossshop.core.BSGoods;
import org.black_ixx.bossshop.core.BSShop;
import org.black_ixx.bossshop.core.BSShopHolder;
import org.black_ixx.bossshop.managers.ShopCustomizer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;

public class LotteryHolder extends BSShopHolder{

    private int mStopCount=4;
    private LotteryStatus mStatus=LotteryStatus.UNOPENED;
    private final Map<Integer,String> mNowShow=Collections.synchronizedMap(new HashMap<Integer,String>());
    private Random mItemRandom=new Random(System.currentTimeMillis());

    public LotteryHolder(BossShop pPlugin,Player pOwner,BSShop pShop){
        super(pPlugin,pOwner,pShop);
    }

    /**
     * 获取当前乐透状态
     */
    public LotteryStatus getStatus(){
        synchronized(this){
            return this.mStatus;
        }
    }

    /**
     * 切换抽奖背包状态为LotteryStatus.RUNNING,并刷新一次
     */
    public void startLottery(){
        this.mStatus=LotteryStatus.RUNNING;
        this.mStopCount=LotteryManager.mRefalshFastCount;
        this.gotoNextFrame();
    }

    /**
     * 重置乐透,切换抽奖背包状态为{@link LotteryStatus#WAIT}
     */
    public void resetLottery(){
        this.mStatus=LotteryStatus.UNOPENED;
        this.mNowShow.clear();
        this.runSyncTask(()->{
            this.mInv.clear();
            this.updateInventory(false);
        });
    }

    /**
     * 关闭乐透,在背包关闭时调用,此时切换背包状态为{@link LotteryStatus#CLOSE}
     */
    public void closeLottery(){
        this.mInv.clear();
        this.mStatus=LotteryStatus.CLOSE;
    }

    /**
     * 获取奖励物品.,此函数获取的物品和乐透状态有关
     */
    @Override
    public BSGoods getDisplayItemAt(int pPosition){
        synchronized(this){
            if(this.mStatus==LotteryStatus.WAIT){
                BSGoods tBuy=this.getGoods(pPosition);
                if(tBuy!=null&&tBuy.getFirstReward().getRewardType()==RewardType.Lottery&&tBuy.getWeight()<=0)
                    return tBuy;
                else return null;
            }else if(this.mStatus==LotteryStatus.RUNNING){
                return null;
            }else if(this.mStatus==LotteryStatus.RESULT){
                if(!this.mNowShow.containsKey(pPosition)) return null;
                return this.getGoods(pPosition);
            }
            return null;
        }
    }

    @Override
    public Set<Integer> getDisplayPositions(){
        HashSet<Integer> t=new HashSet<>(this.mNowShow.keySet());
        return t;
    }

    @Override
    public Map<Integer,String> getDisplayMap(){
        HashMap<Integer,String> a=new HashMap<>();
        synchronized(this){
            a.putAll(this.mNowShow);
        }
        return a;
    }

    /**
     * 只在配置重载,或者背包打开时调用
     * <p>
     * 1.同步更新配置到物品<br />
     * 2.用于在背包打开的时候切换holder从{@link LotteryStatus#UNOPENED}到{@link LotteryStatus#WAIT}
     * </p>
     */
    @Override
    public boolean updateInventory(boolean pReset){
        synchronized(this){
            if(!this.mShop.isShopValid()){
                this.mInv.clear();
                this.mOwner.closeInventory();
                return false;
            }
            if(this.mStatus==LotteryStatus.UNOPENED){
                this.mStatus=LotteryStatus.WAIT;
                this.mStopCount=4;
                if(!LotteryManager.existTask(this)){
                    ReflashInventoryTask tTask=new ReflashInventoryTask(this.mPlugin,this);
                    int tTaskId=Bukkit.getScheduler().scheduleAsyncRepeatingTask(BossShop.getInstance(),tTask,LotteryManager.mReflashBaseTicks,LotteryManager.mReflashBaseTicks);
                    LotteryManager.addTask(this,tTaskId);
                    Log.debug("新增抽奖背包刷新任务");
                }
                return this.gotoNextFrame();
            }
            super.updateInventory(pReset);
            return true;
        }
    }

    /**
     * 显示抽奖背包的下一帧动画
     * <p>
     * 在背包状态为{@link LotteryStatus#WAIT}和{@link LotteryStatus#RUNNING}的时候调用<br />
     * 在{@link LotteryStatus#RUNNING}状态的最后一帧动画时,由该函数切换状态为{@link LotteryStatus#RESULT}
     * </p>
     */
    public boolean gotoNextFrame(){
        synchronized(this){
            if(!this.mShop.isShopValid()){
                this.runSyncTask(()->{
                    this.mInv.clear();
                    this.mOwner.closeInventory();
                });
                return false;
            }
            if(this.mStatus==LotteryStatus.WAIT){
                if(this.mStopCount<=0){
                    this.reselectItemAndUpdate();
                    this.mStopCount=LotteryManager.mReflashLowMultiple;
                }
                this.mStopCount--;
            }else if(this.mStatus==LotteryStatus.RUNNING&&this.mStopCount>0){
                this.reselectItemAndUpdate();
                this.mStopCount--;
            }else if(this.mStopCount<=0||this.mStatus==LotteryStatus.RESULT){
                if(this.mStatus!=LotteryStatus.RESULT){
                    //一定要先清背包,再切换状态
                    this.mNowShow.clear();
                    int result=this.getResultItem();
                    if(result!=-1){
                        String tName=this.mDisplayPos.get(result);
                        this.mNowShow.put(result,tName);
                        BSGoods tGoods=this.mShop.getGoods(result);
                        if(tGoods!=null){
                            Log.info(this.mPlugin.C("MsgPlayerLotteryGetInShopWithItem",new String[]{"%player%","%shop%","%item%"},this.mOwner.getName(),this.mShop.getShopName(),tGoods.getName()));
                            this.runSyncTask(()->{
                                this.mInv.clear();
                                this.mPlugin.getManager(ShopCustomizer.class).addGoodsToInv(this.mOwner,this.mInv,result,tGoods);
                            });
                        }
                    }
                    // 有问题,需要根据NowShow来检查获取的物品6
                    this.mStatus=LotteryStatus.RESULT;
                }
            }
            return true;
        }
    }

    protected void reselectItemAndUpdate(){
        this.reselectMenuItem();
        this.runSyncTask(()->{
            this.mInv.clear();
            this.mPlugin.getManager(ShopCustomizer.class).updateInventory(this);
        });
    }

    protected void runSyncTask(Runnable pTask){
        if(Bukkit.isPrimaryThread()){
            pTask.run();
        }else{
            Bukkit.getScheduler().runTask(this.mPlugin,pTask);
        }
    }

    /**
     * 重新设置{@link LotteryHolder#mNowShow}的内容
     * <p>
     * 此函数不会自动刷新背包,需要自己调用刷新函数<br />
     * 如果背包当前状态为{@link LotteryStatus#WAIT}状态时,会自动添加一个抽奖按钮到当前显示列表
     * 该抽奖按钮的设置条件为类型={@link RewardType#Lottery}且weight为0,且是第一个,该物品的添加
     * 不会影响显示比例<br />
     * 如果概率显示物品的数量除按钮外为零个,那么如果存在可以显示的物品,显示物品会至少添加一个到显示列表
     * </p>
     */
    private void reselectMenuItem(){
        HashSet<String> tOldItem=new HashSet<>();
        tOldItem.addAll(this.mNowShow.values());
        this.mNowShow.clear();
        ArrayList<Map.Entry<Integer,String>> canShowItem=new ArrayList<>();
        //第一遍循环找出所有可以显示给玩家的商品,如果是等待状态,添加抽奖按钮到显示列表
        boolean find=false;
        int total=0;
        for(Map.Entry<Integer,String> sEntry : this.mDisplayPos.entrySet()){
            BSGoods tGoods=this.mShop.getGoods(sEntry.getValue());
            if(tGoods==null||!tGoods.disaplyToPlayer(this.mOwner))
                continue;
            if(tGoods.getFirstReward().getRewardType()==RewardType.Lottery&&tGoods.getWeight()<=0){
                if(this.mStatus==LotteryStatus.WAIT&&!find){
                    find=true;
                    this.mNowShow.put(sEntry.getKey(),tGoods.getName());
                    continue;
                }else if(this.mStatus==LotteryStatus.RUNNING){
                    continue;
                }

            }
            total++;
            if(tOldItem.contains(sEntry.getValue()))
                continue;
            int tPos=this.mItemRandom.nextInt(canShowItem.size()+1);
            canShowItem.add(tPos,sEntry);
        }
        int showCount=total*LotteryManager.mShowPrecent/100;
        if(showCount<=0)
            showCount=1;
        for(int index=0;index<showCount&&index<canShowItem.size();index++){
            Map.Entry<Integer,String> tEntry=canShowItem.get(index);
            this.mNowShow.put(tEntry.getKey(),tEntry.getValue());
        }
    }

    /**
     * 获取抽奖结果物品,使用权重算法
     * <p>
     * 如果没有符合条件的物品将返回-1
     * </p>
     * 
     * @return 物品背包位置
     */
    private int getResultItem(){
        LinkedHashMap<Integer,Integer> weigthQueue=new LinkedHashMap<>();
        //设置权重队列值
        int addWeight=0;
        for(Map.Entry<Integer,String> sEntry : this.mDisplayPos.entrySet()){
            BSGoods tGoods=this.mShop.getGoods(sEntry.getKey());
            if(tGoods==null)
                continue;
            if(tGoods.getWeight()<=0||!tGoods.disaplyToPlayer(this.mOwner))
                continue;
            addWeight+=tGoods.getWeight();
            weigthQueue.put(addWeight,sEntry.getKey());
        }
        if(weigthQueue.size()==0)
            return -1;
        int findWeight=mItemRandom.nextInt(addWeight+1);
        //确定是哪个物品
        for(int sWeight : weigthQueue.keySet()){
            if(sWeight<findWeight)
                continue;
            return weigthQueue.get(sWeight);
        }
        return -1;
    }

}
