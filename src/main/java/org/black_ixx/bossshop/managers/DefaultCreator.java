package org.black_ixx.bossshop.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSShop;
import org.black_ixx.bossshop.core.BSShopManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import com.github.wulf.xmaterial.IMaterial;

import cc.commons.commentedyaml.CommentedSection;
import cc.commons.commentedyaml.CommentedYamlConfig;
import cc.commons.util.StringUtil;;

public class DefaultCreator{

    private BossShop mPlugin;

    public DefaultCreator(BossShop pPlugin){
        this.mPlugin=pPlugin;
    }

    public static void addDefault(CommentedYamlConfig config,String name,String rewardType,String priceType,Object reward,Object price,List<String> menuitem,String message,int loc,String permission){
        CommentedSection c=config.getSection("shop."+name);
        if(c==null)
            c=config.createSection("shop."+name);
        c.set("RewardType",rewardType);
        c.set("PriceType",priceType);
        c.set("Price",price);
        c.set("Reward",reward);
        c.set("MenuItem",menuitem);
        c.set("Message",message);
        c.set("InventoryLocation",loc);
        c.set("ExtraPermission",permission);
    }

    public static void addDefault(CommentedYamlConfig config,String name,String rewardType,String priceType,Object reward,Object price,List<String> menuitem,String message,int loc,String permission,String extra_node,Object extra_object){
        CommentedSection c=config.getSection("shop."+name);
        if(c==null)
            c=config.createSection("shop."+name);
        c.set("RewardType",rewardType);
        c.set("PriceType",priceType);
        c.set("Price",price);
        c.set("Reward",reward);
        c.set("MenuItem",menuitem);
        c.set("Message",message);
        c.set("InventoryLocation",loc);
        c.set("ExtraPermission",permission);
        c.set(extra_node,extra_object);
    }

    private File getFile(String name){
        return new File(BossShop.getInstance().getDataFolder().getAbsolutePath()+"/shops/"+name);
    }

    private CommentedYamlConfig getConfig(File f){
        CommentedYamlConfig tConfig=new CommentedYamlConfig();
        tConfig.loadFromFile(f);
        return tConfig;
    }

    public static List<String> createMenuItem(String name,String lore,String material,String add){
        List<String> item=new ArrayList<String>();
        item.add("type:"+material);
        item.add("amount:1");
        item.add("name:"+name);
        if(StringUtil.isNotEmpty(lore))
            item.add("lore:"+lore);
        if(StringUtil.isNotEmpty(add))
            item.add(add);
        return item;
    }

    public static List<String> createMenuItem(String name,String lore,String material,String add,String add2){
        List<String> item=new ArrayList<String>();
        item.add("type:"+material);
        item.add("amount:1");
        item.add("name:"+name);
        item.add("lore:"+lore);
        if(StringUtil.isNotEmpty(add)){
            item.add(add);
            item.add(add2);
        }
        return item;
    }

    public static List<String> createMenuItem(String name,String lore,String material,String add,String add2,String add3){
        List<String> item=new ArrayList<String>();
        item.add("type:"+material);
        item.add("amount:1");
        item.add("name:"+name);
        item.add("lore:"+lore);
        if(StringUtil.isNotEmpty(add)){
            item.add(add);
            item.add(add2);
            item.add(add3);
        }
        return item;
    }

    public static List<List<String>> createItem(String name,String lore,String material,String add,String add2){
        List<List<String>> x=new ArrayList<List<String>>();
        List<String> item=new ArrayList<String>();
        item.add("type:"+material);
        item.add("amount:1");
        item.add("name:"+name);
        item.add("lore:"+lore);
        if(StringUtil.isNotEmpty(add)){
            item.add(add);
            item.add(add2);
        }
        x.add(item);
        return x;
    }

    public static List<List<String>> createItem(String name,String lore,String material,String add,String add2,String add3){
        List<List<String>> x=new ArrayList<List<String>>();
        List<String> item=new ArrayList<String>();
        item.add("type:"+material);
        item.add("amount:1");
        item.add("name:"+name);
        item.add("lore:"+lore);
        if(StringUtil.isNotEmpty(add)){
            item.add(add);
            item.add(add2);
            item.add(add3);
        }
        x.add(item);
        return x;
    }

    public static List<List<String>> createPoorItem(String amount,String material,String add){
        List<List<String>> x=new ArrayList<List<String>>();
        List<String> item=new ArrayList<String>();
        item.add("type:"+material);
        item.add("amount:"+amount);
        if(StringUtil.isNotEmpty(add)){
            item.add(add);
        }
        x.add(item);
        return x;
    }

