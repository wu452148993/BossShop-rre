package cc.bukkitPlugin.bossshop.nbt;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSShopManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import cc.bukkitPlugin.bossshop.mail.MailManager;
import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTSerializer;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.exception.NBTDeserializeException;
import cc.bukkitPlugin.commons.plugin.INeedReload;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.AFileManager;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.IConfigModel;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.commentedyaml.CommentedValue;
import cc.commons.commentedyaml.CommentedYamlConfig;

/**
 * NBT管理系统,用于NBT的创建,保存和恢复
 * 
 * @author 聪聪
 */
public class NBTEditManager extends AFileManager<BossShop> implements IConfigModel,INeedReload{

    public static final String SEC_NBT_MAIN="NBTS";
    public static final String SEC_NBT_CONTENT="Content";
    public static final String SEC_AUTO_ADD="AutoAdd";
    public static final String SEC_LAST_USE_TIME="LastUseTime";

    private final HashMap<String,NBT> mNBTMap=new HashMap<>();
    private int mExpiredDays=7;
    private boolean mAllNBTNotAutoAdd=false;

    /**
     * 无初始化限制条件
     * 
     * @param pPlugin
     */
    public NBTEditManager(BossShop pPlugin){
        super(pPlugin,"nbt.yml","1.2");

        this.mPlugin.getConfigManager().registerConfigModel(this);
        this.mPlugin.registerReloadModel(this);
    }

    @Override
    public void setConfig(CommandSender pSender,CommentedYamlConfig pConfig){
        CommentedYamlConfig tConfig=this.mPlugin.getConfigManager().getConfig();
        this.mExpiredDays=tConfig.getInt("AutoClear.NBTExpiredDays",this.mExpiredDays);
        if(this.mExpiredDays<=0){
            this.mExpiredDays=Integer.MAX_VALUE;
        }
    }

    /**
     * 切勿单独调用,防止NBT引用次数被重置
     */
    public boolean reloadConfig(CommandSender pSender){
        super.reloadConfig(pSender);
        this.checkUpdate();
        this.generateNBT();
        this.saveConfig(pSender);
        return true;
    }

    /**
     * 检查配置文件版本,用以更新配置文件 当前更新范围 无版本->1.1
     */
    protected boolean checkUpdate(){
        boolean update=super.checkUpdate();
        if(!update)
            return false;
        String tversion=this.mConfig.getString(SEC_CFG_VERSION,"1.0");
        if(tversion.compareTo("1.1")<0){
            tversion="1.1";
            this.mConfig.set("index",null);
            CommentedSection tMainSec=this.mConfig.getOrCreateSection(SEC_NBT_MAIN);
            for(String sKey : tMainSec.getKeys(false)){
                CommentedValue tNBTSec=tMainSec.removeCommentedValue(sKey);
                tMainSec.set(sKey+"."+SEC_NBT_CONTENT,tNBTSec);
                tMainSec.set(sKey+"."+SEC_AUTO_ADD,false);
            }
        }
        if(tversion.compareTo("1.2")<0){
            tversion="1.2";
            CommentedSection tMainSec=this.mConfig.getOrCreateSection(SEC_NBT_MAIN);
            for(String sKey : tMainSec.getKeys(false)){
                CommentedSection tNBTSec=tMainSec.getSection(sKey);
                if(tNBTSec!=null&&(tNBTSec=tNBTSec.getSection(SEC_NBT_CONTENT))!=null){
                    NBTEditManager.update_V1_2(tNBTSec);
                }
            }
        }
        this.mConfig.set(SEC_CFG_VERSION,this.mVersion);
        return true;
    }

