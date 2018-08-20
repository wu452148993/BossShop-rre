package cc.bukkitPlugin.bossshop.command;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSShopManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.plugin.command.TACommandBase;
import cc.bukkitPlugin.commons.plugin.command.TCommandExc;
import cc.bukkitPlugin.commons.plugin.command.TCommandHelp;

public class CommandExc extends TCommandExc<BossShop> implements CommandExecutor,TabCompleter{

    /**
     * 必须在配置文件启用后才能调用此方法
     * 
     * @param pPlugin
     */
    public CommandExc(BossShop pPlugin){
        super(pPlugin,"shop",true);
        this.registerSubCommand();
    }

    @Override
    protected void registerSubCommand(){
        this.register(new TCommandHelp<BossShop,CommandExc>(this));

        this.register(new CommandClear(this));
        this.register(new CommandClose(this));
        this.register(new CommandName(this));
        this.register(new CommandMail(this));
        this.register(new CommandNBT(this));
        this.register(new CommandOpen(this));
        this.register(new CommandReload(this));
        this.register(new CommandSale(this));
        this.register(new CommandUnsale(this));
    }

    @Override
    public boolean onCommand(CommandSender pSender,Command command,String pLabel,String[] pArgs){
        if(pSender instanceof Player){
            TACommandBase<BossShop,?> openCmd=this.getCommand(CommandOpen.class);
            if(pArgs.length==0){
                pArgs=new String[]{openCmd.getCommandLabel(),this.mPlugin.getConfigManager().getMainShop()};
            }else if(pArgs.length==1){
                // 是商店且不是命令
                if(this.getCommand(pArgs[0])==null&&this.mPlugin.getManager(BSShopManager.class).getShop(pArgs[0])!=null){
                    pArgs=new String[]{openCmd.getCommandLabel(),pArgs[0]};
                }
            }
        }
        return super.onCommand(pSender,command,pLabel,pArgs);
    }

}