    public static List<String> createOneLineList(String text){
        List<String> l=new ArrayList<String>();
        l.add(text);
        return l;
    }

    public static List<String> createMenuItemSpell(String name,String lore,String add){
        List<String> item=new ArrayList<String>();
        item.add("type:WRITTEN_BOOK");
        item.add("amount:1");
        item.add("name:"+name);
        item.add("lore:"+lore);
        if(StringUtil.isNotEmpty(add)){
            item.add(add);
        }
        return item;
    }

    public static void setSettings(CommentedYamlConfig config,String shopName,String displayname){
        config.set("Setting.DisplayName",displayname);
        config.set("signs.text","["+shopName+"]");
        config.set("signs.NeedPermissionToCreateSign",true);
        config.createSection("shop");
    }

    /**
     * 为商店添加一个物品项用于链接到其他商店
     * @param config 配置文件
     * @param pLocation 商店物品放置位置
     * @param itemID 菜单显示物品ID
     * @param pTarget 目标商店
     * @param pName 配置文件中存储的物品名字
     */
    public static void setGuidItem(CommentedYamlConfig config,int pLocation,int itemID,String pTarget,String pName){
        String tTargetDisplay=pTarget;
        BSShop tShop=BossShop.getInstance().getManager(BSShopManager.class).getShop(pTarget);
        if(tShop!=null)
            tTargetDisplay=tShop.getDisplayName();
        if(StringUtil.isEmpty(tTargetDisplay))
            tTargetDisplay=pTarget;
        setGuidItem(config,pLocation,itemID,pTarget,tTargetDisplay,pName);
    }
    //重载String
    public static void setGuidItem(CommentedYamlConfig config,int pLocation,String itemName,String pTarget,String pName){
        String tTargetDisplay=pTarget;
        BSShop tShop=BossShop.getInstance().getManager(BSShopManager.class).getShop(pTarget);
        if(tShop!=null)
            tTargetDisplay=tShop.getDisplayName();
        if(StringUtil.isEmpty(tTargetDisplay))
            tTargetDisplay=pTarget;
        setGuidItem(config,pLocation,itemName,pTarget,tTargetDisplay,pName);
    }
    
    /**
     * 为商店添加一个物品项用于链接到其他商店
     * @param config 配置文件
     * @param pLocation 商店物品放置位置
     * @param itemID 菜单显示物品ID
     * @param pTarget 目标商店
     * @param pTargetDisplay 目标名字,用于合成菜单物品名
     * @param pName 配置文件中存储的物品名字
     */
    public static void setGuidItem(CommentedYamlConfig config,int pLocation,int itemID,String pTarget,String pTargetDisplay,String pName){
        String display=BossShop.getInstance().getLangManager().getNode("WordGoTo")+" "+pTargetDisplay;
        String tShowItem=Material.REDSTONE.name();
        //Material mate=Material.getMaterial(itemID);
        Material mate=IMaterial.fromID(itemID+"");
        if(mate!=null&&mate!=Material.AIR)
            tShowItem=mate.name();
        addDefault(config,pName,"shop","free",pTarget,null,createMenuItem("&9&l"+display,"",tShowItem,""),"",pLocation,"");
    }
    //重载String
    public static void setGuidItem(CommentedYamlConfig config,int pLocation,String itemName,String pTarget,String pTargetDisplay,String pName){
        String display=BossShop.getInstance().getLangManager().getNode("WordGoTo")+" "+pTargetDisplay;
        String tShowItem=Material.REDSTONE.name();
        Material mate=Material.getMaterial(itemName);
        if(mate!=null&&mate!=Material.AIR)
            tShowItem=mate.name();
        addDefault(config,pName,"shop","free",pTarget,null,createMenuItem("&9&l"+display,"",tShowItem,""),"",pLocation,"");
    }
    
