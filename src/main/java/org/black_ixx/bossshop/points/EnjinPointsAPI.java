package org.black_ixx.bossshop.points;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import com.enjin.officialplugin.points.ErrorConnectingToEnjinException;
import com.enjin.officialplugin.points.PlayerDoesNotExistException;
import com.enjin.officialplugin.points.PointsAPI.Type;

import cc.bukkitPlugin.commons.Log;

public class EnjinPointsAPI extends APointsAPI{

    public EnjinPointsAPI(BossShop pPlugin){
        super(pPlugin,PointsPlugin.ENJIN_MINECRAFT_PLUGIN.getCustom());
        Plugin plugin=Bukkit.getServer().getPluginManager().getPlugin(this.getName());
        if(plugin==null){
            Log.warn(this.mPlugin.C("MsgNoPointConfigedPluginFound").replace("%plugin%",this.getName()));
        }
    }

    public int getPoints(OfflinePlayer player){
        try{
            return com.enjin.officialplugin.points.PointsAPI.getPointsForPlayer(player.getName());
        }catch(PlayerDoesNotExistException e){
            Log.severe("[Enjin Minecraft Plugin] Not able to get Player "+player.getName()+". Not Existing!");
        }catch(ErrorConnectingToEnjinException e){
            Log.severe("[Enjin Minecraft Plugin] Not able to connect to Enjin!");
        }
        return 0;
    }

    public int setPoints(OfflinePlayer player,int points){
        com.enjin.officialplugin.points.PointsAPI.modifyPointsToPlayerAsynchronously(player.getName(),points,Type.SetPoints);
        return points;
    }

    public int takePoints(OfflinePlayer player,int points){
        String name=player.getName();
        try{
            return com.enjin.officialplugin.points.PointsAPI.modifyPointsToPlayer(name,points,Type.RemovePoints);
        }catch(NumberFormatException e){
            Log.severe("[Enjin Minecraft Plugin] Not able to take Points... \"NumberFormatException\" Tried to take "+points+" Points from "+name+".");
        }catch(PlayerDoesNotExistException e){
            Log.severe("[Enjin Minecraft Plugin] Not able to get Player "+name+". Not Existing!");
        }catch(ErrorConnectingToEnjinException e){
            Log.severe("[Enjin Minecraft Plugin] Not able to connect to Enjin!");
        }
        return getPoints(player);
    }

    public int givePoints(OfflinePlayer player,int points){
        String name=player.getName();
        try{
            return com.enjin.officialplugin.points.PointsAPI.modifyPointsToPlayer(name,points,Type.AddPoints);
        }catch(NumberFormatException e){
            Log.severe("[Enjin Minecraft Plugin] Not able to take Points... \"NumberFormatException\" Tried to take "+points+" Points from "+name+".");
        }catch(PlayerDoesNotExistException e){
            Log.severe("[Enjin Minecraft Plugin] Not able to get Player "+name+". Not Existing!");
        }catch(ErrorConnectingToEnjinException e){
            Log.severe("[Enjin Minecraft Plugin] Not able to connect to Enjin!");
        }
        return getPoints(player);
    }

}
