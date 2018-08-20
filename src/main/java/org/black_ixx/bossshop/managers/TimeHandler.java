package org.black_ixx.bossshop.managers;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.timedCommands.TimedCommands;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.manager.AManager;
import cc.commons.util.StringUtil;

public class TimeHandler extends AManager<BossShop>{

    private TimedCommands tc;

    public TimeHandler(BossShop pPlugin){
        super(pPlugin);
    }

    private void buildPlugin(){
        Plugin plugin=Bukkit.getServer().getPluginManager().getPlugin("TimedCommands");
        if(plugin!=null){
            tc=(TimedCommands.class.cast(plugin));
        }else{
            Log.warn("未发现TimedCommands插件,如果你需要使用该插件,请前往: http://dev.bukkit.org/bukkit-plugins/scheduledcommands/");
            return;
        }
    }
    
    private TimedCommands getTimedCommands(){
        if(tc==null) this.buildPlugin();
        return tc;
    }
    
    /**
     * 添加一个延时命令
     * @param pDelay 命令延时(秒)
     * @param pCommand command
     */
    public void addCommand(long pDelay,String pCommand){
        if(pDelay<0) pDelay=0;
        if(StringUtil.isEmpty(pCommand)) return;
        if(this.getTimedCommands()==null){
            Log.warn("未发现TimedCommands插件,放弃命令["+pCommand+"],延迟: "+pDelay+"秒 的添加");
            return;
        }
        this.tc.getStoreHandler().addCommandToHashMap(System.currentTimeMillis()+pDelay*1000,pCommand);
    }

}