    public void addAllExamples(){
        // ShopMenu
        {
            String name="Menu";
            File f=getFile(name+".yml");
            CommentedYamlConfig config=getConfig(f);
            setSettings(config,name,"&lMenu");
            addDefault(config,"MenuBuyShop","shop","free","BuyShop",null,createMenuItem("&9&lBuyShop &6[+]","&8Here you can buy Items","GLASS",""),"",1,"");
            addDefault(config,"MenuSellShop","shop","free","SellShop",null,createMenuItem("&b&lSellShop &6[+]","&8Here you can sell Items","COBBLESTONE",""),"",3,"");
            addDefault(config,"MenuPotions","shop","free","Potions",null,createMenuItem("&5&lPotions &6[**]","&8Here you can buy Potions","POTION","potioneffect:SPEED#1#1"),"",5,"");
            addDefault(config,"MenuSpells","shop","free","Spells",null,createMenuItem("&4&lSpells &6[*]","&8Here you can buy magical Spells","WRITTEN_BOOK",""),"",7,"");
            String mpsl="&8Here you can buy Points #"+(simplePointsPluginCheck()?"#&b&lYour Points: &r&9%playerpoints_points%":"")+"#&b&lYour Money: &r&9%vault_eco_balance% ##&cOnly for VIPs";
            addDefault(config,"MenuPointShop","shop","free","PointShop",null,createMenuItem("&6&lPointShop &a[v]",mpsl,"DIAMOND",""),"",9,"Permission.Vip");
            addDefault(config,"MenuWarps","shop","free","Warps",null,createMenuItem("&a&lWarps &6[x]","&8Free Teleportation","PAPER",""),"",10,"");
            config.saveToFile(f);
        }
        // Items
        {
            String name="BuyShop";
            File f=getFile(name+".yml");
            CommentedYamlConfig config=getConfig(f);
            setSettings(config,name,"&9&lBuyShop");
            List<List<String>> changeRewardItems=new ArrayList<List<String>>();
            List<String> changeRewardItem1=new ArrayList<String>();
            changeRewardItem1.add("type:WOOL");
            changeRewardItem1.add("amount:10");
            changeRewardItem1.add("durability:14");
            List<String> changeRewardItem2=new ArrayList<String>();
            changeRewardItem2.add("type:WOOL");
            changeRewardItem2.add("amount:10");
            changeRewardItem2.add("durability:11");
            List<String> changeRewardItem3=new ArrayList<String>();
            changeRewardItem3.add("type:WOOL");
            changeRewardItem3.add("amount:10");
            changeRewardItem3.add("durability:4");
            List<String> changeRewardItem4=new ArrayList<String>();
            changeRewardItem4.add("type:WOOL");
            changeRewardItem4.add("amount:10");
            changeRewardItem4.add("durability:5");
            changeRewardItems.add(changeRewardItem1);
            changeRewardItems.add(changeRewardItem2);
            changeRewardItems.add(changeRewardItem3);
            changeRewardItems.add(changeRewardItem4);
            addDefault(config,"Wool","item","money",changeRewardItems,400,createMenuItem("&9Colored Wool","&c10 Red, &110 Blue, &e10 Yellow and &a10 Green Wool #&cPrice: 400 Money","WOOL","durability:11","amount:40"),"&eYou bought colored Wool! Money left: &c%left%",1,"");
            addDefault(config,"Diamonds","item","money",createPoorItem("5","DIAMOND",""),5000,createMenuItem("&95 Diamonds","&cPrice: 5000 Money","DIAMOND","amount:5"),"&eYou bought %reward%! Money left: &c%left%",3,"");
            addDefault(config,"ShadowArmor","item","money",createItem("&5ShadowArmor","","LEATHER_CHESTPLATE","color:110#10#140","enchantment:PROTECTION_ENVIRONMENTAL#10"),1500,createMenuItem("&5%itemname%","&cPrice: 1500 Money","LEATHER_CHESTPLATE","color:110#10#140","enchantment:PROTECTION_ENVIRONMENTAL#10"),"&eYou bought 1 %itemname%! Money left: &c%left%",5,"");
            addDefault(config,"Obsidian","item","money",createPoorItem("64","OBSIDIAN",""),10000,createMenuItem("&964 Obsidian","&cPrice: 10000 Money","OBSIDIAN","amount:64"),"&eYou bought %reward%! Money left: &c%left%",7,"");
            addDefault(config,"GodApple","item","money",createPoorItem("1","GOLDEN_APPLE","durability:1"),10000,createMenuItem("&9%itemname%","&cPrice: 10000 Money","GOLDEN_APPLE","durability:1"),"&eYou bought 1 %itemname%! Money left: &c%left%",9,"");
            addDefault(config,"EnchantUnbreaking","enchantment","exp","DURABILITY#3",25,createMenuItem("&4[crown] &cEnchantment &4[crown]","&8Enchants the Item in your hand. #&cPrice: 25 Exp Level ##&cOnly for VIPs!","ENCHANTED_BOOK","enchantment:DURABILITY#3"),"&eThe Enchantment Unbreaking III was added to your item!",19,"Permission.Vip");
            addDefault(config,"BossSword","item","points",createItem("&cBossSword","&8&o%player_name%'s Sword","DIAMOND_SWORD","enchantment:DAMAGE_ALL#5","enchantment:FIRE_ASPECT#2"),2000,createMenuItem("&4[*] &c%itemname% &4[*]","&cPrice: 2000 Points","DIAMOND_SWORD","enchantment:DAMAGE_ALL#5","enchantment:FIRE_ASPECT#2"),"&eYou bought 1 %itemname%! Money left: &c%left%",21,"");
            addDefault(config,"Back","shop","free","menu",null,createMenuItem("&cBack","&8Back to Menu","REDSTONE",""),"&6Leaving the ItemShop...",27,"");
            config.saveToFile(f);
        }
        // SellShop
        {
            String name="SellShop";
            File f=getFile(name+".yml");
            CommentedYamlConfig config=getConfig(f);
            setSettings(config,name,"&b&lSellShop");
            addDefault(config,"Diamond","money","item",100,createPoorItem("1","DIAMOND",""),createMenuItem("&b1 Diamond","&2Reward: 100 Money","DIAMOND","amount:1"),"&eYou sold %price% for %reward% %rewardtype%!",1,"");
            addDefault(config,"Cobblestone","money","item",20,createPoorItem("32","COBBLESTONE",""),createMenuItem("&b32 Cobblestone","&2Reward: 20 Money","COBBLESTONE","amount:32"),"&eYou sold &eYou sold %price% for %reward% %rewardtype%!",2,"");
            addDefault(config,"GLASS","money","item",30,createPoorItem("32","GLASS",""),createMenuItem("&b32 Glass","&2Reward: 30 Money","GLASS","amount:32"),"&eYou sold %price% for %reward% %rewardtype%!",3,"");
            addDefault(config,"Back","shop","free","menu",null,createMenuItem("&cBack","&8Back to Menu","REDSTONE",""),"&6Leaving the ItemShop...",27,"");
            config.saveToFile(f);
        }
        // Potions
        {
            String name="Potions";
            File f=getFile(name+".yml");
            CommentedYamlConfig config=getConfig(f);
            setSettings(config,name,"&5&lPotions");
            addDefault(config,"NinjaPotion","item","item",createItem("&5NinjaPotion","&8No Barrier can stop you!","POTION","potioneffect:SPEED#2#600","potioneffect:JUMP#0#600","amount:3"),createPoorItem("5","EMERALD",""),createMenuItem("&5NinjaPotion","&8No Barrier can stop you! #&cPrice: 5 Emeralds","POTION","potioneffect:SPEED#2#600","potioneffect:JUMP#0#600","amount:3"),"&eYou bought 3 NinjaPotions!",1,"");
            addDefault(config,"BeserkerPotion","item","item",createItem("&5BeserkerPotion","&8Cut em down!!!","POTION","potioneffect:INCREASE_DAMAGE#2#600","potioneffect:CONFUSION#0#600","amount:3"),createPoorItem("1","GHAST_TEAR",""),createMenuItem("&5BeserkerPotion","&8Cut em down!!! #&cPrice: 1 Ghast Tear","POTION","potioneffect:INCREASE_DAMAGE#2#600","potioneffect:CONFUSION#0#600","amount:3"),"&eYou bought 3 BeserkerPotions!",2,"");
            addDefault(config,"GhostPotion","item","item",createItem("&5GhostPotion","&8Where are you? I can't see you!","POTION","potioneffect:INVISIBILITY#0#600","potioneffect:NIGHT_VISION#0#600","amount:3"),createPoorItem("30","SOUL_SAND",""),createMenuItem("&5GhostPotion","&8Where are you? I can't see you! #&cPrice: 30 SoulSand","POTION","potioneffect:INVISIBILITY#0#600","potioneffect:NIGHT_VISION#0#600","amount:3"),"&eYou bought 3 GhostPotions!",3,"");
            addDefault(config,"TitanPotion","item","points",createItem("&cTitanPotion","&8Ahaha only Gods can defeat you!!!","POTION","potioneffect:REGENERATION#1#600","amount:3"),750,createMenuItem("&4[*] &cTitanPotion &4[*]","&8Ahaha only Gods can defeat you!!! #&cPrice: 750 Points","POTION","potioneffect:REGENERATION#1#600","amount:3"),"&eYou bought 3 TitanPotions!",5,"");
            addDefault(config,"Back","shop","free","menu",null,createMenuItem("&cBack","&8Back to Menu","REDSTONE",""),"&6Leaving the ItemShop...",9,"");
            config.saveToFile(f);
        }
        // Spells
        {
            String name="Spells";
            File f=getFile(name+".yml");
            CommentedYamlConfig config=getConfig(f);
            setSettings(config,name,"&4&lSpells");
            addDefault(config,"SpellFireball","permission","exp",createOneLineList("Permission.Fireball"),10,createMenuItemSpell("&4Fireball","&8Allows you to shoot Fireballs #&cPrice: 10 Levels",""),"&eYou bought the Fireball Spell!",1,"");
            addDefault(config,"SpellPoison","permission","exp",createOneLineList("Permission.Poison"),20,createMenuItemSpell("&4Poison &2[radioactive]","&8Allows you to poison your enemies #&cPrice: 20 Levels",""),"&eYou bought the Poison Spell!",2,"");
            addDefault(config,"SpellBolt","permission","exp",createOneLineList("Permission.Bolt"),30,createMenuItemSpell("&4Bolt","&8Allows you to strike your enemies with Bolts #&cPrice: 30 Levels",""),"&eYou bought the Bolt Spell!",3,"");
            addDefault(config,"SpellVanish","permission","exp",createOneLineList("Permission.Vanish"),40,createMenuItemSpell("&4Vanish","&8Allows you to vanish for a few seconds #&cPrice: 40 Levels",""),"&eYou bought the Vanish Spell!",4,"");
            addDefault(config,"SpellFreeze","permission","points",createOneLineList("Permission.Freeze"),3500,createMenuItemSpell("&4[*] &cFreeze &4[*]","&8Allows you to freeze your enemies #&cPrice: 3500 Points",""),"&eYou bought the Freeze Spell!",6,"");
            addDefault(config,"Back","shop","free","menu",null,createMenuItem("&cBack","&8Back to Menu","REDSTONE",""),"&6Leaving the SpellShop...",9,"");
            config.saveToFile(f);
        }
        // PointShop
        {
            String name="PointShop";
            File f=getFile(name+".yml");
            CommentedYamlConfig config=getConfig(f);
            setSettings(config,name,"&6&lPointShop &2[v]");
            addDefault(config,"100Points","points","money",100,1000,createMenuItem("&6100 Points","&cPrice: 1000 Money","DIAMOND","amount:1"),"&eYou bought 100 Points! Money left: &c%left%",1,"");
            addDefault(config,"500Points","points","money",500,5000,createMenuItem("&6500 Points","&cPrice: 5000 Money","DIAMOND","amount:5"),"&eYou bought 500 Points! Money left: &c%left%",2,"");
            addDefault(config,"1000Points","points","money",1000,10000,createMenuItem("&61000 Points","&cPrice: 10000 Money","DIAMOND","amount:10"),"&eYou bought 1000 Points! Money left: &c%left%",3,"");
            addDefault(config,"500Money","money","points",500,100,createMenuItem("&6500 Money","&cPrice: 100 Points","EMERALD","amount:5"),"&eYou bought 500 Money! Points left: &c%left%",19,"");
            addDefault(config,"1000Money","money","points",1000,200,createMenuItem("&61000 Money","&cPrice: 200 Points","EMERALD","amount:10"),"&eYou bought 1000 Money! Points left: &c%left%",20,"");
            addDefault(config,"5000Money","money","points",5000,1000,createMenuItem("&65000 Money","&cPrice: 1000 Points","EMERALD","amount:50"),"&eYou bought 5000 Money! Points left: &c%left%",21,"");
            addDefault(config,"Back","shop","free","menu",null,createMenuItem("&cBack","&8Back to Menu","REDSTONE",""),"&6Leaving the PointShop...",27,"");
            config.saveToFile(f);
        }
        // Warps
        {
            String name="Warps";
            File f=getFile(name+".yml");
            CommentedYamlConfig config=getConfig(f);
            setSettings(config,name,"&a&lWarps");
            ArrayList<String> w1=new ArrayList<String>();
            w1.add("warp spawn");
            ArrayList<String> w2=new ArrayList<String>();
            w2.add("warp pvp");
            ArrayList<String> w3=new ArrayList<String>();
            w3.add("warp shop");
            addDefault(config,"Spawn","playercommand","free",w1,null,createMenuItem("&aSpawn","&8Warp to the Spawn","COMPASS","amount:1"),null,1,"");
            addDefault(config,"PvP","playercommand","free",w2,null,createMenuItem("&aPvP","&8Warp to the PvP Arena","DIAMOND_SWORD","amount:1"),null,2,"");
            addDefault(config,"Shop","playercommand","free",w3,null,createMenuItem("&aShop","&8Warp to the Shop","GOLD_INGOT","amount:1"),null,3,"");
            addDefault(config,"Back","shop","free","menu",null,createMenuItem("&cBack","&8Back to Menu","REDSTONE",""),"&6Leaving the ItemShop...",27,"");
            config.saveToFile(f);
        }
    }

