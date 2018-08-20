package org.black_ixx.bossshop.listeners;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSShopHolder;
import org.black_ixx.bossshop.core.BSShopManager;
import org.black_ixx.bossshop.managers.ItemNameManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import cc.bukkitPlugin.bossshop.mail.MailManager;
import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.IConfigModel;
import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.commons.commentedyaml.CommentedYamlConfig;

public class PlayerListener implements Listener,IConfigModel{

    private BossShop mPlugin;
    private Material mOpenItem=Material.CLOCK; 
    private boolean mSneakNeed=true;

    public PlayerListener(BossShop pPlugin){
        this.mPlugin=pPlugin;
        this.mPlugin.getServer().getPluginManager().registerEvents(this,this.mPlugin);
        this.mPlugin.getConfigManager().registerConfigModel(this);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerInteractItem(PlayerInteractEvent pEvent){
        Player player=pEvent.getPlayer();
        ItemStack tItem=player.getItemInHand();
        if(!BukkitUtil.isValidItem(tItem)||tItem.getType()!=this.mOpenItem||(pEvent.getAction()!=Action.RIGHT_CLICK_AIR&&pEvent.getAction()!=Action.RIGHT_CLICK_BLOCK))
            return;
        if(!player.hasPermission(this.mPlugin.getName()+".open.item"))
            return;
        Inventory tInv=player.getOpenInventory().getTopInventory();
        if(tInv!=null&&tInv.getHolder() instanceof BSShopHolder){
            if(((BSShopHolder)tInv.getHolder()).getShop().getShopName().equalsIgnoreCase(this.mPlugin.getConfigManager().getMainShop()))
                return;
        }
        if(!mSneakNeed||player.isSneaking()){
            pEvent.setCancelled(true);
            this.mPlugin.getManager(BSShopManager.class).openShop(player,this.mPlugin.getConfigManager().getMainShop());
        }
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent pEvent){
        Bukkit.getScheduler().runTaskLater(this.mPlugin,new Runnable(){

            @Override
            public void run(){
                PlayerListener.this.mPlugin.getManager(MailManager.class).noticeExistMail(pEvent.getPlayer());
            }
        },100);
    }

    @Override
    public void setConfig(CommandSender pSender,CommentedYamlConfig pConfig){
        CommentedYamlConfig tConfig=this.mPlugin.getConfigManager().getConfig();
        String bindOpenShopItemId=tConfig.getString("OpenMainShop.BindItem","CLOCK");
        this.mOpenItem=Material.getMaterial(bindOpenShopItemId);
        if(this.mOpenItem==null){
            Log.warn(pSender,this.mPlugin.C("MsgBindItemToOpenMainShopNotExist").replace("%item%",bindOpenShopItemId+""));
            tConfig.set("OpenMainShop.BindItem",347);
            this.mPlugin.getConfigManager().saveConfig(null);
            this.mOpenItem=Material.CLOCK;
        }else Log.info(pSender,this.mPlugin.C("MsgUseItemToOpenMainShop").replace("%item%",this.mPlugin.getManager(ItemNameManager.class).getName(this.mOpenItem,(short)0)));
        this.mSneakNeed=tConfig.getBoolean("OpenMainShop.SneakNeed");
    }

    @Override
    public void addDefaults(CommentedYamlConfig pConfig){

    }

}
