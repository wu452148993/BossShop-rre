package cc.bukkitPlugin.bossshop.command;

import java.util.ArrayList;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.commons.plugin.command.TACommandBase;

public class CommandClear extends TACommandBase<BossShop,CommandExc>{

    public CommandClear(CommandExc exector){
        super(exector,0);
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(!hasCmdPermission(pSender))
            return noPermission(pSender,this.mLastConstructPermisson);

        if(pArgs.length==1&&pArgs[0].equalsIgnoreCase("help"))
            return help(pSender,pLabel);

        if(pArgs.length!=0)
            return false;

        int count=this.mExector.getPlugin().clearModelStatus();
        return send(pSender,C("MsgClearedModelStatus").replace("%numb%",count+""));
    }

    @Override
    public ArrayList<String> getHelp(CommandSender pSender,String pLabel){
        ArrayList<String> tList=new ArrayList<>();
        if(hasCmdPermission(pSender)){
            tList.add(this.constructCmdUsage());
            tList.add(this.mExector.getCmdDescPrefix()+C("HelpClearModelStatus"));
        }
        return tList;
    }

}