    public void createShopItemsExample(File pFile,CommentedYamlConfig pConfig){
        pConfig.set("signs.text","[ExampleShop]");
        pConfig.set("signs.NeedPermissionToCreateSign",false);
        pConfig.createSection("shop");
        List<List<String>> diaswordRewardItems=new ArrayList<List<String>>();
        List<String> diaswordRewardItem=new ArrayList<String>();
        diaswordRewardItem.add("id:276");
        diaswordRewardItem.add("amount:1");
        diaswordRewardItem.add("name:&bDiamond Sword");
        diaswordRewardItem.add("lore:Line1#Line2#Line3#Line4#This is an Example");
        diaswordRewardItem.add("enchantment:DAMAGE_ALL#3");
        diaswordRewardItem.add("enchantment:FIRE_ASPECT#2");
        diaswordRewardItems.add(diaswordRewardItem);
        List<String> diaswordMenuItem=new ArrayList<String>();
        diaswordMenuItem.add("type:DIAMOND_SWORD");
        diaswordMenuItem.add("amount:1");
        diaswordMenuItem.add("name:&bDiamond Sword");
        diaswordMenuItem.add("lore:Line1#Line2#Line3#Line4#This is an Example");
        diaswordMenuItem.add("enchantmentId:16#3");
        diaswordMenuItem.add("enchantmentId:20#2");
        addDefault(pConfig,"DiamondSword","item","money",diaswordRewardItems,5000,diaswordMenuItem,"&6You bought a Diamond Sword! &cMoney left: %left%",1,"");
        List<String> pointsMenuItem=new ArrayList<String>();
        pointsMenuItem.add("type:WRITTEN_BOOK");
        pointsMenuItem.add("amount:1");
        pointsMenuItem.add("name:&6PointSet");
        pointsMenuItem.add("lore:&e1000 Points#&cPrice: 500 Money!");
        addDefault(pConfig,"PointSet","points","money",1000,500,pointsMenuItem,"&6You bought 1000 Points! &cMoney left: %left%",3,"");
        List<String> moneyMenuItem=new ArrayList<String>();
        moneyMenuItem.add("type:WRITTEN_BOOK");
        moneyMenuItem.add("amount:1");
        moneyMenuItem.add("name:&6MoneySet");
        moneyMenuItem.add("lore:&e500 Money#&cPrice: 1000 Points!");
        addDefault(pConfig,"MoneySet","money","points",500,1000,moneyMenuItem,"&6You bought 500 Money! &cPoints left: %left%",4,"");
        List<String> sMenuItem=new ArrayList<String>();
        sMenuItem.add("type:WRITTEN_BOOK");
        sMenuItem.add("amount:1");
        sMenuItem.add("name:&4Kick");
        sMenuItem.add("lore:&cPrice: Free");
        List<String> sCmds=new ArrayList<String>();
        sCmds.add("kick %name%");
        addDefault(pConfig,"Kick","command","free",sCmds,null,sMenuItem,"",5,"");
        List<String> opMenuItem=new ArrayList<String>();
        opMenuItem.add("type:BEDROCK");
        opMenuItem.add("amount:1");
        opMenuItem.add("name:&5OP");
        opMenuItem.add("lore:&eYou are from PlanetMinecraft and you want to #&ebecome OP to be able to review the server? #&eNo problem! #&cJust buy it for 5 Levels!");
        List<String> opcmd=new ArrayList<String>();
        opcmd.add("op %name%");
        opcmd.add("say Yay! %name% will review the server now!");
        addDefault(pConfig,"OP","command","exp",opcmd,5,opMenuItem,"",9,"");
        List<String> flyMenuItem=new ArrayList<String>();
        flyMenuItem.add("type:FEATHER");
        flyMenuItem.add("amount:1");
        flyMenuItem.add("name:&5Fly");
        flyMenuItem.add("lore:&eAllows you to fly#&cPrice: 64 diamonds and 10 emeralds! #&cOnly for VIPs!");
        List<List<String>> flyPriceItems=new ArrayList<List<String>>();
        List<String> flyPriceItem1=new ArrayList<String>();
        flyPriceItem1.add("type:DIAMOND");
        flyPriceItem1.add("amount:64");
        List<String> flyPriceItem2=new ArrayList<String>();
        flyPriceItem2.add("type:EMERALD");
        flyPriceItem2.add("amount:10");
        flyPriceItems.add(flyPriceItem1);
        flyPriceItems.add(flyPriceItem2);
        List<String> flyPermissions=new ArrayList<String>();
        flyPermissions.add("essentials.fly");
        addDefault(pConfig,"Fly","permission","item",flyPermissions,flyPriceItems,flyMenuItem,"&6You bought Fly Permissions!",10,"VIP.Access");
        List<List<String>> evilCookieRewardItems=new ArrayList<List<String>>();
        List<String> evilCookieRewardItem=new ArrayList<String>();
        evilCookieRewardItem.add("type: COOKIE");
        evilCookieRewardItem.add("amount:1");
        evilCookieRewardItem.add("name:&4Evil Cookie");
        evilCookieRewardItem.add("lore:&0&l*_* #&cPrice: 40 Levels");
        evilCookieRewardItem.add("enchantment:KNOCKBACK#10");
        evilCookieRewardItem.add("enchantment:FIRE_ASPECT#2");
        evilCookieRewardItem.add("enchantment:DAMAGE_ALL#5");
        evilCookieRewardItems.add(evilCookieRewardItem);
        List<String> evilCookieMenuItem=new ArrayList<String>();
        evilCookieMenuItem.add("type: COOKIE");
        evilCookieMenuItem.add("amount:1");
        evilCookieMenuItem.add("name:&4Evil Cookie");
        evilCookieMenuItem.add("lore:&0&l*_* #&cPrice: 40 Levels #&cOnly for VIPs!");
        evilCookieMenuItem.add("enchantment:KNOCKBACK#10");
        evilCookieMenuItem.add("enchantment:FIRE_ASPECT#2");
        evilCookieMenuItem.add("enchantment:DAMAGE_ALL#5");
        addDefault(pConfig,"EvilCookie","item","exp",evilCookieRewardItems,40,evilCookieMenuItem,"&4Nothing will stop you now!",12,"VIP.Access");
        List<String> npMenuItem=new ArrayList<String>();
        npMenuItem.add("type:POTION");
        npMenuItem.add("amount:1");
        npMenuItem.add("name:&1NinjaPotion");
        npMenuItem.add("lore:&e1 NinjaPotion #&cPrice: 400 Money");
        npMenuItem.add("potioneffect:SPEED#4#600");
        npMenuItem.add("potioneffect:JUMP#0#600");
        npMenuItem.add("potioneffect:NIGHT_VISION#0#600");
        List<List<String>> npItems=new ArrayList<List<String>>();
        List<String> np2MenuItem=new ArrayList<String>();
        np2MenuItem.add("type:POTION");
        np2MenuItem.add("amount:1");
        np2MenuItem.add("name:&1NinjaPotion");
        np2MenuItem.add("lore:&e1 NinjaPotion #&cPrice: 400 Money #&cOnly for VIPs!");
        np2MenuItem.add("potioneffect:SPEED#4#600");
        np2MenuItem.add("potioneffect:JUMP#0#600");
        np2MenuItem.add("potioneffect:NIGHT_VISION#0#600");
        npItems.add(np2MenuItem);
        addDefault(pConfig,"NinjaPotion","item","money",npItems,400,npMenuItem,"&6You bought 1 NinjaPotion! &cMoney left: %left%",13,"VIP.Access");
        List<List<String>> changePriceItems=new ArrayList<List<String>>();
        List<String> changePriceItem=new ArrayList<String>();
        changePriceItem.add("type:DIAMOND");
        changePriceItem.add("amount:2");
        changePriceItems.add(changePriceItem);
        List<List<String>> changeRewardItems=new ArrayList<List<String>>();
        List<String> changeRewardItem1=new ArrayList<String>();
        changeRewardItem1.add("type:WOOL");
        changeRewardItem1.add("amount:10");
        changeRewardItem1.add("durability:14");
        List<String> changeRewardItem2=new ArrayList<String>();
        changeRewardItem2.add("type:WOOL");
        changeRewardItem2.add("amount:10");
        changeRewardItem2.add("durability:11");
        List<String> changeRewardItem3=new ArrayList<String>();
        changeRewardItem3.add("type:WOOL");
        changeRewardItem3.add("amount:10");
        changeRewardItem3.add("durability:4");
        List<String> changeRewardItem4=new ArrayList<String>();
        changeRewardItem4.add("type:WOOL");
        changeRewardItem4.add("amount:10");
        changeRewardItem4.add("durability:5");
        changeRewardItems.add(changeRewardItem1);
        changeRewardItems.add(changeRewardItem2);
        changeRewardItems.add(changeRewardItem3);
        changeRewardItems.add(changeRewardItem4);
        List<String> changeMenuItem=new ArrayList<String>();
        changeMenuItem.add("type:WOOL");
        changeMenuItem.add("amount:40");
        changeMenuItem.add("durability:10");
        changeMenuItem.add("name:&bColored Wool");
        changeMenuItem.add("lore:&c10 Red, &110 Blue, &e10 Yellow and &a10 Green Wool #&cPrice: 2 Diamonds");
        addDefault(pConfig,"WoolSet","item","item",changeRewardItems,changePriceItems,changeMenuItem,"&6You bought a set of wool!",14,"");
        List<String> backMenuItem=new ArrayList<String>();
        backMenuItem.add("type:REDSTONE");
        backMenuItem.add("amount:1");
        backMenuItem.add("name:&4Back");
        backMenuItem.add("lore:&8Go back to the Menu");
        addDefault(pConfig,"Back","shop","free","ShopMenu",null,backMenuItem,"&6Leaving the ExampleShop...",18,"");
        pConfig.saveToFile(pFile);
    }

