package cc.bukkitPlugin.bossshop.mulServer.packet.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.black_ixx.bossshop.BossShop;
import org.black_ixx.bossshop.core.BSGoods;
import org.black_ixx.bossshop.core.BSShop;
import org.black_ixx.bossshop.core.BSShopManager;

import cc.bukkitPlugin.bossshop.mulServer.ServerLink;
import cc.bukkitPlugin.bossshop.mulServer.packet.APacket;
import cc.bukkitPlugin.bossshop.mulServer.packet.server.SPSendMessae;


public class CPBuyItem extends APacket{

    private String mPlayerName;
    private String mShopName;
    private String mBuyName;
    private boolean mRightBuy;
    
    public CPBuyItem(BossShop pPlugin){
        super(pPlugin);
    }
    
    
    public CPBuyItem(BossShop pPlugin,String pPlayerName,String pShopName,String pBuyName,boolean pRightBuy){
        this(pPlugin);
        this.mPlayerName=pPlayerName;
        this.mShopName=pShopName;
        this.mBuyName=pBuyName;
        this.mRightBuy=pRightBuy;
    }

    @Override
    public void handle(ServerLink pLink){
        BSShop tShop=this.mPlugin.getManager(BSShopManager.class).getShop(this.mShopName);
        if(tShop==null){
            pLink.sendPacket(new SPSendMessae(this.mPlugin,this.mPlayerName,this.mPlugin.C("MsgNoShopFound")+" ["+this.mShopName+"]"));
            return;
        }
        BSGoods tGoods=tShop.getGoods(this.mBuyName);
        if(tGoods==null){
            pLink.sendPacket(new SPSendMessae(this.mPlugin,this.mPlayerName,this.mPlugin.C("MsgNoGoodsFound")+" ["+this.mBuyName+"]"));
            return;
        }
        //TODO
        /**
         * 现获取可以购买的数量,
         * 如果为0,检查是否有消息缓存
         * 如果不为0,发送价格收取包
         */

    }

    @Override
    public void readContent(DataInputStream pStream) throws IOException{
        this.mPlayerName=pStream.readUTF();
        this.mShopName=pStream.readUTF();
        this.mBuyName=pStream.readUTF();
        this.mRightBuy=pStream.readBoolean();
    }

    @Override
    public void writeContent(DataOutputStream pStream) throws IOException{
        pStream.writeUTF(this.mPlayerName);
        pStream.writeUTF(this.mShopName);
        pStream.writeUTF(this.mBuyName);
        pStream.writeBoolean(this.mRightBuy);
    }

}
