package cc.bukkitPlugin.bossshop.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTSerializer;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.exception.NBTDeserializeException;
import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.util.ByteUtil;
import cc.commons.util.StringUtil;
import cc.commons.util.ToolKit;
import cc.commons.util.reflect.ClassUtil;

public class ItemLoader{

    public static final boolean HAS_ITEMFLAG=ClassUtil.isClassLoaded("org.bukkit.inventory.ItemFlag");

    /**
     * 载入物品
     * 
     * @param pSecPath
     *            配置节点位置
     * @param pValue
     *            物品信息节点为物品信息字符串
     * @param pDef
     *            默认物品
     * @return 载入的物品,如果载入失败,返回默认物品
     */
    public static ItemStack loadItem(String pSecPath,Object pValue,ItemStack pDef){
        if(pValue==null)
            return pDef;

        ItemStack tLoad=null;
        if(pValue instanceof CommentedSection){
            CommentedSection tSec=(CommentedSection)pValue;
            tLoad=ItemLoader.loadItem(pSecPath+"."+tSec.getName(),tSec);
        }else{
            String tValue=String.valueOf(pValue);
            if(tValue.equalsIgnoreCase("default")){
                return pDef;
            }
            tLoad=ItemLoader.loadItem(pSecPath,tValue);
        }

        return tLoad==null?pDef:tLoad;
    }

    /**
     * 从字符串中载入物品
     * 
     * @param pSecPath
     *            配置节点位置
     * @param pSection
     *            物品信息节点
     * @return 载入的物品或null
     */
    public static ItemStack loadItem(String pSecPath,CommentedSection pSection){
        ItemStack tItem=ItemLoader.loadItemOrType(pSection.getString("ItemType"));
        if(tItem==null){
            Log.info("§c节点 "+pSecPath+" 的 ItemType 配置错误或不存在");
            return null;
        }
        tItem.setDurability(pSection.getShort("ItemDamage",(short)0));
        tItem.setAmount(Math.abs(pSection.getInt("ItemAmount",1)));

        String tNBTBase64=pSection.getString("ItemNBT");
        if(!StringUtil.isEmpty(tNBTBase64)){
            try{
                Object tDeserializeNBT=NBTSerializer.deserializeNBTFromByte(ByteUtil.base64ToByte(tNBTBase64));
                Object tNBTExist=NBTUtil.getItemNBT(tItem);
                if(tNBTExist!=null){
                    tNBTExist=NBTUtil.mixNBT(tNBTExist,tDeserializeNBT,false);
                }else{
                    tNBTExist=tDeserializeNBT;
                }
                ItemStack ti=NBTUtil.setItemNBT(tItem,tNBTExist);
                if(ti!=null){
                    tItem=ti;
                }
            }catch(NBTDeserializeException|IllegalArgumentException exp){
                Log.severe("反序列化节点 "+pSecPath+" 的NBT时发生了错误",exp);
                return null;
            }
        }

        ItemMeta tMeta=tItem.getItemMeta();
        String tProp=pSection.getString("ItemName");
        if(StringUtil.isNotEmpty(tProp)){
            tMeta.setDisplayName(Log.color(tProp));
        }
        List<String> tLores=pSection.getStringList("ItemLore");
        if(!tLores.isEmpty()){
            List<String> tNewLores=new ArrayList<>(tLores.size());
            for(String sLore : tLores){
                tNewLores.add(Log.color(sLore));
            }
            tMeta.setLore(tNewLores);
        }
        CommentedSection tSecEnchant=pSection.getSection("ItemEnchant");
        if(tSecEnchant!=null){
            Enchantment tEnchant=null;
            for(String sKey : tSecEnchant.getKeys(false)){
                if((tEnchant=BukkitUtil.getEnchantment(sKey))!=null){
                    tMeta.addEnchant(tEnchant,tSecEnchant.getInt(sKey,1),true);
                }else{
                    Log.info("§e节点 "+pSecPath+" 的 ItemEnchant 配置存在错误,附魔 "+sKey+" 不存在");
                }
            }
        }

        if(HAS_ITEMFLAG){
            ItemFlagBridge.loadItemFlag(tMeta,pSecPath,pSection.getStringList("HideItemFlag"));
        }

        tItem.setItemMeta(tMeta);

        if(pSection.getBoolean("Unbreakable")){
            try{
                Object tNMSItem=NMSUtil.getNMSItem(tItem);
                Object tNBTTag=NBTUtil.getItemNBT_NMS(tNMSItem);
                NBTUtil.invokeNBTTagCompound_set(tNBTTag,"Unbreakable",NBTUtil.newNBTTagByte((byte)1));
                tItem=NBTUtil.setItemNBT(tItem,tNBTTag);
            }catch(Throwable exp){
                Log.severe("为配置节点 "+pSecPath+" 的物品设置无法破坏NBT时发生了错误",exp);
            }
        }
        return tItem;
    }

