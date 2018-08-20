package cc.bukkitPlugin.bossshop.command;

import java.util.ArrayList;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import cc.bukkitPlugin.bossshop.nbt.NBT;
import cc.bukkitPlugin.bossshop.nbt.NBTEditManager;
import cc.bukkitPlugin.commons.plugin.command.TACommandBase;

public class CommandNBT extends TACommandBase<BossShop,CommandExc>{

    public CommandNBT(CommandExc exector){
        super(exector,1);
        this.mSubCmd.add("add");
        this.mSubCmd.add("clear");
        this.mSubCmd.add("help");
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(pArgs.length==0||pArgs.length==1&&pArgs[0].equalsIgnoreCase("help"))
            return help(pSender,pLabel);

        String subLabel=pArgs[0];
        if(subLabel.equalsIgnoreCase("add")){
            if(!hasCmdPermission(pSender,"add"))
                return noPermission(pSender,this.mLastConstructPermisson);
            if(!(pSender instanceof Player))
                return this.consoleNotAllow(pSender);
            return this.add((Player)pSender,pArgs);
        }else if(subLabel.equalsIgnoreCase("clear")){
            if(!hasCmdPermission(pSender,"clear"))
                return noPermission(pSender,this.mLastConstructPermisson);
            return this.clear(pSender,pArgs);
        }else return unknowChildCommand(pSender,pLabel,subLabel);
    }

    private boolean add(Player pPlayer,String pArgs[]){
        if(pArgs.length!=1)
            return this.errorArgsNumber(pPlayer,pArgs.length);
        ItemStack item=pPlayer.getItemInHand();
        if(item==null||item.getType()==Material.AIR){
            this.send(pPlayer,this.C("MsgYouShouldTakeItemInHand"));
            return true;
        }
        NBTEditManager NBTMan=this.mPlugin.getManager(NBTEditManager.class);
        NBT tNBT=NBTMan.getNBTByItem(item);
        if(tNBT!=null){
            this.send(pPlayer,this.C("MsgNBTHasExistInStockAndItsID")+tNBT.mLabel);
            return true;
        }
        String label=NBTMan.addItemNBTToStock(item,false);
        if(label==null)
            this.send(pPlayer,this.C("MsgItemMayDonotHaveNBT"));
        else this.send(pPlayer,this.C("MsgNBTHaveAddAndItsID")+label);
        return true;
    }

    private boolean clear(CommandSender sender,String pArgs[]){
        if(pArgs.length!=1)
            return errorArgsNumber(sender,pArgs.length);
        this.mPlugin.getManager(NBTEditManager.class).clearNBT(sender);
        return true;
    }

    @Override
    public ArrayList<String> getTabSubCmd(CommandSender pSender,String pLabel,String[] pArgs){
        ArrayList<String> tTab=new ArrayList<>(this.mSubCmd.size());
        tTab.addAll(this.mSubCmd);
        return tTab;
    }

}