    public void createShopItemsMenu(File f,CommentedYamlConfig config){
        config.set("signs.text","[ShopMenu]");
        config.set("signs.NeedPermissionToCreateSign",false);
        config.createSection("shop");
        List<String> exampleShopMenuItem=new ArrayList<String>();
        exampleShopMenuItem.add("type:TNT");
        exampleShopMenuItem.add("amount:1");
        exampleShopMenuItem.add("name:&1ExampleShop");
        exampleShopMenuItem.add("lore:&6Click me!");
        addDefault(config,"ExampleShop","shop","free","ExampleShop",null,exampleShopMenuItem,"&6Opening ExampleShop...",1,"");
        List<String> spellShopMenuItem=new ArrayList<String>();
        spellShopMenuItem.add("type:WRITTEN_BOOK");
        spellShopMenuItem.add("amount:1");
        spellShopMenuItem.add("name:&5SpellShop");
        spellShopMenuItem.add("lore:&8Click me!");
        addDefault(config,"SpellShop","shop","free","SpellShop",null,spellShopMenuItem,"&6Opening SpellShop...",9,"");
        config.saveToFile(f);
    }

    public void createShopItemsSpell(File f,CommentedYamlConfig config){
        config.set("signs.text","[SpellShop]");
        config.set("signs.NeedPermissionToCreateSign",false);
        config.createSection("shop");
        List<String> exampleShopMenuItem=new ArrayList<String>();
        exampleShopMenuItem.add("type:WRITTEN_BOOK");
        exampleShopMenuItem.add("amount:1");
        exampleShopMenuItem.add("name:&4Fireball");
        exampleShopMenuItem.add("lore:&8Allows you to shoot Fireballs! #&cPrice: 10 Levels");
        List<String> perms=new ArrayList<String>();
        perms.add("Plugin.Fireball");
        addDefault(config,"SpellFireball","permission","exp",perms,10,exampleShopMenuItem,"&6Opening ExampleShop...",1,"");
        List<String> backMenuItem=new ArrayList<String>();
        backMenuItem.add("type:REDSTONE");
        backMenuItem.add("amount:1");
        backMenuItem.add("name:&4Back");
        backMenuItem.add("lore:&8Go back to the Menu");
        addDefault(config,"Back","shop","free","ShopMenu",null,backMenuItem,"&6Leaving the SpellShop...",9,"");
        config.saveToFile(f);
    }

    private boolean simplePointsPluginCheck(){
        if(Bukkit.getPluginManager().getPlugin("PlayerPoints")!=null){
            return true;
        }
        if(Bukkit.getPluginManager().getPlugin("PointsAPI")!=null){
            return true;
        }
        if(Bukkit.getPluginManager().getPlugin("CommandPoints")!=null){
            return true;
        }
        if(Bukkit.getPluginManager().getPlugin("EnjinMinecraftPlugin")!=null){
            return true;
        }
        if(Bukkit.getPluginManager().getPlugin("BossAPI")!=null){
            return true;
        }
        return false;
    }

}
