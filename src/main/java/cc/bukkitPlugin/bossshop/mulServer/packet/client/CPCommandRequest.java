package cc.bukkitPlugin.bossshop.mulServer.packet.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.Bukkit;

import cc.bukkitPlugin.bossshop.mulServer.ServerLink;
import cc.bukkitPlugin.bossshop.mulServer.WPlayer;
import cc.bukkitPlugin.bossshop.mulServer.packet.APacket;
import cc.bukkitPlugin.bossshop.mulServer.packet.server.SPSendMessae;
import cc.commons.util.StringUtil;


public class CPCommandRequest extends APacket{

    private String mSenderName;
    private WPlayer mSender;
    private String mCmdLabel;
    private String[] mCmdArgs;
    
    public CPCommandRequest(BossShop pPlugin){
        super(pPlugin);
    }
    
    /**
     * 构造一个命令使用包
     * @param pSender   命令发送者
     * @param pCmdArgs  命令完整参数
     */
    public CPCommandRequest(BossShop pPlugin,String pSenderName,String pCmdLabel,String[] pCmdArgs){
        this(pPlugin);
        this.mSenderName=pSenderName;
        this.mSender=new WPlayer(pSenderName);
        this.mCmdLabel=pCmdLabel;
        this.mCmdArgs=pCmdArgs;
    }
    
    @Override
    public void handle(ServerLink pLink){
        this.mPlugin.getCommandExc().onCommand(this.mSender,Bukkit.getPluginCommand(this.mCmdLabel),this.mCmdLabel,this.mCmdArgs);
        String tMsgs=this.mSender.getCacheMessage();
        if(StringUtil.isNotEmpty(tMsgs)){
            pLink.sendPacket(new SPSendMessae(this.mPlugin,this.mSenderName,tMsgs));
        }
    }

    @Override
    public void readContent(DataInputStream pStream) throws IOException{
        this.mSenderName=pStream.readUTF();
        this.mCmdLabel=pStream.readUTF();

        int tArgsLength=pStream.readInt();
        this.mCmdArgs=new String[tArgsLength];
        for(int i=0;i<tArgsLength;i++){
            this.mCmdArgs[i]=pStream.readUTF();
        }
    }

    @Override
    public void writeContent(DataOutputStream pStream) throws IOException{
        pStream.writeUTF(this.mSenderName);
        pStream.writeUTF(this.mCmdLabel);
        
        pStream.writeInt(this.mCmdArgs.length);
        for(int i=0;i<this.mCmdArgs.length;i++){
            pStream.writeUTF(this.mCmdArgs[i]);
        }
    }

}
