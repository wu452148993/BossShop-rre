package cc.bukkitPlugin.bossshop.mulServer.packet.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.bossshop.mulServer.ServerLink;
import cc.bukkitPlugin.bossshop.mulServer.packet.APacket;
import cc.bukkitPlugin.commons.Log;


public class SPSendMessae extends APacket{

    private String mPlayerName;
    private String mMessages;
    
    public SPSendMessae(BossShop pPlugin){
        super(pPlugin);
    }
    
    public SPSendMessae(BossShop pPlugin,String pPlayerName,String pMessages){
        this(pPlugin);
        this.mPlayerName=pPlayerName;
        this.mMessages=pMessages;
    }

    @Override
    public void handle(ServerLink pLink){
        Player tPlayer=Bukkit.getPlayer(this.mPlayerName);
        if(tPlayer!=null){
            Log.send(tPlayer,this.mMessages);
        }else if(this.mPlayerName.equalsIgnoreCase("console")){
            Log.info(this.mMessages);
        }
    }

    @Override
    public void readContent(DataInputStream pStream) throws IOException{
        this.mPlayerName=pStream.readUTF();
        this.mMessages=pStream.readUTF();
    }

    @Override
    public void writeContent(DataOutputStream pStream) throws IOException{
        pStream.writeUTF(this.mPlayerName);
        pStream.writeUTF(this.mMessages);
    }

}