    /**
     * 从字符串中载入物品
     * 
     * @param pSecPath
     *            配置节点位置
     * @param pInfo
     *            物品信息字符串
     * @return 载入的物品或null
     */
    public static ItemStack loadItem(String pSecPath,String pInfo){
        if(StringUtil.isBlank(pInfo))
            return null;

        String[] tProps=pInfo.split(";",4);
        ItemStack tItem=ItemLoader.loadItemOrType(tProps[0]);
        if(tItem==null){
            Log.info("§c节点 "+pSecPath+" 的 物品类型 配置错误或不存在");
            return null;
        }
        if(tProps.length>1){
            tItem.setDurability((short)ToolKit.paseIntOrDefault(tProps[1],0));
        }
        if(tProps.length>2){
            tItem.setAmount(Math.abs(ToolKit.paseIntOrDefault(tProps[2],1)));
        }
        if(tProps.length>3){
            ItemMeta tMeta=tItem.getItemMeta();
            tMeta.setDisplayName(Log.color(tProps[3]));
            tItem.setItemMeta(tMeta);
        }

        return tItem;
    }

    /** 根据类型字符串载入物品或者物品类型 */
    public static ItemStack loadItemOrType(String pStr){
        if(StringUtil.isBlank(pStr))
            return null;
        ItemStack tItem=null;
        pStr=pStr.trim();
        Material tMate=BukkitUtil.getItemType(pStr);
        if(tMate!=null){
            tItem=new ItemStack(tMate,1,(byte)0);
        }
        return tItem;
    }

    public static void addItemTemplate(CommentedSection pSection){
        CommentedSection tSecItem=pSection.getOrCreateSection("ComplexItem","第一种物品格式配置");
        ArrayList<String> tComments=new ArrayList<>();
        tSecItem.addDefault("ItemType","STONE","物品类型");
        tSecItem.addDefault("ItemDamage",0,"物品子id,可以不填,如果未设置则默认为0");
        tSecItem.addDefault("ItemAmount",1,"物品数量,可以不填,如果未设置则默认为1");
        tSecItem.addDefault("ItemName","§2复杂物品模板","物品名字,可以不设置");
        tSecItem.addDefault("ItemLore",new String[]{"","§2第一条lore","§2第二条lore"},"物品Lore,可以不设置");
        HashMap<String,Integer> tEnchants=new HashMap<>();
        tEnchants.put("DAMAGE_ALL",4);
        tSecItem.addDefault("ItemEnchant",tEnchants,"物品附魔,可以不设置");
        if(HAS_ITEMFLAG){
            ItemFlagBridge.addFlagTemplate(tSecItem);
        }
        tSecItem.addDefault("ItemNBT","","物品NBT,与商店物品配置的RawNBT相同");
        tSecItem.addDefault("Unbreakable",true,"设置不可破坏");
        tComments.clear();
        tComments.add("第二种物品格式配置");
        tComments.add("类型;子id;数量;名字");
        tComments.add("此类型的配置,可以往后向前省略配置,注意此写法不支持Lore");
        tComments.add("子id 与 数量 如果配置错误将使用相应的默认值");
        pSection.addDefault("SimpleItem","STONE;0;1;§2简单物品模板",tComments.toArray(new String[0]));
    }

}
