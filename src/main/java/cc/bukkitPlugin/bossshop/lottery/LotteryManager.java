package cc.bukkitPlugin.bossshop.lottery;

import java.util.HashMap;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.IConfigModel;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.commentedyaml.CommentedYamlConfig;

public class LotteryManager implements IConfigModel{

    public static int mReflashBaseTicks=5;
    public static int mReflashLowMultiple=4;
    public static int mRefalshFastCount=20;
    public static int mShowPrecent=34;
    private BossShop mPlugin;

    public LotteryManager(BossShop pPlugin){
        this.mPlugin=pPlugin;
        this.mPlugin.getConfigManager().registerConfigModel(this);
    }

    @Override
    public void setConfig(CommandSender pSender,CommentedYamlConfig pConfig){
        CommentedSection tSec=this.mPlugin.getConfigManager().getConfig().getSection("LotterySystem");
        if(tSec==null)
            return;
        mReflashBaseTicks=tSec.getInt("ReflashBaseTicks",mReflashBaseTicks);
        if(mReflashBaseTicks<2)
            mShowPrecent=2;
        mReflashLowMultiple=tSec.getInt("ReflashLowMultiple",mReflashLowMultiple);
        if(mReflashLowMultiple<1)
            mReflashLowMultiple=1;
        mRefalshFastCount=tSec.getInt("RefalshFastCount",mRefalshFastCount);
        if(mRefalshFastCount<1)
            mRefalshFastCount=20;
        mShowPrecent=tSec.getInt("ShowPrecent",mShowPrecent);
        if(mShowPrecent<=0||mShowPrecent>50)
            mShowPrecent=34;
        Log.info(pSender,this.mPlugin.C("MsgLotteryConfigReloacded")+": "+mReflashBaseTicks+"|"+mReflashLowMultiple+"|"+mRefalshFastCount+"|"+mShowPrecent);
    }

    /**
     * 任务列表
     * <p>
     * 第一个参数为任务目标背包的持有者<br />
     * 第二个参数为Bukkit任务ID
     * </p>
     */
    private final static HashMap<LotteryHolder,Integer> mTasks=new HashMap<>();

    /**
     * 添加一个任务到列表中
     * @param pHolder 任务关联的holder
     * @param pTaskId Bukkit任务Id
     */
    public static void addTask(LotteryHolder pHolder,int pTaskId){
        LotteryManager.mTasks.put(pHolder,pTaskId);
    }

    /**
     * 尝试停止一个列表中的任务
     * @param pHolder 任务关联的holder
     */
    public static void stopTask(LotteryHolder pHolder){
        Integer tTaskId=LotteryManager.mTasks.remove(pHolder);
        if(tTaskId!=null){
            Bukkit.getScheduler().cancelTask(tTaskId);
        }
    }

    /**
     * 检查holder是否已经有关联的任务存在
     * @param pHolder 任务关联的holder
     * @return 是否存在
     */
    public static boolean existTask(LotteryHolder pHolder){
        return LotteryManager.mTasks.containsKey(pHolder);
    }

    @Override
    public void addDefaults(CommentedYamlConfig pConfig){}

}