    private static CommentedSection update_V1_2(CommentedSection pSection){
        for(String sKey : pSection.getKeys(false)){
            CommentedValue tValue=pSection.removeCommentedValue(sKey);
            int tTypeId=0;
            Object tNewContent=null;

            if(tValue.getValue() instanceof CommentedSection){
                CommentedSection tChildSec=(CommentedSection)tValue.getValue();
                if(tChildSec.getInt("_type_",0)==9){
                    tTypeId=9;
                }else{
                    tTypeId=10;
                }
                tNewContent=NBTEditManager.update_V1_2(tChildSec);
            }else{
                String tValueStr=String.valueOf(tValue.getValue());
                String[] pParts=tValueStr.split("[|]",2);
                try{
                    tTypeId=Integer.valueOf(pParts[0]);
                    tNewContent=pParts[1];
                }catch(NumberFormatException|ArrayIndexOutOfBoundsException exp){
                    continue;
                }
            }
            pSection.set(String.format("%02d|",tTypeId)+sKey,tNewContent,tValue.getComments().toArray(new String[0]));
        }
        return pSection;
    }

    /**
     * 清理自动生成且引用次数为0的NBT节点
     * 
     * @return 清理的个数
     */
    public int clearNBT(CommandSender pSender){
        if(this.mNBTMap.isEmpty()||this.mAllNBTNotAutoAdd)
            return 0;

        //先检查商店和邮件是否在范围时间内引用了NBT,如果没有则重载来增加NBT最后引用时间
        //数字为了防止int越界,同时保证清理时间的精度
        MailManager mailMan=((BossShop)this.mPlugin).getManager(MailManager.class);
        if((System.currentTimeMillis()-mailMan.getLastReloadTime())/86400000>=this.mExpiredDays&&this.mExpiredDays>0)
            mailMan.reloadConfig(null);
        BSShopManager shopMan=((BossShop)this.mPlugin).getManager(BSShopManager.class);
        if((System.currentTimeMillis()-shopMan.getLastReloadTime())/86400000>=this.mExpiredDays&&this.mExpiredDays>0)
            shopMan.reloadConfig(null);

        int count=0;
        CommentedSection tNBTSec=this.mConfig.getOrCreateSection(SEC_NBT_MAIN);
        Iterator<Entry<String,NBT>> it=this.mNBTMap.entrySet().iterator();
        while(it.hasNext()){
            Entry<String,NBT> entry=it.next();
            NBT nbt=entry.getValue();
            if(nbt.autoadd){
                if((System.currentTimeMillis()-nbt.lastUseTime)/86400000>this.mExpiredDays){
                    tNBTSec.set(entry.getKey(),null);
                    it.remove();
                    count++;
                }else{
                    this.mAllNBTNotAutoAdd=false;
                }
            }
        }
        if(count>0){
            this.saveConfig(null);
        }
        if((pSender instanceof Player)||count>0){
            Log.send(pSender,this.C("MsgClearedNoUsefulNBT").replace("%numb%",count+""));
        }
        return count;
    }

    /**
     * 添加物品的NBT到库存
     * 
     * @param pItem
     *            要获取NBT的物品
     * @param pAuto
     *            是否是自动添加的
     * @return 添加成功后存储在文件中的NBT节点名,如果物品没有nbt信息,将返回null
     */
    public String addItemNBTToStock(ItemStack pItem,boolean pAuto){
        Object tNBTTag=NBTUtil.getItemNBT(pItem);
        if(tNBTTag==null)
            return null;
        Object tNBTTagClone=NBTUtil.invokeNBTTagCompound_clone(tNBTTag); //克隆方法
        Map<String,Object> tagContent=NBTUtil.getNBTTagCompoundValue(tNBTTagClone);
        if(tagContent==null||tagContent.isEmpty())
            return null;
        NBT addNBT=this.getNBTByTag(tNBTTagClone);
        if(addNBT==null){
            long time=System.currentTimeMillis()/1000;
            String label=time+"_"+pItem.getType().name();
            while(this.mConfig.contains(label)){
                time++;
                label=time+"_"+pItem.getType().name();
            }
            addNBT=new NBT(label,tNBTTagClone,pAuto);
            this.mNBTMap.put(label,addNBT);
        }
        this.updateNBTLastTime(addNBT);
        this.mAllNBTNotAutoAdd&=!pAuto;
        return addNBT.mLabel;
    }

