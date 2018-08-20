package cc.bukkitPlugin.bossshop.gui.tasks;

import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

public class CloseInvTask extends BukkitRunnable{

    private final Player mPlayer;
    private Runnable mCallBack=null;

    public CloseInvTask(Player pPlayer){
        this.mPlayer=pPlayer;
    }

    public CloseInvTask(Player pPlayer,Runnable pCallBack){
        this(pPlayer);
        this.mCallBack=pCallBack;
    }

    @Override
    public void run(){
        if(this.mPlayer.isOnline()&&!(this.mPlayer.getOpenInventory() instanceof PlayerInventory)){
            this.mPlayer.closeInventory();
        }

        if(this.mCallBack!=null){
            this.mCallBack.run();
        }
    }

}
