package cc.bukkitPlugin.bossshop.gui.slotlistener;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import cc.commons.util.interfaces.IFilter;

public class FilterItemSlot extends SlotListener{

    public final IFilter<ItemStack> mFilter;

    public FilterItemSlot(Inventory pInv,int pSlotIndex,IFilter<ItemStack> pFilter){
        super(pInv,pSlotIndex);
        if(pFilter==null)
            throw new NullPointerException("Filter not null");
        this.mFilter=pFilter;
    }

    @Override
    public boolean acceptThisItem(ItemStack pItem){
        return this.mFilter.accept(pItem);
    }

    @Override
    public boolean allowPopItem(){
        return true;
    }

}
