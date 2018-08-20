package cc.bukkitPlugin.bossshop.numbkey;

import java.util.HashMap;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSGoods;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.manager.AManager;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.IConfigModel;
import cc.commons.commentedyaml.CommentedSection;
import cc.commons.commentedyaml.CommentedYamlConfig;

public class NumbKeyManager extends AManager<BossShop> implements IConfigModel{

    private HashMap<Integer,AFunction> mBindFounction=new HashMap<>();
    private HashMap<String,AFunction> mFounctions=new HashMap<>();

    public NumbKeyManager(BossShop pPlugin){
        super(pPlugin);

        this.mPlugin.getConfigManager().registerConfigModel(this);

        this.registerFunction(new FCLeftClickBuyGoods(this.mPlugin));
        this.registerFunction(new FCRightClickBuyGoods(this.mPlugin));
        this.registerFunction(new FCShiftLeftClickBuyGoods(this.mPlugin));
        this.registerFunction(new FCUnsaleGoods(this.mPlugin));
    }

    @Override
    public void setConfig(CommandSender pSender,CommentedYamlConfig pConfig){
        CommentedYamlConfig tConfig=this.mPlugin.getConfigManager().getConfig();
        this.mBindFounction.clear();
        CommentedSection numbKeySection=tConfig.getOrCreateSection("NumbKey");
        for(int i=1;i<9;i++){
            String tValue=numbKeySection.getString("NumbKey@"+i);
            if(tValue==null)
                continue;
            AFunction tFunction=this.getFunction(tValue);
            if(tFunction==null){
                Log.severe(pSender,this.mPlugin.C("MsgErrorNumbKeyConfigFunctionNotFound").replace("%key%",i+"").replace("%function%",tValue));
                Log.severe(pSender,this.mPlugin.C("MsgAllNumbKeyFunctionIs")+this.mFounctions.keySet());
            }
            this.mBindFounction.put(i,tFunction);
        }
    }

    public void doFunction(int pNumbKey,Player pPlayer,BSGoods pReward){
        AFunction tFunction=this.mBindFounction.get(pNumbKey);
        if(tFunction!=null){
            tFunction.doFunction(pPlayer,pReward);
        }
    }

    protected void registerFunction(AFunction pFunction){
        this.mFounctions.put(pFunction.getFunctionName().toUpperCase(),pFunction);
    }

    public AFunction getFunction(String pName){
        return this.mFounctions.get(pName.toUpperCase());
    }

    @Override
    public void addDefaults(CommentedYamlConfig pConfig){

    }

}
