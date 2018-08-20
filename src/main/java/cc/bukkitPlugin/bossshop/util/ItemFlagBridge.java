package cc.bukkitPlugin.bossshop.util;

import java.util.Arrays;
import java.util.List;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import cc.bukkitPlugin.commons.Log;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.util.ToolKit;

public class ItemFlagBridge{

    public static void loadItemFlag(ItemMeta pMeta,String pSecPath,List<String> pFlagStrs){
        ItemFlag tFlag=null;
        for(String sKey : pFlagStrs){
            if((tFlag=ToolKit.getElement(ItemFlag.values(),sKey.trim()))!=null){
                pMeta.addItemFlags(tFlag);
            }else{
                Log.info("§e节点 "+pSecPath+" 的 HideItemFlag 配置存在错误,标志 "+sKey+" 不存在");
            }
        }
    }
    
    public static void addFlagTemplate(CommentedSection pSection){
        pSection.addDefault("HideItemFlag",new String[]{"HIDE_ENCHANTS"},"物品信息隐藏设置,可以不设置","可用值: "+Arrays.toString(ItemFlag.values()));
    }
    
}
