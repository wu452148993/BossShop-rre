package org.black_ixx.bossshop.points;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import cc.bukkitPlugin.commons.Log;
import pgDev.bukkit.CommandPoints.CommandPoints;

public class CommandPointsAPI extends APointsAPI{

    private pgDev.bukkit.CommandPoints.CommandPointsAPI mPPlugin;

    public CommandPointsAPI(BossShop pPlugin){
        super(pPlugin,PointsPlugin.COMMANDPOINTS.getCustom());
        Plugin commandPoints=pPlugin.getServer().getPluginManager().getPlugin(this.getName());
        if(commandPoints!=null){
            mPPlugin=((CommandPoints)commandPoints).getAPI();
        }else{
            Log.warn(this.mPlugin.C("MsgNoPointConfigedPluginFound").replace("%plugin%",this.getName()));
        }
    }

    @Override
    public int getPoints(OfflinePlayer player){
        if(this.mPPlugin==null) return 0;
        
        return this.mPPlugin.getPoints(player.getName(),this.mPlugin);
    }

    @Override
    public int takePoints(OfflinePlayer player,int points){
        if(this.mPPlugin==null) return 0;
        
        this.mPPlugin.removePoints(player.getName(),points,"Purchase",this.mPlugin);
        return this.getPoints(player);
    }

    @Override
    public int givePoints(OfflinePlayer player,int points){
        if(this.mPPlugin==null) return 0;
        
        this.mPPlugin.addPoints(player.getName(),points,"Reward",this.mPlugin);
        return this.getPoints(player);
    }

}
