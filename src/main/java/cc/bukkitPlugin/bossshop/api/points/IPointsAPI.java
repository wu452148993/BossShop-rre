package cc.bukkitPlugin.bossshop.api.points;

import org.bukkit.OfflinePlayer;

public interface IPointsAPI{

    public String getName();

    public abstract int getPoints(OfflinePlayer pPlayer);

    public abstract int takePoints(OfflinePlayer pPlayer,int points);

    public abstract int givePoints(OfflinePlayer pPlayer,int points);
    
}
