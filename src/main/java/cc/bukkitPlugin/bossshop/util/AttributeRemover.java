package cc.bukkitPlugin.bossshop.util;

import java.util.Collection;
import java.util.Map;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.commons.util.reflect.ClassUtil;

public class AttributeRemover{

    private static boolean useItemFlags;
    private static boolean useReflection;

    public static boolean init(){

        if(ClassUtil.isClassLoaded("org.bukkit.inventory.ItemFlag")){
            useItemFlags=true;
        }else{
            Log.warn("此服务器版本"+NMSUtil.getServerVersion()+"可能无法启用属性移除");
            useReflection=true;
        }
        return true;
    }

    public static ItemStack hideAttributes(ItemStack tItem,int mFlags){
        if(tItem==null)
            return null;
        mFlags=mFlags&0xFFFFFFFF;
        if(useItemFlags){
            ItemMeta tMeta=tItem.getItemMeta();
            for(ItemFlag sFlag : ItemFlag.values()){
                int tPos=sFlag.ordinal();
                int tValue=((1<<tPos)&mFlags)>>tPos;
                if(tValue==1){
                    tMeta.addItemFlags(sFlag);
                }else{
                    tMeta.removeItemFlags(sFlag);
                }
                //removeItemFlags
            }
            if(!BukkitUtil.isItemMetaEmpty(tMeta))
                tItem.setItemMeta(tMeta);
            return tItem;
        }else if(useReflection&&mFlags>0){
            try{
                Object tNMSItem=NMSUtil.getNMSItem(tItem);
                if(tNMSItem==null)
                    return tItem;
                Object tNBTTag=NBTUtil.getItemNBT_NMS(tNMSItem);
                Object nbtList=ClassUtil.newInstance(NBTUtil.clazz_NBTTagList);
                Map<String,Object> tTagMap=NBTUtil.getNBTTagCompoundValue(tNBTTag);
                tTagMap.put("AttributeModifiers",nbtList);
                return NMSUtil.getCBTItem(tNMSItem);
            }catch(Exception e){
            }
        }
        return tItem;
    }

    private static boolean isNullOrEmpty(Collection<?> coll){
        return coll==null||coll.isEmpty();
    }

}
