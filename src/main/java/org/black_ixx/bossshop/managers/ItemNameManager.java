package org.black_ixx.bossshop.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.local.LocalLanguage;
import cc.bukkitPlugin.commons.plugin.INeedReload;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.IConfigModel;
import cc.commons.commentedyaml.CommentedYamlConfig;
import cc.commons.util.FileUtil;
import cc.commons.util.IOUtil;

public class ItemNameManager extends LocalLanguage<BossShop> implements IConfigModel,INeedReload{

    private final static Pattern mLangReg=Pattern.compile("minecraft/lang/([\\w_]+).lang");
    private String mLinkVersions="https://s3.amazonaws.com/Minecraft.Download/versions/%version%/%version%.json";
    private String mLinkAssetIndex="https://launchermeta.mojang.com";
    private String mLinkLang="http://resources.download.minecraft.net/";

    private final String mMCVersion;
    private final HashMap<String,String> mItemNames=new HashMap<>();
    /** 存储上一次成功下载语言文件后asset中语言文件对应的sha1值,默认为null */
    private HashMap<String,String> mLangAssetsSHA1;
    private String mLang="en_US";

    public ItemNameManager(BossShop pPlugin){
        super(pPlugin);
        String tVersionStr=Bukkit.getVersion();
        //(MC: " + this.console.getVersion() + ")"
        Matcher matcher=Pattern.compile("^.*?\\(MC: (.*?)\\)$").matcher(tVersionStr);
        if(matcher.find())
            this.mMCVersion=matcher.group(1);
        else this.mMCVersion="1.7.10";

        this.mPlugin.getConfigManager().registerConfigModel(this);
        this.mPlugin.registerReloadModel(this);
    }

    @Override
    protected Map<String,String> getExtraLang(){
        return this.mItemNames;
    }

    public File getLangFile(){
        return new File(this.mPlugin.getDataFolder(),"lang"+File.separator+"ItemName."+this.mMCVersion+"."+this.mLang+".lang");
    }

