package org.black_ixx.bossshop.points;

import org.black_ixx.bossshop.BossShop;

import cc.bukkitPlugin.bossshop.api.points.IPointsAPI;


public abstract class APointsAPI implements IPointsAPI{

    protected BossShop mPlugin;
    private final String mPluginName;
    
    public APointsAPI(BossShop pPlugin,String pPluginName){
        this.mPlugin=pPlugin;
        this.mPluginName=pPluginName;
    }
    
    @Override
    public String getName(){
        return this.mPluginName;
    }

}
