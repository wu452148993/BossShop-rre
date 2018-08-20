package cc.bukkitPlugin.bossshop.goods.price;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.PriceType;
import org.black_ixx.bossshop.managers.ItemNameManager;
import org.black_ixx.bossshop.managers.ItemStackCreator;
import org.black_ixx.bossshop.managers.WorthHandler;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import cc.bukkitPlugin.commons.Log;
import cc.commons.util.StringUtil;
import lombok.val;

public class PriceItem implements IPrice{

    public static abstract class MatchKind{

        private static List<MatchKind> mValues=new ArrayList<>();

        public static MatchKind Type=new MatchKind("type","id"){

            @Override
            public boolean isMatch(ItemStack pItem1,ItemStack pItem2){
                return pItem1.getType()==pItem2.getType();
            }
        };
        public static MatchKind Damage=new MatchKind("damage","meta"){

            @Override
            public boolean isMatch(ItemStack pItem1,ItemStack pItem2){
                return pItem1.getDurability()==pItem2.getDurability();
            }
        };
        public static MatchKind Name=new MatchKind("name","displayname"){

            @Override
            public boolean isMatch(ItemStack pItem1,ItemStack pItem2){
                ItemNameManager tLang=BossShop.getInstance().getManager(ItemNameManager.class);
                return StringUtil.compareTo(tLang.getDisplayName(pItem1),tLang.getDisplayName(pItem2))==0;
            }
        };
        public static MatchKind Lore=new MatchKind("lore"){

            @Override
            public boolean isMatch(ItemStack pItem1,ItemStack pItem2){
                ItemMeta tMeta=null;
                List<String> tLore1=(pItem1.hasItemMeta()&&(tMeta=pItem1.getItemMeta()).hasLore())?tMeta.getLore():new ArrayList<>();
                List<String> tLore2=(pItem2.hasItemMeta()&&(tMeta=pItem2.getItemMeta()).hasLore())?tMeta.getLore():new ArrayList<>();
                return tLore1.equals(tLore2);
            }

        };
        public static MatchKind Enchant=new MatchKind("enchant","enchantment"){

            @Override
            public boolean isMatch(ItemStack pItem1,ItemStack pItem2){
                ItemMeta tMeta1=pItem1.hasItemMeta()?pItem1.getItemMeta():null;
                ItemMeta tMeta2=pItem2.hasItemMeta()?pItem2.getItemMeta():null;

                if((tMeta1==null||!tMeta1.hasEnchants())&&(tMeta2==null||!tMeta2.hasEnchants()))
                    return true;
                if(tMeta1.getEnchants().size()!=tMeta2.getEnchants().size())
                    return false;

                for(Entry<Enchantment, Integer> sEntry : tMeta1.getEnchants().entrySet()){
                    if(tMeta2.getEnchantLevel(sEntry.getKey())!=sEntry.getValue())
                        return false;
                }
                return true;
            }

        };
        public static MatchKind All=new MatchKind("all","any","*"){

            @Override
            public boolean isMatch(ItemStack pItem1,ItemStack pItem2){
                return pItem1.isSimilar(pItem2);
            }

        };

        private LinkedHashSet<String> mAllNames=new LinkedHashSet<>();

        private MatchKind(String...pAliase){
            for(String sAlise : pAliase){
                this.mAllNames.add(sAlise.toLowerCase());
            }
            MatchKind.mValues.add(this);
        }

        @Override
        public String toString(){
            return this.mAllNames.iterator().next();
        }
        
        public abstract boolean isMatch(ItemStack pItem1,ItemStack pItem2);

        public static List<MatchKind> values(){
            return Collections.unmodifiableList(MatchKind.mValues);
        }

        public static List<MatchKind> getKinds(String pStr){
            List<MatchKind> tKinds=new ArrayList<>();
            for(String sKindStr : StringUtil.splitNoEmpty(pStr,',')){
                MatchKind tKind=getKind(sKindStr);
                if(tKind!=null) tKinds.add(tKind);
            }
            return tKinds;
        }

        public static MatchKind getKind(String pStr){
            pStr=pStr.trim().toLowerCase();
            for(MatchKind sKind : MatchKind.values()){
                for(String sName : sKind.mAllNames){
                    if(sName.equals(pStr)) return sKind;
                }
            }
            return null;
        }
    }

    public static class PriceInfo implements Cloneable{

        public ItemStack mPriceItem;
        public int mAmount=1;
        public List<MatchKind> mMatchKinds=new ArrayList<>();

        public PriceInfo(ItemStack pPriceItem,int pAmount,Collection<MatchKind> pMatchKinds){
            this.mPriceItem=pPriceItem;
            this.mAmount=pAmount;
            this.mMatchKinds.addAll(pMatchKinds);
        }

        @Override
        public PriceInfo clone(){
            return new PriceInfo(this.mPriceItem.clone(),this.mAmount,new ArrayList<>(this.mMatchKinds));
        }

    };

