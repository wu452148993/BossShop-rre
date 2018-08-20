package cc.bukkitPlugin.bossshop.command;

import java.util.ArrayList;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.managers.ItemNameManager;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import cc.bukkitPlugin.commons.plugin.command.TACommandBase;

public class CommandName extends TACommandBase<BossShop,CommandExc>{

    public CommandName(CommandExc exector){
        super(exector,"name");
        this.mSubCmd.add("key");
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(!pSender.isOp())
            return true;
        if(!(pSender instanceof Player))
            return consoleNotAllow(pSender);
        Player tPlayer=(Player)pSender;
        ItemStack inHandItem=tPlayer.getItemInHand();
        if(inHandItem==null||inHandItem.getType()==Material.AIR)
            return send(pSender,this.C("MsgYouShouldTakeItemInHand"));
        if(pArgs.length==0)
            return send(pSender,">"+this.mPlugin.getManager(ItemNameManager.class).getName(inHandItem));
        else if(pArgs[0].equalsIgnoreCase("key"))
            return send(pSender,">"+this.mPlugin.getManager(ItemNameManager.class).getUnlocalizedName(inHandItem));
        return true;
    }

    @Override
    public ArrayList<String> getHelp(CommandSender pSender,String pLabel){
        return new ArrayList<>(0);
    }

    @Override
    public ArrayList<String> getTabSubCmd(CommandSender pSender,String pLabel,String[] pArgs){
        ArrayList<String> tTab=new ArrayList<>(this.mSubCmd.size());
        tTab.addAll(this.mSubCmd);
        return tTab;
    }

}
