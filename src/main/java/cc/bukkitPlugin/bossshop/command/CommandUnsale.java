package cc.bukkitPlugin.bossshop.command;

import java.util.ArrayList;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSGoods;
import org.black_ixx.bossshop.managers.RecordManager;
import org.black_ixx.bossshop.managers.WorthHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.bossshop.sale.SaleManager;
import cc.bukkitPlugin.commons.plugin.command.TACommandBase;

public class CommandUnsale extends TACommandBase<BossShop,CommandExc>{

    public CommandUnsale(CommandExc exector){
        super(exector,1);
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(pArgs.length==0||pArgs.length==1&&pArgs[0].equalsIgnoreCase("help"))
            return help(pSender,pLabel);

        if(pArgs.length!=1)
            return help(pSender,pLabel);

        SaleManager tSaleMan=this.mPlugin.getManager(SaleManager.class);
        BSGoods tReward=tSaleMan.getSaleItem(pArgs[0]);
        if(tReward==null)
            return send(pSender,C("MsgNoSaleItemFound")+"["+pArgs[0]+"]");

        if(!tReward.hasOwner())
            return true;
        if(!hasCmdPermission(pSender,"user"))
            return noPermission(pSender,this.mLastConstructPermisson);
        boolean op=hasCmdPermission(pSender,"admin");
        if(pSender instanceof Player){
            Player tPlayer=(Player)pSender;
            if(!tReward.getOwner().equals(tPlayer.getUniqueId())&&!op)
                return send(pSender,C("MsgYouCannotUnsaleOther"));
            if(!op){
                RecordManager recordMan=this.mPlugin.getManager(RecordManager.class);
                int tCount=recordMan.getUnsaleRecord(tPlayer.getUniqueId());
                int tMaxCount=tSaleMan.getUnsaleCount();
                if(tCount>=tMaxCount)
                    return send(pSender,C("MsgYouTodayCannotUnsaleMore")+"("+tMaxCount+")");
                int tCost=tSaleMan.getUnSaleCost();
                WorthHandler wHandler=this.mPlugin.getManager(WorthHandler.class);
                if(tCost>0){
                    if(!wHandler.hasMoney(tPlayer,tCost))
                        return send(pSender,C("MsgYouNeed%%MoneyToUnsale").replace("%numb%",tCost+""));
                    wHandler.takeMoney(tPlayer,tCost);
                }
                recordMan.addUnsaleRecord(tPlayer.getUniqueId());
            }
        }
        tSaleMan.unsaleGoods(tReward);
        return send(pSender,C("MsgSuccessUnsaleItem")+"["+tReward.getName()+"]");
    }

    @Override
    public ArrayList<String> getHelp(CommandSender pSender,String pLabel){
        ArrayList<String> list=new ArrayList<>();
        if(hasCmdPermission(pSender,"admin")||hasCmdPermission(pSender,"user")){
            list.add(this.constructCmdUsage()+" <"+C("WordSaleID")+">");
            list.add(this.constructCmdDesc());
        }
        return list;
    }

    @Override
    public ArrayList<String> getTabSubCmd(CommandSender pSender,String pLabel,String[] pArgs){
        ArrayList<String> allSale=new ArrayList<>();
        if(pSender.hasPermission(this.mPluginName+".unsale.admin")){
            allSale.addAll(this.mPlugin.getManager(SaleManager.class).getSaleItemsName());
        }else if(pSender.hasPermission(this.mPluginName+".unsale.user")&&pSender instanceof Player){
            allSale.addAll(this.mPlugin.getManager(SaleManager.class).getSaleItemsName((Player)pSender));
        }
        return allSale;
    }

}
