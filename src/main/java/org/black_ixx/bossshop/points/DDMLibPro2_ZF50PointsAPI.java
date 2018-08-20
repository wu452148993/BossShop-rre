package org.black_ixx.bossshop.points;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import com.ddmcloud.ZF50.ZF50;

import cc.bukkitPlugin.commons.Log;


public class DDMLibPro2_ZF50PointsAPI extends APointsAPI{

    private boolean mInitSuccess;
    
    public DDMLibPro2_ZF50PointsAPI(BossShop pPlugin){
        super(pPlugin,PointsPlugin.DDMLibPro2_ZF50.getCustom());
        Plugin plugin=Bukkit.getServer().getPluginManager().getPlugin(this.getName());
        if(plugin==null){
            this.mInitSuccess=false;
            Log.warn(this.mPlugin.C("MsgNoPointConfigedPluginFound").replace("%plugin%",this.getName()));
            return;
        }else this.mInitSuccess=true;
    }

    @Override
    public int getPoints(OfflinePlayer player){
        if(!this.mInitSuccess) return 0;
        
        try{
            return ZF50.getPlayeCredit(player.getName());
        }catch(Throwable exp){
            if(player.getPlayer()!=null)
            Log.warn(player.getPlayer(),this.mPlugin.C("MsgErrorHappedWhenHandlerPoints"));
        }
        return 0;
    }
    
    @Override
    public int takePoints(OfflinePlayer player,int points){
        if(!this.mInitSuccess) return 0;
        
        try{
            ZF50.takePlayerCredit(player.getName(),points,this.mPlugin.getName());
            return this.getPoints(player);
        }catch(Throwable exp){
            if(player.getPlayer()!=null)
            Log.warn(player.getPlayer(),this.mPlugin.C("MsgErrorHappedWhenHandlerPoints"));
        }
        return 0;
    }

    @Override
    public int givePoints(OfflinePlayer player,int points){
        if(!this.mInitSuccess) return 0;
        
        return this.getPoints(player);
    }

}
