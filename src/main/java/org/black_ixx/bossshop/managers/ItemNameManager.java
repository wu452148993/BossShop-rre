package org.black_ixx.bossshop.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.commons.commentedyaml.CommentedYamlConfig;
import cc.commons.util.FileUtil;
import cc.commons.util.IOUtil;

public class ItemNameManager extends LocalLanguage<BossShop> implements IConfigModel,INeedReload{

    private final static Pattern mLangReg=Pattern.compile("minecraft/lang/([\\w_]+).(?:lang|json)");
    private static boolean LANG_JSON_FORMAT=false;
    private String mLinkVersions="https://s3.amazonaws.com/Minecraft.Download/versions/%version%/%version%.json";
    private String mLinkAssetIndex="https://launchermeta.mojang.com";
    private String mLinkLang="http://resources.download.minecraft.net/";

    private final HashMap<String,String> mItemNames=new HashMap<>();
    /** 存储上一次成功下载语言文件后asset中语言文件对应的sha1值,默认为null */
    private HashMap<String,String> mLangAssetsSHA1;
    private String mLang="en_US";

    public ItemNameManager(BossShop pPlugin){
        super(pPlugin);

        this.mPlugin.getConfigManager().registerConfigModel(this);
        this.mPlugin.registerReloadModel(this);
    }

    @Override
    protected Map<String,String> getExtraLang(){
        return this.mItemNames;
    }

    public File getLangFile(){
        return new File(this.mPlugin.getDataFolder(),"lang"+File.separator+"ItemName."+BukkitUtil.getMinecraftVersion()+"."+this.mLang+".lang");
    }

