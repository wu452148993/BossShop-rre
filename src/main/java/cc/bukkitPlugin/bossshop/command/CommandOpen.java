package cc.bukkitPlugin.bossshop.command;

import java.util.ArrayList;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSShop;
import org.black_ixx.bossshop.core.BSShopManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.plugin.command.TACommandBase;
import cc.bukkitPlugin.commons.util.BukkitUtil;

public class CommandOpen extends TACommandBase<BossShop,CommandExc>{

    public CommandOpen(CommandExc exector){
        super(exector,2);
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(pArgs.length==0||pArgs.length==1&&pArgs[0].equalsIgnoreCase("help"))
            return help(pSender,pLabel);

        BSShopManager shopMan=this.mPlugin.getManager(BSShopManager.class);
        if(pArgs.length==1){
            if(!pSender.hasPermission(this.mPluginName+".open.command"))
                return this.noPermission(pSender);
            if(!(pSender instanceof Player))
                return this.consoleNotAllow(pSender);
            Player tPlayer=(Player)pSender;
            BSShop tshop=shopMan.getShop(pArgs[0]);
            if(tshop==null)
                return this.send(pSender,this.C("MsgNoShopFound")+" ["+pArgs[0]+"]");
            this.openShop(tPlayer,tshop);
        }else if(pArgs.length==2){
            Player tPlayer=Bukkit.getPlayer(pArgs[0]);
            if(tPlayer==null)
                return this.send(pSender,this.C("MsgNoPlayerFound")+" ["+pArgs[0]+"]");
            boolean self=pSender.getName().equalsIgnoreCase(pArgs[0]);
            if(!self&&!pSender.hasPermission(this.mPluginName+".open.other"))
                return this.noPermission(pSender);
            if(!pSender.hasPermission(this.mPluginName+".open.command"))
                return this.noPermission(pSender);
            BSShop tshop=shopMan.getShop(pArgs[1]);
            if(tshop==null)
                return this.send(pSender,this.C("MsgNoShopFound")+" ["+pArgs[1]+"]");
            this.openShop(tPlayer,tshop);
            if(!self)
                this.send(pSender,this.C("MsgOpenShopForPlayer","%player%",tshop.getShopName()));
        }else return errorArgsNumber(pSender,pArgs.length);
        return true;
    }

    @Override
    public ArrayList<String> getHelp(CommandSender sender,String pLabel){
        ArrayList<String> list=new ArrayList<>();
        if(sender.hasPermission(this.mPluginName+".open.command")){
            list.add(this.mMainCmdLabel+" [open] ["+C("WordShopName")+"]");
            list.add(this.C("HelpOpenShop"));
        }
        if(sender.hasPermission(this.mPluginName+".open.other")){
            list.add(this.mMainCmdLabel+" open <"+C("WordPlayerName")+"> <"+C("WordShopName")+">");
            list.add(this.C("HelpOpenOtherShop"));
        }
        return list;
    }

    private void openShop(final Player pPlayer,final BSShop pShop){
        int tDelay=this.mPlugin.getConfigManager().getShopOpenDelay();
        final BSShopManager shopMan=this.mPlugin.getManager(BSShopManager.class);
        if(tDelay<=0){
            shopMan.openShop(pPlayer,pShop);
        }else Bukkit.getScheduler().scheduleSyncDelayedTask(this.mPlugin,new Runnable(){

            @Override
            public void run(){
                shopMan.openShop(pPlayer,pShop);
            }
        },tDelay);

    }

    @Override
    public ArrayList<String> getTabSubCmd(CommandSender pSender,String pLabel,String[] pArgs){
        ArrayList<String> mayCmd=new ArrayList<>();
        switch(pArgs.length){
        case 1:
            mayCmd.addAll(this.mPlugin.getManager(BSShopManager.class).getShops().keySet());
            if(pSender.hasPermission(this.mPluginName+".open.other"))
                mayCmd.addAll(BukkitUtil.getOnlinePlayersName());
            return mayCmd;
        case 2:
            mayCmd.addAll(this.mPlugin.getManager(BSShopManager.class).getShops().keySet());
            return mayCmd;
        }
        return super.getTabSubCmd(pSender,pLabel,pArgs);
    }

}
