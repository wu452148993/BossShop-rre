package cc.bukkitPlugin.bossshop.mail;

import java.util.ArrayList;
import java.util.List;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSEnums.RewardType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import cc.bukkitPlugin.bossshop.goods.reward.IReward;
import cc.bukkitPlugin.bossshop.goods.reward.RewardItem;
import cc.bukkitPlugin.bossshop.sale.SaleManager;

public class MailItem{

    private final String mName;
    /** 过期物品 */
    private final IReward mReward;
    /** 物品来源 */
    private final String mSource;
    /** 缓存的物品 */
    private ItemStack mCache=null;

    /**
     * 构造一个Mail物品
     * 
     * @param pItem
     *            物品,允许Integer,List<ItemStack> ItemStack
     * @param pSaleType
     *            物品类型,只支持Item,points,money
     * @param pCount
     *            数量
     * @param pSource
     *            来源,用于生成预览
     */
    public MailItem(String pName,IReward pReward,String pSource){
        this.mName=pName;
        this.mReward=pReward;
        this.mSource=pSource;
    }

    public String getName(){
        return this.mName;
    }

    public String getPreview(){
        return this.mReward.getDescription(1);
    }

    public String getSource(){
        return this.mSource;
    }

    /** 邮件列表中的物品统一为一件 */
    public IReward getItem(){
        return this.mReward;
    }

    public RewardType getItemType(){
        return this.mReward.getRewardType();
    }

    public ItemStack generateItem(Player pOwner){
        if(this.mCache==null){
            ItemStack tItem=null;
            SaleManager tSaleMan=BossShop.getInstance().getManager(SaleManager.class);
            if(this.getItemType()==RewardType.Money){
                tItem=tSaleMan.getMoneyItem().clone();
            }else if(this.getItemType()==RewardType.Points){
                tItem=tSaleMan.getPointsItem().clone();
            }else if(this.getItemType()==RewardType.Item){
                tItem=((RewardItem)this.getItem()).getReward(1).get(0);
            }else throw new IllegalArgumentException("Unsupported mail item type");

            ItemMeta tMeta=tItem.getItemMeta();
            tMeta.setDisplayName(this.getPreview());

            List<String> tLore=tMeta.hasLore()?tMeta.getLore():new ArrayList<String>();
            tLore.add(this.getSource());
            tMeta.setLore(tLore);

            tItem.setItemMeta(tMeta);
            this.mCache=tItem;
        }
        return this.mCache;
    }

}