    private List<PriceInfo> mPrice;
    /** 该变量只在调用initPrice才会初始化 */
    private BossShop mPlugin;

    @Override
    public BossShop getPlugin(){
        return this.mPlugin;
    }
    
	@Override
	public List<PriceInfo> getPrice() {
		return this.mPrice;
	}
    
    @Override
    public PriceType getPriceType(){
        return PriceType.Item;
    }

    @Override
    public boolean hasPrice(Player pPlayer,double pMulCount){
        this.clearPlayerCache();
        return this.mPlugin.getManager(WorthHandler.class).hasItems(pPlayer,this.mulItem(pMulCount,pPlayer));
    }

    @Override
    public String takePrice(Player pPlayer,double pMulCount){
        return this.mPlugin.getManager(WorthHandler.class).takeItems(pPlayer,this.mulItem(pMulCount,pPlayer));
    }

    @Override
    public boolean initPrice(BossShop pPlugin,Object pObjPrice){
        this.mPlugin=pPlugin;
        this.clearPlayerCache();

        if(pObjPrice==null){
            Log.severe("未配置价格内容,价格["+this.mPlugin.C(this.getPriceType().getNameKey())+"]的内容时必须的");
            return false;
        }
        if(!(pObjPrice instanceof List<?>)){
            Log.severe("价格类型为物品时,配置必须为list(Item)");
            return false;
        }
        List<?> l=(List<?>)pObjPrice;
        boolean mul=false;
        for(Object o : l){
            if(o instanceof List<?>){
                mul=true;
                break;
            }
        }
        this.mPrice=new ArrayList<>();
        ItemStackCreator itemCteator=this.mPlugin.getManager(ItemStackCreator.class);
        try{
            List<List<String>> itemInfos;
            if(mul)
                itemInfos=(List<List<String>>)pObjPrice;
            else{
                itemInfos=new ArrayList<>();
                itemInfos.add((List<String>)pObjPrice);
            }
            for(List<String> s : itemInfos){
                ItemStack tItem=itemCteator.createItemStackS(s,false);
                if(tItem==null) return false;
                int count=tItem.getAmount();
                tItem.setAmount(1);
                HashSet<MatchKind> tKinds=new HashSet<>();
                boolean tFind=false;
                for(String sInfo : s){
                    if(sInfo.toLowerCase().startsWith("matchkinds:")){
                        tFind=true;
                        tKinds.addAll(MatchKind.getKinds(sInfo.substring("matchkinds:".length())));
                    }
                }
                if(tFind&&tKinds.isEmpty()) tKinds.add(MatchKind.All);
                this.mPrice.add(new PriceInfo(tItem,count,tFind?tKinds:this.mPlugin.getConfigManager().getPriceItemDefMatchKinds()));
            }
        }catch(Exception exp){
            return false;
        }
        return true;
    }

    private Player mLastPlayer=null;
    private ArrayList<PriceInfo> mLastPlayerPrice=null;

    public void clearPlayerCache(){
        this.mLastPlayer=null;
        this.mLastPlayerPrice=null;
    }

    private ArrayList<PriceInfo> mulItem(double pMulCount,Player pPlayer){
        if(pPlayer==this.mLastPlayer&&this.mLastPlayerPrice!=null)
            return this.mLastPlayerPrice;

        ArrayList<PriceInfo> tMulPrice=new ArrayList<>();
        for(PriceInfo sInfo : this.mPrice){
            sInfo=sInfo.clone();
            sInfo.mAmount=(int)Math.rint(sInfo.mAmount*pMulCount);
            sInfo.mPriceItem=WorthHandler.transformRewardItem(sInfo.mPriceItem,pPlayer);
            tMulPrice.add(sInfo);
        }

        this.mLastPlayer=pPlayer;
        return this.mLastPlayerPrice=tMulPrice;
    }

    @Override
    public IPrice copy(){
        PriceItem tPrice=new PriceItem();
        tPrice.mPlugin=this.mPlugin;
        tPrice.mPrice=new ArrayList<>();
        for(PriceInfo sInfo : this.mPrice){
            tPrice.mPrice.add(sInfo.clone());
        }
        return tPrice;
    }

    @Override
    public String getDescription(double pMulCount){
        ItemNameManager nameMan=this.mPlugin.getManager(ItemNameManager.class);
        StringBuilder description=new StringBuilder();
        String wordCount=this.mPlugin.C("WordCount");
        for(PriceInfo sInfo : this.mPrice){
            description.append((int)Math.rint(sInfo.mAmount*pMulCount))
                    .append(wordCount)
                    .append(nameMan.getDisplayName(sInfo.mPriceItem)).append(", ");
        }
        if(description.length()>2)
            description.delete(description.length()-2,description.length());
        return description.toString();
    }

}
