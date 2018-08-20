package cc.bukkitPlugin.bossshop.mulServer.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.inventory.ItemStack;

import cc.bukkitPlugin.bossshop.mulServer.ServerLink;

public abstract class APacket{

    protected BossShop mPlugin;

    public APacket(BossShop pPlugin){
        this.mPlugin=pPlugin;
    }

    public String getName(){
        return this.getClass().getSimpleName();
    }

    /**
     * 用于在接收端处理包
     */
    public abstract void handle(ServerLink pLink);

    /**
     * 用于在接收端构造空包之后,写入包数据
     * @param 读取的数据流
     * @return 数据是否读入成功
     */
    public abstract void readContent(DataInputStream pStream) throws IOException;

    /**
     * 用于在发送端序列化数据包
     * @param pStream 写入的数据流
     */
    public abstract void writeContent(DataOutputStream pStream) throws IOException;

    public void writeItem(DataOutputStream pStream,ItemStack pItem) throws IOException{

    }

    public ItemStack readItem(DataInputStream pStream) throws IOException{
        return null;
    }

}
