package cc.bukkitPlugin.bossshop.gui.tasks;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class UpdateInvTask extends BukkitRunnable{

    private final Player mPlayer;

    public UpdateInvTask(Player pPlayer){
        this.mPlayer=pPlayer;
    }

    @Override
    public void run(){
        if(this.mPlayer.isOnline()){
            this.mPlayer.updateInventory();
        }
    }

}