    private boolean downloadLang(CommandSender pSender){
        synchronized(this.mLang){
            String tURL=null;
            JSONObject tJson=null;
            JSONParser tParser=new JSONParser();
            File tempDir=new File(this.mPlugin.getDataFolder(),"temp/"),tTempF=null;
            if(this.mLangAssetsSHA1==null||this.mLangAssetsSHA1.isEmpty()){
                tURL=this.mLinkVersions.replace("%version%",BukkitUtil.getMinecraftVersion());
                Log.info(pSender,this.mPlugin.C("MsgDownloadVersionLinkInfo")+" "+tURL);
                tTempF=new File(tempDir,"versions.json");
                String tVersionFile=null;
                if(this.downloadFile(tURL,tTempF,null)){
                    tURL=null;
                    try{
                        Object t=tParser.parse(FileUtil.readContent(tTempF,"UTF-8"));
                        if(t instanceof JSONObject&&(t=((JSONObject)t).get("assetIndex")) instanceof JSONObject){
                            t=((JSONObject)t).get("url");
                            if(t!=null){
                                tURL=this.replaceHost(tVersionFile=t.toString(),this.mLinkAssetIndex);
                            }
                        }

                        if(tURL==null){
                            Log.severe(this.mPlugin.C("MsgCannotGetURLFromFile"));
                            return false;
                        }
                    }catch(ParseException|IOException psexp){
                        Log.severe(pSender,this.mPlugin.C("MsgErrorHappedWhenTransFormFileContent")+": "+psexp.getLocalizedMessage());
                        return false;
                    }
                }else{
                    Log.severe(pSender,this.mPlugin.C("MsgContDownFile","%file%","version.json"));
                    return false;
                }

                Log.info(pSender,this.mPlugin.C("MsgDownloadResourceLinkInfo")+" "+tURL);
                tTempF=new File(tempDir,"version_now.json");
                if(this.downloadFile(tURL,tTempF,null)){
                    try{
                        Object t=tParser.parse(FileUtil.readContent(tTempF,"UTF-8"));
                        if(t instanceof JSONObject&&(t=((JSONObject)t).get("objects")) instanceof JSONObject){
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
                                LANG_JSON_FORMAT=tResourceKey.endsWith(".json");
                                tResourceKey=langMatcher.group(langMatcher.groupCount()).toLowerCase();
                                Object sha1=tResource.get("hash");
                                if(sha1==null)
                                    continue;
                                this.mLangAssetsSHA1.put(tResourceKey,sha1.toString());
                            }
                        }
                    }catch(ParseException|IOException psexp){
                        Log.severe(pSender,this.mPlugin.C("MsgErrorHappedWhenTransFormFileContent")+": "+psexp.getLocalizedMessage());
                        return false;
                    }
                }else{
                    Log.severe(pSender,this.mPlugin.C("MsgContDownFile","%file%",tVersionFile));
                    return false;
                }
            }

            String downLoadSha1=this.mLangAssetsSHA1.get(this.mLang.toLowerCase());
            if(downLoadSha1==null){
                Log.warn(pSender,this.mPlugin.C("MsgCannotFoundThisLangDownloadLink").replace("%lang%",this.mLang));
                return false;
            }

            tTempF=new File(tempDir,"lang.lang");
            tURL=String.format("%s%s/%s",this.mLinkLang,downLoadSha1.substring(0,2),downLoadSha1);
            Log.info(pSender,this.mPlugin.C("MsgDownloadVanillaLang")+" "+tURL);
            if(this.downloadFile(tURL,tTempF,null)){
                File tLangF=this.getLangFile();
                try{
                    FileUtil.deleteFile(tLangF);
                    tTempF.renameTo(tLangF);
                    if(!tLangF.isFile()){
                        FileUtil.copyFile(tLangF,tLangF); //重命名失败时直接复制
                    }
                }catch(IOException ioexp){
                    Log.severe(pSender,this.mPlugin.C("MsgErrorHappedWhenTransFormFileContent")+": "+ioexp.getLocalizedMessage());
                    return false;
                }
            }else{
                Log.severe(pSender,this.mPlugin.C("MsgContDownFile","%file%",this.getLangFile().getName()));
                return false;
            }
        }
        return true;
    }

    private boolean downloadFile(String pURL,File pSaveLoc,String pHash){
        URL tURL=null;
        try{
            tURL=new URL(pURL);
        }catch(MalformedURLException e){
            Log.severe(this.mPlugin.C("MsgMalformedURL","%url%",pURL));
        }

        int tryTime=3;
        while(tryTime-->0){
            int tStep=1;
            try{
                URLConnection tConn=tURL.openConnection();
                tStep=2;
                tConn.setConnectTimeout(15000);
                tConn.setReadTimeout(60000);
                InputStream tIStream=tConn.getInputStream();
                tStep=3;
                FileOutputStream tFOStream=FileUtil.openOutputStream(pSaveLoc,false);
                tStep=4;
                IOUtil.copy(tIStream,tFOStream);
                tStep=5;
                IOUtil.closeStream(tIStream,tFOStream);
                return true;
            }catch(IOException e){
                if(tStep==1){
                    Log.severe(this.mPlugin.C("MsgContOpenConnection")+": "+e.getLocalizedMessage()+"("+tryTime+")");
                    continue;
                }
                if(tStep==3) return false;
                Log.severe(this.mPlugin.C("MsgErrorHappedWhenDownLoadFile")+": "+e.getLocalizedMessage()+"("+tryTime+")");
            }
        }

        return false;
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
                Bukkit.getScheduler().runTaskAsynchronously(this.mPlugin,()->{
                    if(downloadLang(pSender))
                        reloadConfig(pSender);
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
        String tContent=null;
        try{
            tContent=FileUtil.readContent(pLangFile,"UTF-8");
        }catch(IOException ioexp){
            Log.severe(pSender,this.mPlugin.C("MsgErrorHappedWhenReadItemLangFromFile").replace("%file%",pLangFile.getAbsolutePath())+": "+ioexp.getLocalizedMessage());
            return;
        }

        this.mItemNames.clear();
        if(!LANG_JSON_FORMAT){
            String[] tLines=tContent.split("[\\r]?\n");
            for(String sLine : tLines){
                String[] t=sLine.split("=",2);
                if(t.length>=2){
                    this.mItemNames.put(t[0],t[1]);
                }
            }
        }else{
            try{
                JSONObject tLangJson=(JSONObject)new JSONParser().parse(tContent);
                for(Map.Entry<Object,Object> entry : (Set<Map.Entry<Object,Object>>)tLangJson.entrySet()){
                    this.mItemNames.put(String.valueOf(entry.getKey()),String.valueOf(entry.getValue()));
                }
            }catch(ParseException e){
                Log.severe(this.mPlugin.C("MsgUnableToConvertLangFile","%lang%",this.mLang)+": "+e.getMessage());
            }
        }
        Log.info(this.mPlugin.C("MsgTotalImportMinecraftLang","%numb%",String.valueOf(this.mItemNames.size())));
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
