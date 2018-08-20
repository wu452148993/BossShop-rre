package org.black_ixx.bossshop.points;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import cc.bukkitPlugin.commons.Log;

public class PlayerPointsAPI extends APointsAPI{

    private PlayerPoints pp;
    
    public PlayerPointsAPI(BossShop pPlugin){
        super(pPlugin,PointsPlugin.PLAYERPOINTS.getCustom());
        final Plugin plugin=Bukkit.getServer().getPluginManager().getPlugin(this.getName());
        if(plugin!=null){
            pp=(PlayerPoints.class.cast(plugin));
        }else{
            Log.warn(this.mPlugin.C("MsgNoPointConfigedPluginFound").replace("%plugin%",this.getName()));
        }
    }

    @Override
    public int getPoints(OfflinePlayer player){
        if(this.pp==null) return 0;
        
        return pp.getAPI().look(player.getName());
    }

    @Override
    public int takePoints(OfflinePlayer player,int points){
        if(this.pp==null) return 0;
        
        pp.getAPI().take(player.getName(),points);
        return getPoints(player);
    }

    @Override
    public int givePoints(OfflinePlayer player,int points){
        if(this.pp==null) return 0;
        
        pp.getAPI().give(player.getName(),points);
        return getPoints(player);
    }

}
