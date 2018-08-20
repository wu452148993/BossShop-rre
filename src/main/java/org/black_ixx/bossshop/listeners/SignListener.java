package org.black_ixx.bossshop.listeners;

import java.util.Map;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSShop;
import org.black_ixx.bossshop.core.BSShopManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.IConfigModel;
import cc.commons.commentedyaml.CommentedYamlConfig;
import cc.commons.util.StringUtil;

public class SignListener implements Listener,IConfigModel{

    private boolean mEnable;
    private BossShop mPlugin;

    public SignListener(BossShop plugin){
        this.mPlugin=plugin;

        this.mPlugin.getServer().getPluginManager().registerEvents(this,this.mPlugin);
        this.mPlugin.getConfigManager().registerConfigModel(this);
    }

    @Override
    public void setConfig(CommandSender pSender,CommentedYamlConfig pConfig){
        this.mEnable=this.mPlugin.getConfigManager().signsEnabled();
    }

    private BSShop getBossShopSign(String pLine){
        if(StringUtil.isEmpty(pLine))
            return null;
        pLine=pLine.toLowerCase();
        Map<String,BSShop> set=this.mPlugin.getManager(BSShopManager.class).getShops();
        for(String s : set.keySet()){
            BSShop shop=set.get(s);
            if(pLine.endsWith(shop.getSignText().toLowerCase()))
                return shop;
        }
        return null;
    }

    @EventHandler
    public void createSign(SignChangeEvent pEvent){
        if(!this.mEnable)
            return;
        BSShop shop=getBossShopSign(pEvent.getLine(0));
        if(shop!=null){
            if(shop.needPermToCreateSign()){
                if(!pEvent.getPlayer().hasPermission(this.mPlugin.getName()+".createSign")){
                    Log.send(pEvent.getPlayer(),this.mPlugin.getLangManager().getNode("MsgNoPermitToCreateSignShop"));
                    pEvent.setCancelled(true);
                    return;
                }
            }
            for(int i=0;i<pEvent.getLines().length;i++){
                String tLine=pEvent.getLine(i);
                if(StringUtil.isEmpty(tLine))
                    continue;
                pEvent.setLine(i,ChatColor.translateAlternateColorCodes('&',tLine));
            }
        }
    }

    @EventHandler
    public void interactSign(PlayerInteractEvent pEvent){
        if(!this.mEnable)
            return;
        if(pEvent.getClickedBlock()==null)
            return;
        if(pEvent.getAction()!=Action.RIGHT_CLICK_BLOCK)
            return;
        Block clickBlock=pEvent.getClickedBlock();
//        if(clickBlock.getType()==Material.SIGN||clickBlock.getType()==Material.SIGN_POST||clickBlock.getType()==Material.WALL_SIGN){
        if(clickBlock.getType()==Material.SIGN||clickBlock.getType()==Material.WALL_SIGN){
            if(clickBlock.getState() instanceof Sign){
                Sign s=(Sign)clickBlock.getState();
                BSShop shop=getBossShopSign(s.getLine(0));
                if(shop==null)
                    return;

                if(!pEvent.getPlayer().hasPermission(this.mPlugin.getName()+".open.sign")){
                    Log.send(pEvent.getPlayer(),this.mPlugin.getLangManager().getNode("MsgNoPermitToOpenSignShop"));
                    return;
                }
                this.mPlugin.getManager(BSShopManager.class).openShop(pEvent.getPlayer(),shop);
                return;
            }
        }
    }

    @Override
    public void addDefaults(CommentedYamlConfig pConfig){}

}
