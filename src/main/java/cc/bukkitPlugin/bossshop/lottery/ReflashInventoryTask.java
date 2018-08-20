package cc.bukkitPlugin.bossshop.lottery;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.inventory.Inventory;

public class ReflashInventoryTask implements Runnable{
    
    private BossShop mPlugin;
    private final LotteryHolder mHolder;
    private final Inventory mInv;

    public ReflashInventoryTask(BossShop pPlugin,LotteryHolder pHolder){
        this.mPlugin=pPlugin;
        this.mHolder=pHolder;
        this.mInv=this.mHolder.getInventory();
    }
    
    @Override
    public void run(){
        LotteryStatus status=this.mHolder.getStatus();
        if(this.mInv.getViewers().isEmpty()) this.stopTask();
        if(status==LotteryStatus.WAIT||status==LotteryStatus.RUNNING){
            this.mHolder.gotoNextFrame();
        }else if(status==LotteryStatus.CLOSE){
            this.stopTask();
        }
        //status==LotteryStatus.RESULT时等待,不关闭任务,可以为下次连续抽奖做准备
    }
    
    protected void stopTask(){
        LotteryManager.stopTask(this.mHolder);
    }

}