    private void downloadLang(CommandSender pSender){
        synchronized(this.mLang){
            URL tURL=null;
            URLConnection urlConn;
            JSONObject tJson=null;
            JSONParser tParser=new JSONParser();
            InputStream is=null;
            OutputStream os=null;
            File tempDir=new File(this.mPlugin.getDataFolder(),"temp/");
            int tryTime=3;
            if(this.mLangAssetsSHA1==null||this.mLangAssetsSHA1.isEmpty()){
                while(tryTime>0){
                    tryTime--;
                    try{
                        File versionsFile=new File(tempDir,"versions.json");
                        String tUrl=this.mLinkVersions.replace("%version%",this.mMCVersion);
                        Log.info(pSender,this.mPlugin.C("MsgDownloadVersionLinkInfo")+" "+tUrl+" ("+tryTime+")");
                        urlConn=new URL(tUrl).openConnection();
                        urlConn.setConnectTimeout(15000);
                        urlConn.setReadTimeout(60000);
                        is=urlConn.getInputStream();
                        os=FileUtil.openOutputStream(versionsFile,false);
                        IOUtil.copy(is,os);
                        Object t=tParser.parse(FileUtil.readContent(versionsFile,"UTF-8"));
                        if(!(t instanceof JSONObject))
                            continue; //根节点不是JSON串,可能文件损坏
                        t=((JSONObject)t).get("assetIndex");
                        if(!(t instanceof JSONObject))
                            continue; //不存在assetIndex节点或assetIndex节点不是JSONObject,可能文件损坏
                        t=((JSONObject)t).get("url");
                        if(t==null)
                            continue; //不存在url节点
                        String url=this.replaceHost(t.toString(),this.mLinkAssetIndex);
                        tURL=new URL(url);
                        is.close();
                        os.close();
                        break;
                    }catch(IOException ioexp){
                        Log.severe(pSender,this.mPlugin.C("MsgErrorHappedWhenDownLoadFile")+": "+ioexp.getMessage(),ioexp);
                        continue;
                    }catch(ParseException psexp){
                        Log.severe(pSender,this.mPlugin.C("MsgErrorHappedWhenTransFormFileContent")+": "+psexp.getMessage(),psexp);
                        continue;
                    }finally{
                        IOUtil.closeStream(is);
                        is=null;
                        IOUtil.closeStream(os);
                        os=null;
                    }
                }
                if(tryTime<=0){
                    Log.severe(pSender,this.mPlugin.C("MsgCancelDownLoadLangAfterThreeFailDownLoadVersionLinkInfo"));
                    FileUtil.deleteFile(tempDir);
                    return;
                }
                tryTime=3;
                while(tryTime>0){
                    tryTime--;
                    try{
                        Log.info(pSender,this.mPlugin.C("MsgDownloadResourceLinkInfo")+" "+tURL+" ("+tryTime+")");
                        File versionFile=new File(tempDir,"version_now.json");
                        urlConn=tURL.openConnection();
                        urlConn.setConnectTimeout(15000);
                        urlConn.setReadTimeout(60000);
                        is=urlConn.getInputStream();
                        os=FileUtil.openOutputStream(versionFile,false);
                        IOUtil.copy(is,os);
                        Object t=tParser.parse(FileUtil.readContent(versionFile,"UTF-8"));
                        if(!(t instanceof JSONObject))
                            continue; //根节点不是JSON串,可能文件损坏
                        t=((JSONObject)t).get("objects");
                        if(!(t instanceof JSONObject))
                            continue; //一级节点不是objects或不是JSONObject,文件可能损坏
                        tJson=(JSONObject)t;
                        this.mLangAssetsSHA1=new HashMap<>();
                        for(Map.Entry<Object,Object> entry : (Set<Map.Entry<Object,Object>>)tJson.entrySet()){
                            if(!(entry.getValue() instanceof JSONObject))
                                continue;
                            String tResourceKey=String.valueOf(entry.getKey());
                            JSONObject tResource=(JSONObject)entry.getValue();
                            Matcher langMatcher=ItemNameManager.mLangReg.matcher(tResourceKey);
                            if(!langMatcher.find())
                                continue;
                            tResourceKey=langMatcher.group(langMatcher.groupCount()).toLowerCase();
                            Object sha1=tResource.get("hash");
                            if(sha1==null)
                                continue;
                            this.mLangAssetsSHA1.put(tResourceKey,sha1.toString());
                        }
                        is.close();
                        os.close();
                        break;
                    }catch(IOException ioexp){
                        Log.severe(pSender,this.mPlugin.C("MsgErrorHappedWhenDownLoadFile")+": "+ioexp.getMessage(),ioexp);
                        continue;
                    }catch(ParseException psexp){
                        Log.severe(pSender,this.mPlugin.C("MsgErrorHappedWhenTransFormFileContent")+": "+psexp.getMessage(),psexp);
                        continue;
                    }finally{
                        IOUtil.closeStream(is);
                        is=null;
                        IOUtil.closeStream(os);
                        os=null;
                    }
                }
                if(tryTime<=0){
                    Log.severe(pSender,this.mPlugin.C("MsgCancelDownLoadLangAfterThreeFailDownLoadResourceLinkInfo"));
                    FileUtil.deleteFile(tempDir);
                    return;
                }
            }
            String downLoadSha1=this.mLangAssetsSHA1.get(this.mLang.toLowerCase());
            if(downLoadSha1==null){
                Log.warn(pSender,this.mPlugin.C("MsgCannotFoundThisLangDownloadLink").replace("%lang%",this.mLang));
                return;
            }
            tryTime=3;
            File tLangFile=this.getLangFile();
            while(tryTime>0){
                tryTime--;
                try{
                    String tUrlStr=String.format("%s%s/%s",this.mLinkLang,downLoadSha1.substring(0,2),downLoadSha1);
                    Log.info(pSender,this.mPlugin.C("MsgDownloadVanillaLang")+" "+tUrlStr+" ("+tryTime+")");
                    File versionFile=new File(tempDir,"lang.lang");
                    urlConn=new URL(tUrlStr).openConnection();
                    urlConn.setConnectTimeout(15000);
                    urlConn.setReadTimeout(60000);
                    is=urlConn.getInputStream();
                    os=FileUtil.openOutputStream(versionFile,false);
                    IOUtil.copy(is,os);
                    is.close();
                    os.close(); //必须先关闭,否则无法重命名
                    FileUtil.deleteFile(tLangFile);
                    versionFile.renameTo(tLangFile);
                    if(tLangFile.isFile())
                        this.reloadConfig(pSender);
                    else FileUtil.copyFile(versionFile,tLangFile); //重命名失败时直接复制
                    break;
                }catch(IOException ioexp){
                    Log.severe(pSender,this.mPlugin.C("MsgErrorHappedWhenDownLoadFile")+": "+ioexp.getMessage(),ioexp);
                    continue;
                }finally{
                    IOUtil.closeStream(is);
                    is=null;
                    IOUtil.closeStream(os);
                    os=null;
                }
            }
            if(!Log.isDebug())
                FileUtil.deleteFile(tempDir);
            if(tryTime<=0){
                Log.severe(pSender,this.mPlugin.C("MsgCancelDownLoadLangAfterThreeFailDownLoadVanillaLangFile"));
                return;
            }
        }
    }

