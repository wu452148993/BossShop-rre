package org.black_ixx.bossshop.api;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import cc.commons.util.StringUtil;

public abstract class BossShopAddon extends JavaPlugin{

    private BossShop mPlugin;
    private boolean mEnable=false;

    @Override
    public void onEnable(){
        mEnable=false;
        Plugin plugin=Bukkit.getPluginManager().getPlugin("BossShop");
        if(plugin==null){
            printSevere("BossShop 未找到... 你需要它来运行 "+getAddonName()+"! Get it here: http://dev.bukkit.org/bukkit-plugins/bossshop/. Version v"+getRequiredBossShopVersion()+" or newer is required!");
            printInfo("关闭附属...");
            mEnable=true;
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        mPlugin=(BossShop)plugin;
        double bs_worth=getWorth(mPlugin.getDescription().getVersion());
        double ao_worth=getWorth(getRequiredBossShopVersion());
        if(bs_worth<ao_worth){
            printSevere("BossShop was found but it seems to be outdated... you need v"+getRequiredBossShopVersion()+" or newer in order to run "+getAddonName()+"! Get it here: http://dev.bukkit.org/bukkit-plugins/bossshop/");
            printInfo("Disabling Addon...");
            mEnable=true;
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        mPlugin.getAPI().addEnabledAddon(this);
        Bukkit.getLogger().info("[BossShop] Enabling Addon "+getAddonName());
        enableAddon();
    }

    @Override
    public void onDisable(){
        if(mEnable){
            return;
        }
        Bukkit.getLogger().info("[BossShop] Disabling Addon "+getAddonName());
        disableAddon();
    }

    public void printSevere(String msg){
        Bukkit.getLogger().severe("["+getAddonName()+"] "+msg);
    }

    public void printWarning(String msg){
        Bukkit.getLogger().warning("["+getAddonName()+"] "+msg);
    }

    public void printInfo(String msg){
        Bukkit.getLogger().info("["+getAddonName()+"] "+msg);
    }

    public final BossShop getBossShop(){
        return mPlugin;
    }

    protected double getWorth(String s){
        try{
            if(StringUtil.isEmpty(s)||s.length()<1){
                return 0;
            }
            double x=0;
            String[] parts=s.replace(".",":").split(":");
            x+=Integer.parseInt(parts[0].trim());
            if(parts.length==2){
                x+=0.1*Integer.parseInt(parts[1].trim());
            }
            if(parts.length==3){
                x+=0.1*Integer.parseInt(parts[1].trim());
                x+=0.01*Integer.parseInt(parts[2].trim());
            }
            return x;
        }catch(Exception e){
            printWarning("Was not able to get the version of "+s);
            return 1.00;
        }
    }

    public abstract String getAddonName();

    public abstract String getRequiredBossShopVersion();

    public abstract void enableAddon();

    public abstract void disableAddon();

    public abstract void bossShopReloaded(CommandSender sender);
}
