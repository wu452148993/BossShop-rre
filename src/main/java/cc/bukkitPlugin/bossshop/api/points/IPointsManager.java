package cc.bukkitPlugin.bossshop.api.points;

public interface IPointsManager{

    /**
     * 注册一个点券模块
     */
    public void registerPointsAPI(IPointsAPI pPoints);

    /**
     * 根据依赖的插件名获取注册的点券模块
     * @param pName 插件名字,忽视大小写
     */
    public IPointsAPI getPointsAPI(String pName);
    
}