    private String replaceHost(String pURL,String pNewHost) throws MalformedURLException{
        return pNewHost+new URL(pURL).getFile();
    }

    private boolean isValidURL(String pURL){
        try{
            new URL(pURL);
        }catch(MalformedURLException e){
            return false;
        }
        return true;
    }

    @Override
    public boolean reloadConfig(final CommandSender pSender){
        File tLangFile=this.getLangFile();
        if(!LocalLanguage.en_US.equalsIgnoreCase(this.mLang)){
            if(!tLangFile.isFile()){
                Bukkit.getScheduler().runTaskAsynchronously(this.mPlugin,new Runnable(){

                    @Override
                    public void run(){
                        ItemNameManager.this.downloadLang(pSender);
                    }
                });
                return true;
            }else this.importLang(pSender,tLangFile);
        }
        this.setLang(pSender,this.mLang);
        Log.info(pSender,this.mPlugin.C("MsgVanillaItemNameReloaded")+"["+this.mLang+"]");
        return true;
    }

    /**
     * 根据{@link ItemNameManager#mLang}来将导入指定文件翻译
     * <p>
     * 在调用前请确保文件存在
     * </p>
     */
    private void importLang(CommandSender pSender,File pLangFile){
        String[] contentLines=new String[0];
        FileInputStream fis=null;
        try{
            fis=new FileInputStream(pLangFile);
            byte[] contents=new byte[fis.available()];
            fis.read(contents);
            contentLines=new String(contents,"UTF-8").split("[\\r]?\n");
        }catch(IOException ioexp){
            Log.severe(pSender,this.mPlugin.C("MsgErrorHappedWhenReadItemLangFromFile").replace("%file%",pLangFile.getAbsolutePath())+": "+ioexp.getMessage(),ioexp);
            return;
        }finally{
            IOUtil.closeStream(fis);
        }
        this.mItemNames.clear();
        for(String sLine : contentLines){
            String[] t=sLine.split("=",2);
            if(t.length>=2){
                this.mItemNames.put(t[0],t[1]);
            }
        }
    }

    @Override
    public void setConfig(CommandSender pSender,CommentedYamlConfig pConfig){
        CommentedYamlConfig tConfig=this.mPlugin.getConfigManager().getConfig();
        this.mLang=tConfig.getString("ItemNameLang",this.mLang).trim();
        if(this.mLang.isEmpty())
            this.mLang="zh_CN";
        Log.info(pSender,this.mPlugin.C("MsgSetItemNameLangTo")+" "+this.mLang);
        String tString=tConfig.getString("DownloadLink.Versions",this.mLinkVersions);
        if(!this.isValidURL(tString))
            Log.severe(pSender,this.mPlugin.C("MsgNotValidURL").replace("%url%",tString));
        else this.mLinkVersions=tString;
        tString=tConfig.getString("DownloadLink.AssetIndex",this.mLinkAssetIndex);
        if(!this.isValidURL(tString))
            Log.severe(pSender,this.mPlugin.C("MsgNotValidURL").replace("%url%",tString));
        else this.mLinkAssetIndex=tString;
        tString=tConfig.getString("DownloadLink.Lang",this.mLinkLang);
        if(!this.isValidURL(tString))
            Log.severe(pSender,this.mPlugin.C("MsgNotValidURL").replace("%url%",tString));
        else this.mLinkLang=tString;

    }

    @Override
    public void addDefaults(CommentedYamlConfig pConfig){

    }
}
