package cc.bukkitPlugin.bossshop.command;

import java.util.ArrayList;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSShopHolder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import cc.bukkitPlugin.commons.plugin.command.TACommandBase;
import cc.bukkitPlugin.commons.util.BukkitUtil;

public class CommandClose extends TACommandBase<BossShop,CommandExc>{

    public CommandClose(CommandExc exector){
        super(exector,1);
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        //bs close <player>
        if(!hasCmdPermission(pSender,"other"))
            return noPermission(pSender,this.mLastConstructPermisson);

        if(pArgs.length==0||(pArgs.length==1&&pArgs[0].equalsIgnoreCase("help")&&Bukkit.getPlayer("help")==null))
            return help(pSender,pLabel);

        Player tPlayer=Bukkit.getPlayer(pArgs[0]);
        if(tPlayer==null)
            return send(pSender,C("MsgNoPlayerFound"));
        if(!tPlayer.isOnline())
            return send(pSender,C("MsgPlayerOffline"));
        Inventory inv=tPlayer.getOpenInventory().getTopInventory();
        if(!(inv.getHolder() instanceof BSShopHolder))
            return send(pSender,C("MsgPlayerNotOpenBSShop"));
        tPlayer.closeInventory();
        if(!tPlayer.getName().equals(pSender.getName()))
            send(pSender,C("MsgAlreadyClosePlayerShop"));
        return true;
    }

    @Override
    public ArrayList<String> getHelp(CommandSender pSender,String pLabel){
        ArrayList<String> list=new ArrayList<>();
        if(pSender.hasPermission(this.mPluginName+".close.other")){
            list.add(constructCmdUsage()+" <"+C("WordPlayerName")+">");
            list.add(this.mExector.getCmdDescPrefix()+C("HelpClosePlayerShop"));
        }
        return list;
    }

    @Override
    public ArrayList<String> getTabSubCmd(CommandSender pSender,String pLabel,String[] pArgs){
        return BukkitUtil.getOnlinePlayersName();
    }

}
