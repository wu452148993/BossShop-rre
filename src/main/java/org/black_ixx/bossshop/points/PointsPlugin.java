package org.black_ixx.bossshop.points;

public enum PointsPlugin{
    PLAYERPOINTS("PlayerPoints"),
    COMMANDPOINTS("CommandPoints"),
    ENJIN_MINECRAFT_PLUGIN("EnjinMinecraftPlugin"),
    POINTSAPI("PointsAPI"),
    DDMLibPro2_ZF50("DDMLibPro2"),
    CUSTOM(null);

    /**依赖的插件名字*/
    private String mName;

    private PointsPlugin(String pPluginName){
        this.mName=pPluginName;
    }
    
    public void setCustom(String pName){
        if(this!=PointsPlugin.CUSTOM){
            throw new UnsupportedOperationException("请勿为除CUSTOM外的枚举赋值名字");
        }
        this.mName=pName;
    }

    /**
     * 获取依赖的插件名字
     * <p>如果枚举类型为{@link PointsPlugin#CUSTOM},名字由当前使用的注册的点券模块决定</p>
     */
    public String getCustom(){
        return this.mName;
    }
}
