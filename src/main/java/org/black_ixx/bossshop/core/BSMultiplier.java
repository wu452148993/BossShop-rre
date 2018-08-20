package org.black_ixx.bossshop.core;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.PriceType;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;
import cc.commons.util.ToolKit;

public class BSMultiplier{

    private String mPermission="Permission.Node";
    private PriceType mPriceType=PriceType.Nothing;
    private double mMultiplier=1.0;

    public BSMultiplier(BossShop pPlugin,String config_line){
        String[] parts=config_line.split(":",3);
        if(parts.length!=3){
            Log.severe("Invalid Multiplier Group Line... \""+config_line+"\"! It should look like this: \"Permission.Node:<type>:<multiplier>\"");
            return;
        }
        String permission=parts[0].trim();
        if(parts[1].trim().equalsIgnoreCase("<type>")){
            return;
        }
        PriceType tType=ToolKit.getElement(PriceType.values(),parts[1].trim());

        if(tType==null||tType==PriceType.Item||tType==PriceType.Nothing||tType==PriceType.Free){
            Log.severe("Invalid Multiplier Group Line... \""+config_line+"\"! It should look like this: \"Permission.Node:<type>:<multiplier>\". '"+parts[1].trim()+"' is no valid PriceType... you can use: 'Money', 'Points' and 'EXP'!");
            return;
        }
        double multiplier=1.0;
        try{
            multiplier=Double.parseDouble(parts[2].trim());
        }catch(Exception e){
            Log.severe("Invalid Multiplier Group Line... \""+config_line+"\"! It should look like this: \"Permission.Node:<type>:<multiplier>\". '"+parts[2].trim()+"' is no valid multiplier... What you can use instead (examples): 0.25, 0.3, 0.75, 1.0, 1.5, 2.0 etc.!");
            return;
        }
        setup(permission,tType,multiplier);
    }

    public BSMultiplier(String permission,PriceType type,double multiplier){
        setup(permission,type,multiplier);
    }

    public void setup(String permission,PriceType type,double multiplier){
        this.mPermission=permission;
        this.mPriceType=type;
        this.mMultiplier=multiplier;
    }

    public boolean isValid(){
        return mPriceType!=PriceType.Nothing;
    }

    public PriceType getType(){
        return mPriceType;
    }

    public double getMultiplier(){
        return mMultiplier;
    }

    public String getPermission(){
        return mPermission;
    }

    public boolean hasPermission(Player p){
        return p.hasPermission(mPermission);
    }

    public double calculateWithMultiplier(double d){
        return d*mMultiplier;
    }

    public int calculateWithMultiplier(int d){
        return (int)(d*mMultiplier);
    }

}
