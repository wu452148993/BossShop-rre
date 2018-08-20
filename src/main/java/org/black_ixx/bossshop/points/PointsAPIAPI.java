package org.black_ixx.bossshop.points;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import cc.bukkitPlugin.commons.Log;
import me.BukkitPVP.PointsAPI.PointsAPI;

public class PointsAPIAPI extends APointsAPI{

    private PointsAPI pp;

    public PointsAPIAPI(BossShop pPlugin){
        super(pPlugin,PointsPlugin.POINTSAPI.getCustom());
        Plugin pointsApi=this.mPlugin.getServer().getPluginManager().getPlugin(this.getName());
        if(pointsApi!=null){
            pp=((PointsAPI)pointsApi);
        }else{
            Log.warn(this.mPlugin.C("MsgNoPointConfigedPluginFound").replace("%plugin%",this.getName()));
        }
    }

    @Override
    public int getPoints(OfflinePlayer player){
        if(this.pp==null) return 0;
        
        return pp.getPoints(player);
    }

    @Override
    public int takePoints(OfflinePlayer player,int points){
        if(this.pp==null) return 0;
        
        pp.removePoints(player,points);
        return getPoints(player);
    }

    @Override
    public int givePoints(OfflinePlayer player,int points){
        if(this.pp==null) return 0;
        
        pp.addPoints(player,points);
        return getPoints(player);
    }

}
