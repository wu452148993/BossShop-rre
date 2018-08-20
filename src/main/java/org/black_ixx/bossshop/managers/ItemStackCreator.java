package org.black_ixx.bossshop.managers;

import java.util.ArrayList;
import java.util.List;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Colorable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.wulf.xmaterial.IEnchantment;
import com.github.wulf.xmaterial.IMaterial;
import com.github.wulf.xmaterial.XMaterial;

import cc.bukkitPlugin.bossshop.nbt.NBT;
import cc.bukkitPlugin.bossshop.nbt.NBTEditManager;
import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTSerializer;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.exception.NBTDeserializeException;
import cc.bukkitPlugin.commons.plugin.manager.AManager;
import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.commons.util.ByteUtil;
import cc.commons.util.StringUtil;

public class ItemStackCreator extends AManager<BossShop>{

    public ItemStackCreator(BossShop pPlugin){
        super(pPlugin);
    }

    public ItemStack createItemStack(List<String> itemData){
        return this.createItemStackS(itemData,false);
    }

    /**
     * 创建一个物品
     * 
     * @param pItemData
     *            物品数据
     * @param pOverrideName
     *            是否使用配置中的名字覆盖物品NBT中的名字
     * @return 构建的物品
     */
    @SuppressWarnings("deprecation")
    public ItemStack createItemStackS(List<String> pItemData,boolean pOverrideName){
        String tDisplayName=null;
        ItemStack tCreatingItem=new ItemStack(Material.STONE);
        for(String sLine : pItemData){
            String[] tParts=sLine.split(":",2);
            if(tParts.length!=2){
                Log.severe("错误的物品配置,物品条目信息应该有冒号分割");
                for(String line : pItemData)
                    Log.severe("    "+line);
                return null;
            }
            String tPropName=tParts[0].trim();
            String tPropValue=tParts[1].trim();
            if(tPropName.equalsIgnoreCase("id")){
                tPropValue=stringFix(tPropValue);
                String mattype=tPropValue;
                String dur ="0";
                if(tPropValue.contains(":")){
                    String pa[]=tPropValue.split(":",2);
                    mattype=pa[0].trim();
                    dur=pa[1].trim();
                }
                if(!isInteger(mattype)){
                    Log.severe("Mistake in Config: "+tPropValue+" (id) needs to be a number!");
                    return null;
                }
                
                if(XMaterial.isDamageable(IMaterial.fromID(mattype)) && !dur.equals("0"))
                {
                	 try{
                         short dura=Short.parseShort(dur);
                         tCreatingItem.setDurability(dura);
                         tPropValue=mattype;
                     }catch(Exception e){
                         // Do not change anything
                     }
                }
                Material tMeta=IMaterial.fromID(tPropValue);

                if(tMeta==null||tMeta==Material.AIR){
                    Log.severe("Mistake in Config: "+tPropValue+" (id) is no valid Material!");
                    return null;
                }
                tCreatingItem.setType(tMeta);
                continue;
                
                /*
                if(tPropValue.contains(":")){
                    String pa[]=tPropValue.split(":",2);
                    String type=pa[0].trim();
                    String dur=pa[1].trim();
                    try{
                        short dura=Short.parseShort(dur);
                        tCreatingItem.setDurability(dura);
                        tPropValue=type;
                    }catch(Exception e){
                        // Do not change anything
                    }
                }
                if(!isInteger(tPropValue)){
                    Log.severe("Mistake in Config: "+tPropValue+" (id) needs to be a number!");
                    return null;
                }
                //Material tMeta=Material.getMaterial(Integer.parseInt(tPropValue));
                Material tMeta=IMaterial.fromID(tPropValue);
                if(tMeta==null||tMeta==Material.AIR){
                    Log.severe("Mistake in Config: "+tPropValue+" (id) is no valid Material!");
                    return null;
                }
                tCreatingItem.setType(tMeta);
                continue;*/
            }
            if(tPropName.equalsIgnoreCase("nbt")||tPropName.equalsIgnoreCase("rawnbt")){
                tPropValue=stringFix(tPropValue);
                Object tApplyNBT=null;

                if(tPropName.equalsIgnoreCase("nbt")){
                    NBT tCNBT=this.mPlugin.getManager(NBTEditManager.class).getItemNBT(tPropValue,true);
                    if(tCNBT==null){
                        Log.warn(this.mPlugin.C("MsgMissingNBTNode")+"["+tPropValue+"]");
                        return null;
                    }
                    tApplyNBT=tCNBT.getNBTCopy();
                }else{
                    try{
                        tApplyNBT=NBTSerializer.deserializeNBTFromByte(ByteUtil.base64ToByte(tPropValue));
                    }catch(NBTDeserializeException exp){
                        Log.severe("反序列化物品NBT时发生了错误",exp);
                        return null;
                    }
                }

                Object tNBTExist=NBTUtil.getItemNBT(tCreatingItem);
                if(tNBTExist!=null){
                    tNBTExist=NBTUtil.mixNBT(tNBTExist,tApplyNBT,false);
                }else{
                    tNBTExist=tApplyNBT;
                }
                ItemStack tResult=NBTUtil.setItemNBT(tCreatingItem,tNBTExist);
                if(tResult!=null) tCreatingItem=tResult;
            }else if(tPropName.equalsIgnoreCase("type")){
                tPropValue=stringFix(tPropValue);
                if(tPropValue.contains(":")){
                    String pa[]=tPropValue.split(":",2);
                    String mattype=pa[0].trim();
                    String dur=pa[1].trim();
                    
                    if(XMaterial.isDamageable(XMaterial.XfromString(mattype)) && !dur.equals("0"))
                    {
                    	 try{
                             short dura=Short.parseShort(dur);
                             tCreatingItem.setDurability(dura);
                             tPropValue=mattype;
                         }catch(Exception e){
                             // Do not change anything
                         }
                    }
                }

                
                tPropValue=tPropValue.toUpperCase();
                Material tMeta=XMaterial.fromString(tPropValue);
                if(tMeta==null||tMeta==Material.AIR){
                    Log.severe("Mistake in Config: "+tPropValue+" (type) is no valid Material!");
                    return null;
                }
                tCreatingItem.setType(tMeta);
                continue;
                   
                /*
                if(tPropValue.contains(":")){
                    String pa[]=tPropValue.split(":",2);
                    String type=pa[0].trim();
                    String dur=pa[1].trim();
                    try{
                        short dura=Short.parseShort(dur);
                        tCreatingItem.setDurability(dura);
                        tPropValue=type;
                    }catch(Exception e){
                        // Do not change anything
                    }
                }
                tPropValue=tPropValue.toUpperCase();
                //Material tMeta=Material.getMaterial(tPropValue);
                Material tMeta=XMaterial.fromString(tPropValue).parseMaterial();
                if(tMeta==null||tMeta==Material.AIR){
                    Log.severe("Mistake in Config: "+tPropValue+" (type) is no valid Material!");
                    return null;
                }
                tCreatingItem.setType(tMeta);
                continue;*/
            }else if(tPropName.equalsIgnoreCase("amount")){
                tPropValue=stringFix(tPropValue);
                if(!isInteger(tPropValue)){
                    Log.severe("Mistake in Config: "+tPropValue+" (amount) needs to be a number!");
                    return null;
                }
                tCreatingItem.setAmount(Integer.parseInt(tPropValue));
                continue;
            }else if(tPropName.equalsIgnoreCase("durability")||tPropName.equalsIgnoreCase("damage")){
                tPropValue=stringFix(tPropValue);
                if(!isShort(tPropValue)){
                    Log.severe("Mistake in Config: "+tPropValue+" (durability) needs to be a number!");
                    return null;
                }
                if(XMaterial.isDamageable(tCreatingItem.getType()))
                {
                	 tCreatingItem.setDurability(Short.parseShort(tPropValue));
                }else
                {
                	String mat = (XMaterial.fromMaterial(tCreatingItem.getType()).getOldName()) + ":"+tPropValue;
                    Material tMeta=XMaterial.fromString(mat);                		
                    if(tMeta==null||tMeta==Material.AIR){
                        Log.severe("Mistake in Config: "+tPropValue+" (type) is no valid Material!");
                        return null;
                    }
                    tCreatingItem.setType(tMeta);
                }
                	             
                continue;
            }else if(tPropName.equalsIgnoreCase("name")){
                tDisplayName=ChatColor.translateAlternateColorCodes('&',tPropValue);;
                continue;
            }else if(tPropName.equalsIgnoreCase("lore")){
                ItemMeta meta=tCreatingItem.getItemMeta();
                String par[]=tPropValue.split("#");
                List<String> lore=meta.getLore();
                if(lore==null)
                    lore=new ArrayList<>();
                for(String b : par)
                    lore.add(ChatColor.translateAlternateColorCodes('&',b));
                meta.setLore(lore);
                if(!BukkitUtil.isItemMetaEmpty(meta))
                    tCreatingItem.setItemMeta(meta);
                continue;
            }else if(tPropName.equalsIgnoreCase("enchantment")){
                tPropValue=stringFix(tPropValue);
                try{
                    String par[]=tPropValue.split("#");
                    String eType=par[0].trim().toUpperCase();
                    String eLvl=par[1].trim();
                    Enchantment e;
                    if(isInteger(eType)){
                        //e=Enchantment.getById(Integer.parseInt(eType));
                    	e=IEnchantment.fromID(Integer.parseInt(eType));
                    }else{
                        e=Enchantment.getByName(eType);
                    }
                    if(e==null){
                        Log.severe("Mistake in Config: "+tPropValue+" (enchantment) contains mistakes: Invalid Enchantment name");
                        return null;
                    }
                    if(tCreatingItem.getType()==Material.ENCHANTED_BOOK){
                        EnchantmentStorageMeta meta=(EnchantmentStorageMeta)tCreatingItem.getItemMeta();
                        meta.addStoredEnchant(e,Integer.parseInt(eLvl),true);
                        if(!BukkitUtil.isItemMetaEmpty(meta))
                            tCreatingItem.setItemMeta(meta);
                        continue;
                    }
                    tCreatingItem.addUnsafeEnchantment(e,Integer.parseInt(eLvl));
                }catch(Exception e){
                    Log.severe("Mistake in Config: "+tPropValue+" (enchantment) contains mistakes!");
                    return null;
                }
                continue;
            }else if(tPropName.equalsIgnoreCase("enchantmentid")){
                tPropValue=stringFix(tPropValue);
                try{
                    String par[]=tPropValue.split("#");
                    String eType=par[0].trim();
                    String eLvl=par[1].trim();
                    //Enchantment e=Enchantment.getById(Integer.parseInt(eType));
                    Enchantment e=IEnchantment.fromID(Integer.parseInt(eType));
                    if(e==null){
                        Log.severe("Mistake in Config: "+tPropValue+" (enchantmentid) contains mistakes: Invalid Enchantment id");
                        return null;
                    }
                    if(tCreatingItem.getType()==Material.ENCHANTED_BOOK){
                        EnchantmentStorageMeta meta=(EnchantmentStorageMeta)tCreatingItem.getItemMeta();
                        meta.addStoredEnchant(e,Integer.parseInt(eLvl),true);
                        // meta.addEnchant(e, Integer.parseInt(eLvl), true);
                        if(!BukkitUtil.isItemMetaEmpty(meta))
                            tCreatingItem.setItemMeta(meta);
                        continue;
                    }
                    tCreatingItem.addUnsafeEnchantment(e,Integer.parseInt(eLvl));
                }catch(Exception e){
                    Log.severe("Mistake in Config: "+tPropValue+" (enchantmentid) contains mistakes!");
                    return null;
                }
                continue;
            }else if(tPropName.equalsIgnoreCase("color")){
                tPropValue=stringFix(tPropValue);
                Color c;
                try{
                    String par[]=tPropValue.split("#");
                    String c1=par[0].trim();
                    String c2=par[1].trim();
                    String c3=par[2].trim();
                    Integer i1=Integer.parseInt(c1);
                    Integer i2=Integer.parseInt(c2);
                    Integer i3=Integer.parseInt(c3);
                    c=Color.fromRGB(i1,i2,i3);
                }catch(Exception e){
                    Log.severe("Mistake in Config: "+tPropValue+" (color) contains mistakes! A color Line should look like this: \"color:<red number>#<green number>#<blue number>\". You can find a list of RGB Colors here: http://www.farb-tabelle.de/de/farbtabelle.htm");
                    return null;
                }
                if(!(tCreatingItem.getItemMeta() instanceof Colorable)&!(tCreatingItem.getItemMeta() instanceof LeatherArmorMeta)){
                    Log.severe("Mistake in Config: The item "+tPropValue+" (Type "+tCreatingItem.getType()+")"+"(color) can't be colored/dyed! Tip: Always define the Material Type before you color the item!");
                    return null;
                }
                if(tCreatingItem.getItemMeta() instanceof Colorable){
                    Colorable ic=(Colorable)tCreatingItem.getItemMeta();
                    DyeColor color=DyeColor.getByColor(c);
                    ic.setColor(color);
                    tCreatingItem.setItemMeta((ItemMeta)ic);
                    continue;
                }
                if(tCreatingItem.getItemMeta() instanceof LeatherArmorMeta){
                    LeatherArmorMeta ic=(LeatherArmorMeta)tCreatingItem.getItemMeta();
                    ic.setColor(c);
                    tCreatingItem.setItemMeta(ic);
                    continue;
                }
                continue;
            }else if(tPropName.equalsIgnoreCase("playerhead")){
            	if(tCreatingItem.getType()!=Material.SKELETON_SKULL && tCreatingItem.getType()!=Material.WITHER_SKELETON_SKULL){
                    Log.severe("Mistake in Config: "+tPropValue+" (playerhead) You can't use \"PlayerHead\" on items which are not skulls...");
                    return null;
                }
                SkullMeta meta=(SkullMeta)tCreatingItem.getItemMeta();
                meta.setOwner(tPropValue);
                if(!BukkitUtil.isItemMetaEmpty(meta))
                    tCreatingItem.setItemMeta(meta);
                continue;
            }else if(tPropName.equalsIgnoreCase("potioneffect")){
                tPropValue=stringFix(tPropValue);
                if(tCreatingItem.getType()!=Material.POTION){
                    Log.severe("Mistake in Config: "+tPropValue+" (potioneffect) You can't add PotionEffects to items which are not potions...");
                    return null;
                }
                PotionMeta meta=(PotionMeta)tCreatingItem.getItemMeta();
                try{
                    String par[]=tPropValue.split("#");
                    String pType=par[0].trim().toUpperCase();
                    String pLvl=par[1].trim();
                    String pTime=par[2].trim();
                    PotionEffectType type;
                    if(isInteger(pType)){
                        type=PotionEffectType.getById(Integer.parseInt(pType));
                    }else{
                        type=PotionEffectType.getByName(pType);
                    }
                    meta.addCustomEffect(new PotionEffect(type,getTicksFromSeconds(pTime),Integer.parseInt(pLvl)),true);
                    if(!BukkitUtil.isItemMetaEmpty(meta))
                        tCreatingItem.setItemMeta(meta);
                }catch(Exception e){
                    Log.severe("Mistake in Config: "+tPropValue+" (potioneffect) contains mistakes!");
                    return null;
                }
                continue;
            }
        }

        if(StringUtil.isNotEmpty(tDisplayName)){
            ItemMeta tMeta=tCreatingItem.getItemMeta();
            if(pOverrideName||!tMeta.hasDisplayName()){
                tMeta.setDisplayName(tDisplayName);
                tCreatingItem.setItemMeta(tMeta);
            }
        }
        return tCreatingItem;
    }

    private static boolean isInteger(String str){
        try{
            Integer.parseInt(str);
        }catch(NumberFormatException nfe){
            return false;
        }
        return true;
    }

    private static boolean isShort(String str){
        try{
            Short.parseShort(str);
        }catch(NumberFormatException nfe){
            return false;
        }
        return true;
    }

    private static String stringFix(String s){
        if(s.contains(" ")){
            s=s.replaceAll(" ","");
        }
        return s;
    }

    private static int getTicksFromSeconds(String s){
        try{
            Double d=Double.parseDouble(s);
            return (int)(d*20);
        }catch(Exception e){
        }
        try{
            Integer i=Integer.parseInt(s);
            return (int)(i*20);
        }catch(Exception e){
        }
        return 0;
    }

}
