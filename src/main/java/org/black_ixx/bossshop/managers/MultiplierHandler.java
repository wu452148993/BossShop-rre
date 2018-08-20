package org.black_ixx.bossshop.managers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.PriceType;
import org.black_ixx.bossshop.core.BSMultiplier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.plugin.manager.AManager;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.IConfigModel;
import cc.commons.commentedyaml.CommentedYamlConfig;

public class MultiplierHandler extends AManager<BossShop> implements IConfigModel{

    private Set<BSMultiplier> multipliers=new HashSet<BSMultiplier>();
    private boolean mEnable=false;

    /**
     * 会员价格系统
     * @param plugin
     */
    public MultiplierHandler(BossShop plugin){
        super(plugin);

        this.mPlugin.getConfigManager().registerConfigModel(this);
    }

    @Override
    public void setConfig(CommandSender pSender,CommentedYamlConfig pConfig){
        CommentedYamlConfig tConfig=this.mPlugin.getConfigManager().getConfig();
        if(!(this.mEnable=tConfig.getBoolean("MultiplierGroups.Enabled")))
            return;
        List<String> lines=tConfig.getStringList("MultiplierGroups.List");
        this.multipliers.clear();
        for(String sLine : lines){
            BSMultiplier m=new BSMultiplier(this.mPlugin,sLine);
            if(m.isValid()){
                multipliers.add(m);
            }
        }
    }

    /**
     * 计算玩家购买该商品的折扣
     * @param p 玩家
     * @param type 商品类型
     * @return 商品折扣
     */
    public double calculateWithMultiplier(Player p,PriceType type){
        if(!this.mEnable)
            return 1D;
        double discount=1D;
        for(BSMultiplier m : multipliers){
            if(m.getType()==type){
                if(m.hasPermission(p)){
                    discount*=m.getMultiplier();
                }
            }
        }
        return discount;
    }

    public Set<BSMultiplier> getMultipliers(){
        return multipliers;
    }

    @Override
    public void addDefaults(CommentedYamlConfig pConfig){

    }

}
