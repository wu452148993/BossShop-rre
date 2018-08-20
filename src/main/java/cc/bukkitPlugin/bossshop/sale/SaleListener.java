package cc.bukkitPlugin.bossshop.sale;

import java.util.ArrayList;
import java.util.HashMap;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.tellraw.ChatStyle;
import cc.bukkitPlugin.commons.tellraw.ClickEvent;
import cc.bukkitPlugin.commons.tellraw.Color;
import cc.bukkitPlugin.commons.tellraw.Format;
import cc.bukkitPlugin.commons.tellraw.HoverEvent;
import cc.bukkitPlugin.commons.tellraw.Tellraw;
import cc.commons.util.StringUtil;

public class SaleListener implements Listener{

    private BossShop mPlugin;
    private HashMap<Player,SaleParameter> mForSale=new HashMap<>();

    public SaleListener(BossShop pPlugin){
        this.mPlugin=pPlugin;
        this.mPlugin.getServer().getPluginManager().registerEvents(this,this.mPlugin);
    }

    private String C(String pNode){
        return this.mPlugin.C(pNode);
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerChat(PlayerChatEvent pEvent){
        synchronized(this){
            if(!this.needHandle(pEvent.getPlayer(),pEvent.getMessage())) return;
            pEvent.setCancelled(true);
            pEvent.getRecipients().clear();
            this.handlePlayerMsg(pEvent.getPlayer(),pEvent.getMessage());
        }
    }

    protected boolean needHandle(Player sender,String pMsg){
        if(sender==null||StringUtil.isEmpty(pMsg)) return false;
        if(pMsg.startsWith("/")) return false; //跳过命令
        SaleParameter tParam=this.mForSale.get(sender);
        if(tParam==null) return false;
        if(tParam.mStep==SaleStep.Finish){
            this.stopSaleListenrer(sender);
            return false;
        }
        return true;
    }

    /**
     * 处理玩家发送的消息
     * <p>
     * 调用前,请先使用{@link SaleListener#needHandle(Player, String)}检查是否应该处理
     * </p>
     * 
     * @param pSender
     *            消息发送者
     * @param pMsg
     *            发送的消息
     */
    protected void handlePlayerMsg(Player pSender,String pMsg){
        SaleParameter tParam=this.mForSale.get(pSender);
        if(this.handleStep(pSender,tParam,pMsg)){
            tParam.mStep=SaleStep.getNext(tParam.mStep);
        }
        this.showStepIntroduce(pSender,tParam);
        if(tParam.mStep==SaleStep.Finish)
            this.mForSale.remove(pSender);
    }

    public SaleParameter stopSaleListenrer(Player pPlayer){
        SaleParameter removeParam=this.mForSale.remove(pPlayer);
        if(removeParam==null){
            //Log.ABukkitPlugin(pPlayer,"你没有开始寄售");
        }else{
            Log.send(pPlayer,C("MsgChatSaleExit"));
        }
        return removeParam;
    }

    public void startSaleListenrer(Player pPlayer){
        SaleParameter tp=this.mForSale.get(pPlayer);
        if(tp!=null){
            if(tp.mStep==SaleStep.Finish){
                tp.mStep=SaleStep.GoodsType;
            }else{
                Log.send(pPlayer,C("MsgChatSaleRepartStart"));
                return;
            }
        }else{
            tp=new SaleParameter();
            this.mForSale.put(pPlayer,tp);
        }
        Log.send(pPlayer,C("MsgChatSaleStart"));
        this.showStepIntroduce(pPlayer,tp);
    }

    /**
     * 显示当前寄售步奏的提示
     * 
     * @param pPlayer
     *            用户名
     * @param pStep
     *            参数
     */
    public void showStepIntroduce(Player pPlayer,SaleParameter pParam){
        switch(pParam.mStep){
            case GoodsType:{
                ArrayList<String> disName=new ArrayList<>();
                ArrayList<String> chatTyprMsg=new ArrayList<>();
                for(String sType : new String[]{"Item","Money","Points"}){
                    if(pPlayer.hasPermission(this.mPlugin.getName()+".sale.type."+sType.toLowerCase())){
                        disName.add(C("Word"+sType));
                        chatTyprMsg.add(sType);
                    }
                }
                if(disName.isEmpty()){
                    Log.send(pPlayer,C("MsgYouCannotSaleAnyThing"));
                    this.stopSaleListenrer(pPlayer);
                    return;
                }
                this.showSelect(pPlayer,
                        Tellraw.cHead(Log.getMsgPrefix()+" "+C("MsgIntroduceSellType")),
                        disName.toArray(new String[0]),
                        chatTyprMsg.toArray(new String[0]));
                break;
            }
            case SingleNumb:
                Log.send(pPlayer,C("MsgIntroduceSingleNumb"));
                break;
            case PartNumb:
                Log.send(pPlayer,C("MsgIntroducePartNumb"));
                break;
            case PriceType:{
                ArrayList<String> disName=new ArrayList<>();
                ArrayList<String> chatTyprMsg=new ArrayList<>();
                for(String sType : new String[]{"Money","Points"}){
                    if(pPlayer.hasPermission(this.mPlugin.getName()+".sale.pricetype."+sType.toLowerCase())){
                        disName.add(C("Word"+sType));
                        chatTyprMsg.add(sType);
                    }
                }
                if(disName.isEmpty()){
                    Log.send(pPlayer,C("MsgYouCanSaleNoprice"));
                    this.stopSaleListenrer(pPlayer);
                    return;
                }
                this.showSelect(pPlayer,
                        Tellraw.cHead(Log.getMsgPrefix()+" "+C("MsgIntroducePriceType")),
                        new String[]{C("WordMoney"),C("WordPoints")},
                        new String[]{"money","points"});
                break;
            }
            case Price:
                Log.send(pPlayer,C("MsgIntroducePrice"));
                break;
            case Finish:
                Log.send(pPlayer,C("MsgChatSaleFinish"));
                Tellraw chat=Tellraw.cHead(Log.getMsgPrefix()+" "+C("MsgClickSaleButtonFor"));
                Tellraw saleButton=new Tellraw(C("WordForSale"),Color.blue);
                saleButton.getChatStyle().setFormat(Format.bold,Format.underline);
                saleButton.getChatStyle().setClickEvent(ClickEvent.Action.run_command,this.getSaleCommand(pParam));
                saleButton.getChatStyle().setHoverEvent(HoverEvent.Action.show_text,this.getSaleHover(pParam));
                chat.addExtra(saleButton).sendToPlayer(pPlayer);
                break;
        }
    }

    private String getSaleCommand(SaleParameter pParam){
        StringBuilder builder=new StringBuilder("/bossshop sale ");
        builder.append(pParam.saleType.name()+" ");
        builder.append(pParam.singleNumb+" ");
        builder.append(pParam.partNumb+" ");
        builder.append(pParam.priceType.name()+" ");
        builder.append(pParam.price);
        return builder.toString();
    }

    private String getSaleHover(SaleParameter pParam){
        String color=C("MsgNowSaleParameterColor");
        if(!color.startsWith("§")) color="§a";
        StringBuilder builder=new StringBuilder(color+C("MsgNowSaleParameter")+"\n");
        builder.append(color+C("WordSellType")+": "+C(pParam.saleType.getNameKey())+"\n");
        builder.append(color+C("WordSigleNumb")+": "+pParam.singleNumb+"\n");
        builder.append(color+C("WordPartNumb")+": "+pParam.partNumb+"\n");
        builder.append(color+C("WordPrice")+": "+pParam.price+" "+C(pParam.priceType.getNameKey()));
        return builder.toString();
    }

    /**
     * 处理当前寄售步奏的玩家输入
     * <p>
     * 错误消息会在本函数中立刻显示
     * </p>
     * 
     * @param pPlayer
     *            用户名
     * @param pParam
     *            已经处理的参数
     * @param pMsg
     *            当前参数的输入
     * @return 是否处理成功
     */
    public boolean handleStep(Player pPlayer,SaleParameter pParam,String pMsg){
        SaleManager saleMan=this.mPlugin.getManager(SaleManager.class);
        boolean result=false;
        String successMsg=C("MsgYouSetSaleParamTo");
        switch(pParam.mStep){
            case GoodsType:
                if(!(result=saleMan.handleGoodsTypeInput(pParam,pPlayer,pMsg)))
                    Log.send(pPlayer,pParam.errorInfo);
                else{
                    successMsg=successMsg.replace("%param%",C("WordSellType")).replace("%value%",C(pParam.saleType.getNameKey()));
                    Log.send(pPlayer,successMsg);
                }
                return result;
            case SingleNumb:
                if(!(result=saleMan.handleSingleNumbInput(pParam,pPlayer,pMsg)))
                    Log.send(pPlayer,pParam.errorInfo);
                else{
                    successMsg=successMsg.replace("%param%",C("WordSigleNumb")).replace("%value%",pParam.singleNumb+"");
                    Log.send(pPlayer,successMsg);
                }
                return result;
            case PartNumb:
                if(!(result=saleMan.handlePartNumbInput(pParam,pPlayer,pMsg)))
                    Log.send(pPlayer,pParam.errorInfo);
                else{
                    successMsg=successMsg.replace("%param%",C("WordPartNumb")).replace("%value%",pParam.partNumb+"");
                    Log.send(pPlayer,successMsg);
                }
                return result;
            case PriceType:
                if(!(result=saleMan.handlePriceTypeInput(pParam,pPlayer,pMsg)))
                    Log.send(pPlayer,pParam.errorInfo);
                else{
                    successMsg=successMsg.replace("%param%",C("WordPriceType")).replace("%value%",C(pParam.priceType.getNameKey()));
                    Log.send(pPlayer,successMsg);
                }
                return result;
            case Price:
                if(!(result=saleMan.handlePriceInput(pParam,pPlayer,pMsg)))
                    Log.send(pPlayer,pParam.errorInfo);
                else{
                    successMsg=successMsg.replace("%param%",C("WordPrice")).replace("%value%",pParam.price+"");
                    Log.send(pPlayer,successMsg);
                }
                return result;
            case Finish:
                throw new IllegalArgumentException("在聊天栏寄售状态为Finish时,不能处理输入参数");
        }
        return false;
    }

    /**
     * 使用指定参数显示一个可点击的选项消息
     * 
     * @param pPlayer
     *            发送给谁
     * @param pMsg
     *            消息前缀
     * @param pSelectDisName
     *            选项显示名字
     * @param pSelect
     *            选项值
     */
    protected void showSelect(Player pPlayer,Tellraw pMsgRaw,String[] pSelectDisName,String[] pSelect){
        ChatStyle tStyle=new ChatStyle(Color.gold).setFormat(Format.bold,Format.underline);
        for(int i=0;i<pSelectDisName.length;i++){
            Tellraw detailGoodsType=new Tellraw(pSelectDisName[i]);
            detailGoodsType.getChatStyle().copyFrom(tStyle);
            detailGoodsType.getChatStyle().setClickEvent(ClickEvent.Action.run_command,pSelect[i]);
            detailGoodsType.getChatStyle().setHoverEvent(HoverEvent.Action.show_text,Color.gold+pSelect[i]);
            pMsgRaw.addExtra(detailGoodsType);
            if(i!=pSelectDisName.length-1)
                pMsgRaw.addText(", ");
        }
        pMsgRaw.sendToPlayer(pPlayer);
    }
}