    /**
     * 更新NBT最后引用时间(配置文件和自身实例)
     * <p>
     * 如果NBT配置节点不存在会自动创建
     * </p>
     * 
     * @param pNBT
     *            要更新的NBT
     */
    public void updateNBTLastTime(NBT pNBT){
        CommentedSection sec=this.mConfig.getOrCreateSection(SEC_NBT_MAIN);
        CommentedSection nsec=sec.getSection(pNBT.mLabel);
        if(nsec==null){
            nsec=sec.createSection(pNBT.mLabel);
            nsec.set(SEC_AUTO_ADD,pNBT.autoadd);
            CommentedSection content_sec=nsec.createSection(SEC_NBT_CONTENT);
            NBTSerializer.serializeNBTToYaml_Tag(pNBT.nbt,content_sec);

        }
        pNBT.lastUseTime=System.currentTimeMillis();
        nsec.set(SEC_LAST_USE_TIME,pNBT.lastUseTime);
        this.saveConfig(null);
    }

    /**
     * 根据pItem来获取NBT类实例
     * 
     * @param pItem
     *            Bukkit物品
     * @return 找到的NBl类实例或null
     */
    public NBT getNBTByItem(ItemStack pItem){
        return this.getNBTByTag(NBTUtil.getItemNBT(pItem));
    }

    /**
     * 根据NBTTagCompound来获取NBT类实例
     * 
     * @param pTag
     * @return 找到的NBT类实例或null
     */
    private NBT getNBTByTag(Object pTag){
        if(pTag==null)
            return null;
        for(NBT sNBT : this.mNBTMap.values()){
            if(sNBT.nbt.equals(pTag))
                return sNBT;
        }
        return null;
    }

    /**
     * 从配置文件中生成NBT
     */
    private void generateNBT(){
        this.mNBTMap.clear();
        CommentedSection tMainSec=this.mConfig.getOrCreateSection(SEC_NBT_MAIN);
        for(String label : tMainSec.getKeys(false)){
            CommentedSection tNBTSec=tMainSec.getSection(label);
            if(label==null)
                continue;

            boolean tAuto=tNBTSec.getBoolean(NBTEditManager.SEC_AUTO_ADD,false);
            CommentedSection tNBTContentSec=tNBTSec.getSection(NBTEditManager.SEC_NBT_CONTENT);
            if(tNBTContentSec==null){
                Log.severe("NBT配置节点["+label+"]没有["+NBTEditManager.SEC_NBT_CONTENT+"]节点,这是一个配置错误");
                continue;
            }
            Object tNBTTag=null;
            try{
                tNBTTag=NBTSerializer.deserializeNBTFromYaml(tNBTContentSec);
            }catch(NBTDeserializeException exp){
                Log.severe("反序列化NBT时发生了错误",exp);
                continue;
            }
            NBT cfgNBT=new NBT(label,tNBTTag,tAuto);
            cfgNBT.lastUseTime=tNBTSec.getLong(NBTEditManager.SEC_LAST_USE_TIME,System.currentTimeMillis());
            if(cfgNBT.lastUseTime>System.currentTimeMillis()){
                cfgNBT.lastUseTime=System.currentTimeMillis();
                tNBTSec.set(NBTEditManager.SEC_LAST_USE_TIME,cfgNBT.lastUseTime);
            }
            this.mNBTMap.put(label,cfgNBT);
            this.mAllNBTNotAutoAdd&=!tAuto;

        }
    }

    /**
     * 使用该方法是必须接受返回结果
     * 
     * @param pNBTLabel
     *            nbt标签,所有标签在数据文件夹下的nbt.yml中的NBTS节点下
     * @param pUpdateUseTime
     *            是否更新NBT引用时间
     * @return 标签对应的NBT
     */
    public NBT getItemNBT(String pNBTLabel,boolean pUpdateUseTime){
        NBT tNBT=this.mNBTMap.get(pNBTLabel);
        if(tNBT!=null&&pUpdateUseTime){
            this.updateNBTLastTime(tNBT);
        }
        return tNBT;
    }

    @Override
    public void addDefaults(CommentedYamlConfig pConfig){}

}
