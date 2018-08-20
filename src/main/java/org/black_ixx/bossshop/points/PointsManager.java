package org.black_ixx.bossshop.points;

import java.util.HashMap;
import java.util.Map;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.bossshop.api.points.IPointsAPI;
import cc.bukkitPlugin.bossshop.api.points.IPointsManager;
import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.INeedReload;
import cc.bukkitPlugin.commons.plugin.manager.AManager;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.IConfigModel;
import cc.commons.commentedyaml.CommentedYamlConfig;
import cc.commons.util.StringUtil;

public class PointsManager extends AManager<BossShop> implements IPointsAPI,IPointsManager,IConfigModel,INeedReload{

    private HashMap<String,IPointsAPI> mPointsPluginInterfaces=new HashMap<String,IPointsAPI>();

    private IPointsAPI pa;
    private PointsPlugin mFoundPointPlugin;
    private String mSetPointsPlugin="auto-detect";

    /**
     * 点券管理系统
     */
    public PointsManager(BossShop pPlugin){
        super(pPlugin);
        
        this.mPlugin.getConfigManager().registerConfigModel(this);
        this.mPlugin.registerReloadModel(this);
    }

    public String getPluginName(){
        return this.mFoundPointPlugin==null?"":this.mFoundPointPlugin.name();
    }

    @Override
    public void setConfig(CommandSender pSender,CommentedYamlConfig pConfig){
        this.mSetPointsPlugin=this.mPlugin.getConfigManager().getConfig().getString("PointsPlugin",this.mSetPointsPlugin);
    }
    

    @Override
    public String getName(){
        return "";
    }

    @Override
    public boolean reloadConfig(CommandSender pSender){
        this.mFoundPointPlugin=null;
        // 检索可用的点券插件模块
        for(PointsPlugin sPlugin : PointsPlugin.values()){
            if(sPlugin==PointsPlugin.CUSTOM){
                if(this.getPointsAPI(this.mSetPointsPlugin)!=null){
                    PointsPlugin.CUSTOM.setCustom(this.mSetPointsPlugin);
                    this.mFoundPointPlugin=PointsPlugin.CUSTOM;
                }
            }else{
                if(Bukkit.getPluginManager().getPlugin(sPlugin.getCustom())!=null){
                    if(this.mFoundPointPlugin==null||this.mSetPointsPlugin.equalsIgnoreCase(sPlugin.getCustom()))
                        this.mFoundPointPlugin=sPlugin;
                }
            }
        }

        if(this.mFoundPointPlugin==null){
            Log.warn(pSender,this.mPlugin.C("MsgNotFoundAnySupportPointsPlugin"));
            pa=new FailedPointsAPI(this.mPlugin);
            return true;
        }else if(this.mSetPointsPlugin.equalsIgnoreCase("auto-detect")){
            Log.info(pSender,this.mPlugin.C("MsgAutoDetectPointPlugin").replace("%plugin%",this.mFoundPointPlugin.getCustom()));
        }else if(!this.mFoundPointPlugin.getCustom().equalsIgnoreCase(this.mSetPointsPlugin)){
            Log.info(pSender,this.mPlugin.C("MsgNotFoundPreferencePointsPluginButSupportOneFound").replace("%plugin_set%",mSetPointsPlugin).replace("%plugin_found%",mFoundPointPlugin.getCustom()));
        }else{
            Log.info(pSender,this.mPlugin.C("MsgSetPreferencePointsPlugin").replace("%plugin%",mFoundPointPlugin.getCustom()));
        }
        switch(this.mFoundPointPlugin){
        case COMMANDPOINTS:
            pa=new CommandPointsAPI(this.mPlugin);
            return true;
        case ENJIN_MINECRAFT_PLUGIN:
            pa=new EnjinPointsAPI(this.mPlugin);
            return true;
        case PLAYERPOINTS:
            pa=new PlayerPointsAPI(this.mPlugin);
            return true;
        case POINTSAPI:
            pa=new PointsAPIAPI(this.mPlugin);
            return true;
        case DDMLibPro2_ZF50:
            pa=new DDMLibPro2_ZF50PointsAPI(this.mPlugin);
            return true;
        case CUSTOM:
            IPointsAPI customPoints=this.getPointsAPI(mFoundPointPlugin.getCustom());
            if(customPoints!=null){
                pa=customPoints;
                return true;
            }
            break;
        }
        Log.warn(pSender,this.mPlugin.C("MsgNotFoundAnySupportPointsPlugin"));
        pa=new FailedPointsAPI(this.mPlugin);
        return true;
    }

    public void registerPointsAPI(IPointsAPI pPoints){
        if(pPoints==null) return;

        this.mPointsPluginInterfaces.put(pPoints.getName(),pPoints);
    }

    public IPointsAPI getPointsAPI(String pName){
        if(StringUtil.isEmpty(pName)) return null;

        for(Map.Entry<String,IPointsAPI> sEntry : this.mPointsPluginInterfaces.entrySet()){
            if(sEntry.getKey().equalsIgnoreCase(pName)) return sEntry.getValue();
        }
        return null;
    }

    public int getPoints(OfflinePlayer player){
        return pa.getPoints(player);
    }

    public int givePoints(OfflinePlayer player,int points){
        return pa.givePoints(player,points);
    }

    public int takePoints(OfflinePlayer player,int points){
        return pa.takePoints(player,points);
    }

    @Override
    public void addDefaults(CommentedYamlConfig pConfig){
    }

}
