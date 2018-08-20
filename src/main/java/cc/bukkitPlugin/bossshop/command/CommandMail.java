package cc.bukkitPlugin.bossshop.command;

import java.util.ArrayList;
import java.util.List;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.managers.WorthHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import cc.bukkitPlugin.bossshop.gui.GuiFactory;
import cc.bukkitPlugin.bossshop.mail.MailManager;
import cc.bukkitPlugin.commons.plugin.command.TACommandBase;
import cc.bukkitPlugin.commons.util.BukkitUtil;

public class CommandMail extends TACommandBase<BossShop,CommandExc>{

    private MailManager mMailMan;

    public CommandMail(CommandExc exector){
        super(exector,2);
        this.mSubCmd.add("check");
        this.mSubCmd.add("help");
        this.mSubCmd.add("recive");
        this.mSubCmd.add("send");
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(pArgs.length==1&&pArgs[0].equalsIgnoreCase("help")||(pArgs.length==0&&!(pSender instanceof Player)))
            return help(pSender,pLabel);

        if(!(pSender instanceof Player))
            return consoleNotAllow(pSender);
        Player tPlayer=(Player)pSender;
        String cmdLabel=pArgs.length==0?"gui":pArgs[0].toLowerCase();
        if(cmdLabel.equals("check")){
            if(!hasCmdPermission(pSender,"check"))
                return noPermission(pSender,this.mLastConstructPermisson);
            return this.checkMail(tPlayer,pArgs);
        }else if(cmdLabel.equals("recive")){
            if(!hasCmdPermission(pSender,"recive"))
                return noPermission(pSender,this.mLastConstructPermisson);
            return this.reciveMial(tPlayer,pArgs);
        }else if(cmdLabel.equals("send")){
            if(!hasCmdPermission(pSender,"send"))
                return noPermission(pSender,this.mLastConstructPermisson);
            return this.sendMail(tPlayer,pArgs);
        }else if(cmdLabel.equals("gui")){
            if(!hasCmdPermission(pSender,"gui"))
                return noPermission(pSender,this.mLastConstructPermisson);
            GuiFactory.getInstance().openMailGui(tPlayer);
            return true;
        }else return unknowChildCommand(pSender,pLabel,cmdLabel);
    }

    private MailManager getMailManager(){
        if(this.mMailMan==null)
            this.mMailMan=this.mPlugin.getManager(MailManager.class);
        return this.mMailMan;
    }

    public boolean checkMail(Player pPlayer,String[] pArgs){
        this.getMailManager().checkMail(pPlayer);
        return true;
    }

    public boolean reciveMial(Player pPlayer,String[] pArgs){
        MailManager tMailMan=this.getMailManager();
        if(pArgs.length==1){
            if(tMailMan.getMailCount(pPlayer.getName())<=0)
                return send(pPlayer,C("MsgYouHaveNoMail"));
            tMailMan.reciveMail(pPlayer,null);
        }else if(pArgs.length==2){
            if(!tMailMan.mailExist(pPlayer,pArgs[1]))
                return send(pPlayer,C("MsgMailNotExist").replace("%mail%",pArgs[1]));
            tMailMan.reciveMail(pPlayer,pArgs[1]);
        }
        return true;
    }

    public boolean undoMail(Player pPlayer,String[] pArgs){
        return false;
    }

    public boolean sendMail(Player pPlayer,String[] pArgs){
        if(pArgs.length<2)
            return send(pPlayer,C("MsgPleaseInputPlayerName"));
        OfflinePlayer tTarget=Bukkit.getOfflinePlayer(pArgs[1]);
        if(tTarget==null)
            return send(pPlayer,C("MsgNoPlayerFound")+"["+pArgs[1]+"]");
        if(pArgs[1].equalsIgnoreCase(pPlayer.getName())&&!pPlayer.isOp())
            return send(pPlayer,C("MsgYouCannotSendMailToYouSelf"));
        MailManager tMailM=this.getMailManager();
        if(tMailM.isMailFull(tTarget.getName()))
            return send(pPlayer,C("MsgPlayerMailIsFull").replace("%player%",tTarget.getName()));
        ItemStack tItem=pPlayer.getItemInHand();
        if(tItem==null||tItem.getType()==Material.AIR)
            return send(pPlayer,C("MsgYouShouldTakeItemInHand"));
        int tCost=tMailM.getMailSendCost();
        WorthHandler wHandler=this.mPlugin.getManager(WorthHandler.class);
        if(tCost>0){
            if(!wHandler.hasMoney(pPlayer,tCost))
                return send(pPlayer,C("MsgYouNeed%%MoneyToSendMail").replace("%numb%",tCost+""));
            else wHandler.takeMoney(pPlayer,tCost);
        }
        tMailM.addMail(pPlayer,tTarget,tItem);
        pPlayer.setItemInHand(null);
        return send(pPlayer,C("MsgMailHasSend"));
    }

    @Override
    protected void postSubCmdHelpWrite(CommandSender pSender,String pLabel,List<String> pHelps,String pSubCmd){
        if(pSubCmd.equalsIgnoreCase("send")){
            int tIndex=pHelps.size()-2;
            pHelps.set(tIndex,pHelps.get(tIndex)+" <"+C("WordPlayerName")+">");
        }
    }

    @Override
    public ArrayList<String> getTabSubCmd(CommandSender pSender,String pLabel,String[] pArgs){
        switch(pArgs.length){
        case 0:
        case 1:
            ArrayList<String> tTab=new ArrayList<>();
            tTab.addAll(this.mSubCmd);
            return tTab;
        case 2:
            if(pArgs[0].equalsIgnoreCase("send"))
                return BukkitUtil.getOnlinePlayersName();
            else if(pSender instanceof Player&&pArgs[0].equalsIgnoreCase("recive")){
                return this.mPlugin.getManager(MailManager.class).getPlayerMailsName(((Player)pSender).getName(),null);
            }
        default:
            return new ArrayList<>(0);
        }

    }

}
