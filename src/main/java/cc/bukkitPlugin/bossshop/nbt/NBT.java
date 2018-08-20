package cc.bukkitPlugin.bossshop.nbt;

import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;

public class NBT{
    /**
     * 此处的值类型为NMS的NBTTagCompound
     */
    public final String mLabel;
    protected final Object nbt;
    protected boolean autoadd=false;
    protected long lastUseTime=System.currentTimeMillis();
    
    NBT(String pLabel,Object pnbt,boolean pauto){
        this.mLabel=pLabel;
        this.nbt=pnbt;
        this.autoadd=pauto;
    }
    
    public Object getNBTCopy(){
        return NBTUtil.invokeNBTTagCompound_clone(nbt);
    }
}