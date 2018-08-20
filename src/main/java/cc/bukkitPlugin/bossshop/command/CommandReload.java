package cc.bukkitPlugin.bossshop.command;

import java.util.ArrayList;
import java.util.List;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSShop;
import org.black_ixx.bossshop.core.BSShopManager;
import org.black_ixx.bossshop.managers.ItemNameManager;
import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.bossshop.gui.GuiFactory;
import cc.bukkitPlugin.bossshop.mail.MailManager;
import cc.bukkitPlugin.commons.plugin.command.TCommandReload;

public class CommandReload extends TCommandReload<BossShop,CommandExc>{

    public CommandReload(CommandExc exector){
        super(exector,2);
        this.mSubCmd.add("itemName");
        this.mSubCmd.add("shop");
        this.mSubCmd.add("mail");
        this.mSubCmd.add("gui");
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(pArgs.length==1&&pArgs[0].equalsIgnoreCase("help"))
            return help(pSender,pLabel);

        String tCmdLabel=pArgs.length>0?pArgs[0].toLowerCase():"plugin";
        if(this.mSubCmd.contains(tCmdLabel)){
            if(!hasCmdPermission(pSender,tCmdLabel))
                return noPermission(pSender,this.mLastConstructPermisson);
        }else return unknowChildCommand(pSender,pLabel,tCmdLabel);
        
        if(tCmdLabel.equalsIgnoreCase("itemName")){
            this.mPlugin.getManager(ItemNameManager.class).reloadConfig(pSender);
        }else if(tCmdLabel.equalsIgnoreCase("shop")){
            this.reloadShop(pSender,pArgs);
        }else if(tCmdLabel.equalsIgnoreCase("mail")){
            this.mPlugin.getManager(MailManager.class).reloadConfig(pSender);
            return send(pSender,C("MsgMailAlreadyReload"));
        }else if(tCmdLabel.equalsIgnoreCase("gui")){
            GuiFactory.getInstance().reloadConfig(pSender);
            return send(pSender,C("MsgGuiAlreadyReload"));
        }else return super.execute(pSender,pLabel,pArgs);
        return true;
    }

    @Override
    protected void postSubCmdHelpWrite(CommandSender pSender,String pLabel,List<String> pHelps,String pSubCmd){
        if(pSubCmd.equalsIgnoreCase("shop")){
            int index=pHelps.size()-2;
            pHelps.set(index,pHelps.get(index)+" ["+C("WordShopName")+"]");
        }
    }

    private boolean reloadShop(final CommandSender pSender,final String[] pArgs){
        if(pArgs.length<1||pArgs.length>2)
            return errorArgsNumber(pSender,pArgs.length);
        BSShopManager shopMan=CommandReload.this.mPlugin.getManager(BSShopManager.class);
        if(pArgs.length==1){
            shopMan.reloadConfig(pSender);
            return true;
        }else{
            BSShop tShop=shopMan.getShop(pArgs[1]);
            if(tShop==null)
                return send(pSender,C("MsgNoShopFound")+"["+pArgs[1]+"]");
            if(!tShop.reloadShop(pSender))
                shopMan.removeShop(tShop);
            tShop.updateAllInventory(true);
            return send(pSender,C("MsgAlreadyReloadShop").replace("%shop%",tShop.getShopName()));
        }
    }

    @Override
    public ArrayList<String> getTabSubCmd(CommandSender pSender,String pLabel,String[] pArgs){
        ArrayList<String> mayCmd=new ArrayList<>();
        switch(pArgs.length){
        case 1:
            mayCmd.addAll(this.mSubCmd);
            break;
        case 2:
            if(pArgs[0].equalsIgnoreCase("shop"))
                mayCmd.addAll(this.mPlugin.getManager(BSShopManager.class).getShops().keySet());
            break;
        }
        return mayCmd;
    }

}
