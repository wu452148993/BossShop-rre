package cc.bukkitPlugin.bossshop.gui;

import java.util.HashMap;

import org.bukkit.inventory.ItemStack;

public enum GuiType{

    Mail("MailGui");

    public final String mYamlKey;
    /** 材质默认物品 */
    ItemStack mDefTexture=null;
    /** 指定Slot上的物品 */
    HashMap<Integer,ItemStack> mSlotTexture=new HashMap<>();

    private GuiType(String pYamlKey){
        this.mYamlKey=pYamlKey;
    }

}
