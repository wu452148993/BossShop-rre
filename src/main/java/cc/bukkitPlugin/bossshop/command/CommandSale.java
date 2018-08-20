package cc.bukkitPlugin.bossshop.command;

import java.util.ArrayList;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.bossshop.sale.SaleManager;
import cc.bukkitPlugin.commons.plugin.command.TACommandBase;

public class CommandSale extends TACommandBase<BossShop,CommandExc>{

    public CommandSale(CommandExc exector){
        super(exector,5);
        this.mSubCmd.add("start");
        this.mSubCmd.add("stop");
        this.mSubCmd.add("item");
        this.mSubCmd.add("money");
        this.mSubCmd.add("points");
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(!hasCmdPermission(pSender,"forsale"))
            return noPermission(pSender,this.mLastConstructPermisson);

        if(pArgs.length==0||pArgs.length==1&&pArgs[0].equalsIgnoreCase("help"))
            return help(pSender,pLabel);

        if(!(pSender instanceof Player))
            return this.consoleNotAllow(pSender);
        Player tPlayer=(Player)pSender;
        if(pArgs.length==1){
            String subLabel=pArgs[0];
            if(subLabel.equalsIgnoreCase("help"))
                return help(pSender,pLabel);
            else if(subLabel.equalsIgnoreCase("start")){
                this.mPlugin.getSaleListener().startSaleListenrer(tPlayer);
                return true;
            }else if(subLabel.equalsIgnoreCase("stop")){
                this.mPlugin.getSaleListener().stopSaleListenrer(tPlayer);
                return true;
            }else return unknowChildCommand(pSender,pLabel,subLabel);
        }else if(pArgs.length==5){
            this.mPlugin.getManager(SaleManager.class).sale((Player)pSender,pArgs);
            return true;
        }
        return this.help(pSender,pLabel);
    }

    @Override
    public ArrayList<String> getHelp(CommandSender sender,String pLabel){
        ArrayList<String> list=new ArrayList<>();
        if(sender.hasPermission(this.mPluginName+".sale.forsale")){
            list.add(this.mMainCmdLabel+" sale start");
            list.add(this.C("HelpStartSaleWithChat"));
            list.add(this.mMainCmdLabel+" sale stop");
            list.add(this.C("HelpStopSaleWithChat"));
            list.add(this.mMainCmdLabel+" sale <"+C("WordSellType")+"> <"+C("WordSigleNumb")+"> <"+C("WordPartNumb")+"> <"+C("WordPriceType")+"> <"+C("WordPrice")+">");
            list.add(this.C("HelpSaleForsale"));
            list.add(this.C("HelpSaleSellType"));
            list.add(this.C("HelpSalePriceType"));
            list.add(this.C("HelpSaleOther"));
        }
        return list;
    }

    @Override
    public ArrayList<String> getTabSubCmd(CommandSender pSender,String pLabel,String[] pArgs){
        ArrayList<String> mayCmd=new ArrayList<>();
        switch(pArgs.length){
        case 1:
            mayCmd.addAll(this.mSubCmd);
        case 2:
            mayCmd.add("1");
            mayCmd.add("10");
            mayCmd.add("64");
        case 3:
            mayCmd.add("1");
            mayCmd.add("10");
        case 4:
            mayCmd.add("money");
            mayCmd.add("points");
        case 5:
        }
        return mayCmd;
    }
}
