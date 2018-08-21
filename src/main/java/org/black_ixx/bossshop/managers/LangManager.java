package org.black_ixx.bossshop.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.command.CommandSender;

import cc.bukkitPlugin.commons.Log;
import cc.bukkitPlugin.commons.plugin.manager.fileManager.TLangManager;
import cc.commons.commentedyaml.CommentedSection;

public class LangManager extends TLangManager<BossShop>{

    /**
     * 语言翻译系统
     * 
     * @param plugin
     */
    public LangManager(BossShop pplugin){
        super(pplugin,"messages.yml","1.3");
    }

    @Override
    public boolean reloadConfig(CommandSender pSender){
        if(!super.reloadConfig(pSender)){
            Log.severe(pSender,C("MsgErrorHappendWhenReloadLang"));
            return false;
        }
        this.checkUpdate();
        this.addDefaults();
        boolean result=this.saveConfig(null);
        Log.info(pSender,C("MsgLangReloaded"));
        return result;
    }

    public boolean checkUpdate(){
        boolean update=super.checkUpdate();
        if(!update)
            return false;
        String tVersion=this.mConfig.getString(SEC_CFG_VERSION,"1.0");
        if(tVersion.equalsIgnoreCase("1.0")){// 1.0-->1.1
            tVersion="1.1";
            this.mConfig.set("Mail.MailMaxSaveNumb","====邮件最大保存数量[%numb%]====");
            String tMsg=this.mConfig.getString("Mail.YouReciveMailFrom");
            if(tMsg!=null){
                tMsg=tMsg.replace("你收到了来自","你收到了");
                this.mConfig.set("Mail.YouReciveMailFrom",null);
                this.mConfig.set("Mail.YouReciveMail",tMsg);
            }
        }
        if(tVersion.equalsIgnoreCase("1.1")){// 1.1-->1.2
            tVersion="1.2";
            this.mConfig.set("MsgYouSaleHasSellOne",null);
        }
        if(tVersion.equalsIgnoreCase("1.2")){// 1.2-->1.3
            tVersion="1.3";
            this.mConfig.set("MsgYouEconomyHaveNoAccount",this.mConfig.removeCommentedValue("Economy.NoAccount"));
            this.mConfig.set("MsgEnchantmentInvalid",this.mConfig.removeCommentedValue("Enchantment.Invalid"));
            this.mConfig.set("MsgItemNameReloaded",null);
            CommentedSection tNotEnoughSection=this.mConfig.getSection("NotEnough");
            if(tNotEnoughSection!=null)
                for(String sKey : tNotEnoughSection.getKeys(false))
                    this.mConfig.set("Msg"+sKey+"NotEnough",tNotEnoughSection.get(sKey));
            this.mConfig.removeCommentedValue("NotEnough");
            TreeMap<String,String> keyConvertTree=new TreeMap<>();
            ArrayList<String> emptyNodeKey=new ArrayList<>();
            List<String> replaceLabel=Arrays.asList(new String[]{"Cmd","Mail","Main","CFG","Console","Sale"});
            for(String sLangKey : this.mConfig.getKeys(true)){
                String targetKey=sLangKey;
                if(sLangKey.indexOf('.')!=-1){
                    String[] msgParts=sLangKey.split("\\.",2);
                    if(replaceLabel.indexOf(msgParts[0])!=-1)
                        targetKey="Msg"+msgParts[1];
                    else targetKey=msgParts[0]+msgParts[1];
                }else{
                    if(!sLangKey.equals(LangManager.SEC_CFG_VERSION)){
                        emptyNodeKey.add(sLangKey);
                        continue;
                    }
                }
                keyConvertTree.put(targetKey,sLangKey);
            }
            for(Map.Entry<String,String> sLangNewNode : keyConvertTree.entrySet())
                this.mConfig.set(sLangNewNode.getKey(),this.mConfig.removeCommentedValue(sLangNewNode.getValue()));
            for(String sEmptyKey : emptyNodeKey)
                this.mConfig.set(sEmptyKey,null);
        }
        this.mConfig.set(SEC_CFG_VERSION,this.mVersion);
        return true;
    }

    @Override
    public void addDefaults(){
        super.addDefaults();
        //this.mConfig.addDefault("HelpClear","清理插件中一些模块的状态");
        this.mConfig.addDefault("HelpClearModelStatus","清理插件中一些模块的状态");
        this.mConfig.addDefault("HelpClosePlayerShop","关闭玩家打开的BossShop商店");
        this.mConfig.addDefault("HelpHelpOpen","获取有关/BossShop open的帮助指令");
        this.mConfig.addDefault("HelpMailCheck","检查你的邮件列表");
        this.mConfig.addDefault("HelpMailHelp","获取邮件命令的帮助");
        //this.mConfig.addDefault("HelpMailRevice","接受邮件中的物品");
        this.mConfig.addDefault("HelpMailRecive","接受邮件中的物品");        
        this.mConfig.addDefault("HelpMailSend","发送物品到玩家的邮箱");
        this.mConfig.addDefault("HelpNBTAdd","添加当前手上物品的NBT信息到NBT库存");
        this.mConfig.addDefault("HelpNBTClear","清理由插件自动生成且引用次数为0的NBT库存");
        this.mConfig.addDefault("HelpNBTHelp","获取有关/shop nbt 的帮助指令");
        this.mConfig.addDefault("HelpOpenOtherShop","为别人打开商店");
        this.mConfig.addDefault("HelpOpenShop","为自己打开商店");
        this.mConfig.addDefault("HelpReload","重载BossShop配置");
        this.mConfig.addDefault("HelpReloadConfig","重载插件主配置");
        this.mConfig.addDefault("HelpReloadHelp","获取有关/shop reload的帮助指令");
        this.mConfig.addDefault("HelpReloadItemName","重载物品名字翻译");
        this.mConfig.addDefault("HelpReloadLang","重载语言文件,部分语言可能无法更新");
        this.mConfig.addDefault("HelpReloadShop","重载指定商店配置,商店名需要文件名");
        this.mConfig.addDefault("HelpReloadGui","重载Gui配置");
        this.mConfig.addDefault("HelpReloadMail","重载邮件配置配置");
        this.mConfig.addDefault("HelpSaleForsale","寄售物品");
        this.mConfig.addDefault("HelpSaleHelp","获取有关/BossShop sale 的帮助命令");
        this.mConfig.addDefault("HelpSaleOther","其他参数均为整数");
        this.mConfig.addDefault("HelpSalePriceType","支持的价格类型 money,points");
        this.mConfig.addDefault("HelpSaleSellType","支持的售卖类型 item,money,points");
        this.mConfig.addDefault("HelpStartSaleWithChat","使用聊天栏开始图形化寄售");
        this.mConfig.addDefault("HelpStopSaleWithChat","退出聊天栏寄售");
        this.mConfig.addDefault("HelpUnsale","下架指定编号的寄售物品");
        this.mConfig.addDefault("LoreAlreadySoldOut","&c商品已经售罄");
        this.mConfig.addDefault("LoreBuyIsNotTime","§c商品未到购买时间,将于%time%开放购买");
        this.mConfig.addDefault("LoreEveryoneLimit","§2每人限购%numb%件,你还可以购买%left%件");
        this.mConfig.addDefault("LoreGoodsOutOfDate","&c商品已经过了购买日期");
        this.mConfig.addDefault("LoreGoodsStockLeft","库存剩余%numb%件!");
        this.mConfig.addDefault("LoreNowCanBuyGoods","现在可以购买该商品");
        this.mConfig.addDefault("LoreRightClickBuyNumb","&c右键购买%numb%件");
        this.mConfig.addDefault("LoreShiftLeftClickBuyNumb","&cShift+左键购买最多件");
        this.mConfig.addDefault("LoreWillCloseInTime","&c将于%time%结束开放购买");
        this.mConfig.addDefault("MsgAllNumbKeyFunctionIs","所有可用的数字键盘功能: ");
        this.mConfig.addDefault("MsgAlreadyBought","&c这东西你已经买过了");
        this.mConfig.addDefault("MsgAlreadyClosePlayerShop","已经关闭玩家商店");
        this.mConfig.addDefault("MsgAlreadyReloadShop","已经重载%shop%商店");
        this.mConfig.addDefault("MsgAlreadySoldOut","&c商品已经售罄");
        this.mConfig.addDefault("MsgAutoDetectPointPlugin","自动选择%plugin%点券插件");
        this.mConfig.addDefault("MsgBindItemToOpenMainShopNotExist","&c绑定打开BossShop主商店的的物品%item%不存在,设置为默认值的347(钟表)");
        this.mConfig.addDefault("MsgCannotBuyYourselfGoods","&c你不能购买自己的寄售物品");
        this.mConfig.addDefault("MsgCannotCreatMailItem","&c无法创建邮件物品");
        this.mConfig.addDefault("MsgCannotGetURLFromFile","&c无法从version.json中获取下载链接");
        this.mConfig.addDefault("MsgCannotFoundThisLangDownloadLink","&c未找到%lang%的原版物品翻译语言");
        this.mConfig.addDefault("MsgContOpenConnection","&c无法打开下载链接");
        this.mConfig.addDefault("MsgContDownFile","&c无法下载文件%file%");
        this.mConfig.addDefault("MsgChatSaleExit","你已经退出聊天栏寄售");
        this.mConfig.addDefault("MsgChatSaleFinish","你已经设置所有寄售参数");
        this.mConfig.addDefault("MsgChatSaleRepartStart","&c你已经开始寄售了,请勿重复开始");
        this.mConfig.addDefault("MsgChatSaleStart","你已经进入寄售物品状态,输入/BS sale stop 来退出");
        this.mConfig.addDefault("MsgClearedModelStatus","&c清理了%numb%个模块的状态");
        this.mConfig.addDefault("MsgClearedNoUsefulNBT","共清理了%numb%个未被引用且自动生成的NBT库存");
        this.mConfig.addDefault("MsgClearedTransactionLog","清理了%day%天的购买记录");
        this.mConfig.addDefault("MsgClickSaleButtonFor","点击寄售按钮来进行");
        this.mConfig.addDefault("MsgCloseShop","&6下次再来??");
        this.mConfig.addDefault("MsgConfigReloacded","已经重载配置");
        this.mConfig.addDefault("MsgConsoleNotAllow","&c控制台不能执行该命令");
        this.mConfig.addDefault("MsgDownloadResourceLinkInfo","下载资源文件链接信息");
        this.mConfig.addDefault("MsgDownloadVersionLinkInfo","下载版本链接信息");
        this.mConfig.addDefault("MsgDownloadVanillaLang","下载原版翻译文件信息");
        this.mConfig.addDefault("MsgEnchantmentInvalid","&c这个物品不能附这个魔");
        this.mConfig.addDefault("MsgErrorCreateSaleFile","&c创建寄售商店文件是发生了错误");
        this.mConfig.addDefault("MsgErrorHappedWhenHandlerCmd","&c插件在处理命令的时候发生了异常");
        this.mConfig.addDefault("MsgErrorHappedWhenHandlerPoints","&c在处理点券时发生了异常,请通知管理员");
        this.mConfig.addDefault("MsgErrorHappedWhenReadItemLangFromFile","&c从文件%file%读入物品翻译时发生了错误");
        this.mConfig.addDefault("MsgErrorHappedWhenDownLoadFile","&c下载文件时发生了异常");
        this.mConfig.addDefault("MsgErrorHappedWhenTransFormFileContent","&c转换文件内容格式时发生了异常");
        this.mConfig.addDefault("MsgErrorHappend","&c商店发生了一点小错误,请通知管理员");
        this.mConfig.addDefault("MsgErrorNumbKeyConfigFunctionNotFound","&c数字键盘%key%的功能配置错误,名字为%function%的功能不存在");
        this.mConfig.addDefault("MsgExpNotEnough","&c你的经验太少了");
        this.mConfig.addDefault("MsgFailCreateGoods","&c创建%goods%失败");
        this.mConfig.addDefault("MsgNotFoundAnySupportPointsPlugin","&c未发现任何受支持的点券插件");
        this.mConfig.addDefault("MsgNotFoundPreferencePointsPluginButSupportOneFound","未找到配置文件中指定的%plugin_set%点券插件,但是发现了支持的点券插件%plugin_found%,将此插件作为默认");
        this.mConfig.addDefault("MsgForSaleSuccessAndTisID","&c寄售物品成功,寄售物品的编号是: ");
        this.mConfig.addDefault("MsgFromHandUnsale","来自手动下架");
        this.mConfig.addDefault("MsgFromOutDateSale","来自过期寄售");
        this.mConfig.addDefault("MsgFromPlayer","来自玩家");
        this.mConfig.addDefault("MsgFromUnknow","来自未知");
        this.mConfig.addDefault("MsgGetHelp","&c获取帮助");
        this.mConfig.addDefault("MsgGoodsNotArriveTime","&c商品未到购买时间");
        this.mConfig.addDefault("MsgGoodsOutOfDate","&c商品已经过了购买时间");
        this.mConfig.addDefault("MsgIntroducePartNumb","请输入你要寄售物品的份数: ");
        this.mConfig.addDefault("MsgIntroducePrice","请输入每份商品的价格: ");
        this.mConfig.addDefault("MsgIntroducePriceType","点击选择购买商品的支付方式: ");
        this.mConfig.addDefault("MsgIntroduceSellType","点击选择你要寄售的商品类型: ");
        this.mConfig.addDefault("MsgIntroduceSingleNumb","请输入你要寄售物品每份的数量: ");
        this.mConfig.addDefault("MsgIntroduceSingleNumb","请输入你要寄售物品每份的数量: ");
        this.mConfig.addDefault("MsgItemCannotPileMoreThan","&c此物品的堆叠数量不能超过");
        this.mConfig.addDefault("MsgItemMayDonotHaveNBT","&c物品可能没有nbt信息");
        this.mConfig.addDefault("MsgItemNotEnough","&c兑换需要的物品不足");
        this.mConfig.addDefault("MsgLangReloacded","已经重载语言文件");
        this.mConfig.addDefault("MsgLotteryConfigReloacded","已经重载抽奖配置");
        this.mConfig.addDefault("MsgMailAlreadyReload","已经重载邮件");
        this.mConfig.addDefault("MsgMailHasSend","邮件已经发送");
        this.mConfig.addDefault("MsgMailMaxSaveNumb","====邮件最大保存数量[%numb%]====");
        this.mConfig.addDefault("MsgMailNotExist","&c邮件%mail%不存在");
        this.mConfig.addDefault("MsgMalformedURL","&c链接格式不正确: %url%");
        this.mConfig.addDefault("MsgMenuItemConfigNotFound","未找到菜单物品");
        this.mConfig.addDefault("MsgMissingNBTNode","&cNBT库丢失NBT节点");
        this.mConfig.addDefault("MsgMoneyNotEnough","&c你钱不够了");
        this.mConfig.addDefault("MsgMustAboveZero","&c必须大于零");
        this.mConfig.addDefault("MsgMustBeNumb","&c必须为数字");
        this.mConfig.addDefault("MsgNBTHasExistInStockAndItsID","NBT已经存在在NBT库存中,它的ID是: ");
        this.mConfig.addDefault("MsgNBTHaveAddAndItsID","NBT已经添加到NBT库存,它的ID是: ");
        this.mConfig.addDefault("MsgNoPermission","&c你没有权限");
        this.mConfig.addDefault("MsgNoPermitToBuyThisItem","&c你没有权限购买该物品");
        this.mConfig.addDefault("MsgNoPermitToCreateSignShop","&c没有权限创建牌子商店");
        this.mConfig.addDefault("MsgNoPermitToOpenSignShop","&c没有权限打开牌子商店");
        this.mConfig.addDefault("MsgNoPlayerFound","&c未发现该玩家");
        this.mConfig.addDefault("MsgNoPointConfigedPluginFound","&c未发现配置文件中指定的%plugin%点券插件");
        this.mConfig.addDefault("MsgNoSaleItemFound","&c未找到该寄售物品");
        this.mConfig.addDefault("MsgNoShopFound","&c未发现该商店");
        this.mConfig.addDefault("MsgNoGoodsFound","&c未发现该商品");
        this.mConfig.addDefault("MsgNotValidURL","&c%url%不是有效的URL");
        this.mConfig.addDefault("MsgNowSaleParameter","当前寄售参数(§6可以重复使用)");
        this.mConfig.addDefault("MsgNowSaleParameterColor","&a");
        this.mConfig.addDefault("MsgOpenShop","&6开启小店");
        this.mConfig.addDefault("MsgOpenShopForPlayer","为玩家%player%开启商店%shop%");
        this.mConfig.addDefault("MsgOutOfPersonalLimit","&c已经达到个人购买物品数量上限");
        this.mConfig.addDefault("MsgPlayerMailIsFull","&c玩家[%player%]的邮箱已经满了");
        this.mConfig.addDefault("MsgPlayerNotOpenBSShop","&c玩家没有打开BossShop商店");
        this.mConfig.addDefault("MsgPlayerLotteryGetInShopWithItem","&c玩家%player%在%shop%抽中了%item%");
        this.mConfig.addDefault("MsgPlayerOffline","&c玩家未在线");
        this.mConfig.addDefault("MsgPlayerSaleItem","玩家%player_name%寄售了%item%");
        this.mConfig.addDefault("MsgPleaseInputPlayerName","&c请输入玩家名");
        this.mConfig.addDefault("MsgPluginReloaded","已经重载插件");
        this.mConfig.addDefault("MsgPointsNotEnough","&c你点券不足");
        this.mConfig.addDefault("MsgNoShopFound","&c商店%shop%不存在");
        this.mConfig.addDefault("MsgSaleItemBlackList","&c寄售黑名单物品");
        this.mConfig.addDefault("MsgSaleItemBlackLoreList","&c寄售黑名单Lore");
        this.mConfig.addDefault("MsgSaleItemShouldNotSameWithPrice","&c寄售物品类型和价格类型不能相同");
        this.mConfig.addDefault("MsgSaleReachPreOneLimit","&c寄售商品已达个人寄售上限");
        this.mConfig.addDefault("MsgSaleShopIsFull","&c寄售商店寄售数量已经达到上限");
        this.mConfig.addDefault("MsgShopLoaded","载入了%numb%个商店");
        this.mConfig.addDefault("MsgShopNameTooLongOver32","&c商店名字的长度超过了32个字符");
        this.mConfig.addDefault("MsgServerDisablePoints","&c服务器不支持点券或未启用点券");
        this.mConfig.addDefault("MsgSetMainMenu","设置主商店为%name%");
        this.mConfig.addDefault("MsgSetItemNameLangTo","设置物品翻译语言为");
        this.mConfig.addDefault("MsgSetPreferencePointsPlugin","自定义点券插件为%plugin%");
        this.mConfig.addDefault("MsgShopFileNotExist","&c商店配置文件不存在");
        this.mConfig.addDefault("MsgRemoveShopDueToFileNotExist","商店%shop%由于配置文件不存在,移除");
        this.mConfig.addDefault("MsgStartReloadShop","开始重载商店");
        this.mConfig.addDefault("MsgSuccessUnsaleItem","成功下架寄售物品");
        this.mConfig.addDefault("MsgTotalImportMinecraftLang","成功导入%numb%条原版翻译");
        this.mConfig.addDefault("MsgUnableFoundSaleOwner","&c无法找到寄售商品的所有者");
        this.mConfig.addDefault("MsgUnableToConvertLangFile","&c无法转换语言%lang%的json文件");
        this.mConfig.addDefault("MsgUnknowCommand","&c未知指令,输入/BossShop help 获取帮助");
        this.mConfig.addDefault("MsgUnknowOutDateSaleItemType","&c未知的过期寄售物品类型");
        this.mConfig.addDefault("MsgUnknowPriceType","&c未知的价格类型");
        this.mConfig.addDefault("MsgUnknowSaleItemType","&c未知的寄售物品类型");
        this.mConfig.addDefault("MsgUnknowSalePriceType","&c未知的寄售价格类型,无法给寄售物品所有者所得");
        this.mConfig.addDefault("MsgUnsupportMailItemType","&c不支持的邮件物品类型");
        this.mConfig.addDefault("MsgUnsupportPriceType","&c不支持的价格类型");
        this.mConfig.addDefault("MsgUnsupportSaleItemParam","&c不支持的寄售物品变量设置");
        this.mConfig.addDefault("MsgUnsupportSaleItemType","&c不支持的寄售物品类型");
        this.mConfig.addDefault("MsgUseItemToOpenMainShop","使用%item%作为打开BossShop主商店的的物品");
        this.mConfig.addDefault("MsgVanillaItemNameReloaded","已经重载原版物品名字翻译");
        this.mConfig.addDefault("MsgYouCannotSaleAnyThing","&c你不能寄售任何东西");
        this.mConfig.addDefault("MsgYouCannotSaleThisItem","&c你不能寄售这个物品");
        this.mConfig.addDefault("MsgYouCannotSaleThisType","&c你不能寄售%type%");
        this.mConfig.addDefault("MsgYouCanSaleNoprice","&c你无法使用任何价格来寄售物品");
        this.mConfig.addDefault("MsgYouCannotSaleThisPriceType","&c你无法使用价格类型 %type% 来为物品设置价格");
        this.mConfig.addDefault("MsgYouCannotSendMailToYouSelf","&c你不能给自己发邮件");
        this.mConfig.addDefault("MsgYouCannotUnsaleOther","&c你不能下架别人的寄售的物品");
        this.mConfig.addDefault("MsgYouClickTooQuick","&c你点击的太快了");
        this.mConfig.addDefault("MsgYouEconomyHaveNoAccount","&c你还没有开户");
        this.mConfig.addDefault("MsgYouHaveMail","你有%numb%封邮件");
        this.mConfig.addDefault("MsgYouHaveNoMail","你没有任何邮件");
        this.mConfig.addDefault("MsgYouHaveNotOutDateSaleItem","&c你没有过期的寄售物品");
        this.mConfig.addDefault("MsgYouTotalGoodsNumbIsTooBig","&c你输入的物品总数过大");
        this.mConfig.addDefault("MsgYouMailIsFull","&c你的邮箱已经满了");
        this.mConfig.addDefault("MsgYouNeed%%MoneyToSendMail","&c你需要%numb%的金币来发送邮件");
        this.mConfig.addDefault("MsgYouNeed%%MoneyToUnsale","&c你需要%numb%的金币来下架物品");
        this.mConfig.addDefault("MsgYouReciveMail","你收到了%where%的邮件");
        this.mConfig.addDefault("MsgYouSaleHasSell","你的寄售物品售出%numb%份");
        this.mConfig.addDefault("MsgYouSetSaleParamTo","&a你设置了%param%为%value%");
        this.mConfig.addDefault("MsgYouShouldTakeItemInHand","&c你的手上必须拿着物品");
        this.mConfig.addDefault("MsgYouTodayCannotUnsaleMore","&c你今天已经不能再下架更多物品了");
        this.mConfig.addDefault("MsgunknowChildCommand","&c未知子命令指令");
        this.mConfig.addDefault("WordAll","所有");
        this.mConfig.addDefault("WordCommand","命令");
        this.mConfig.addDefault("WordConsoleCommand","控制台命令");
        this.mConfig.addDefault("WordCount","个");
        this.mConfig.addDefault("WordEnchantment","附魔");
        this.mConfig.addDefault("WordExp","经验");
        this.mConfig.addDefault("WordForSale","寄售");
        this.mConfig.addDefault("WordFree","免费");
        this.mConfig.addDefault("WordGoTo","前往");
        this.mConfig.addDefault("WordInput","输入");
        this.mConfig.addDefault("WordItem","物品");
        this.mConfig.addDefault("WordLottery","抽奖");
        this.mConfig.addDefault("WordMoney","金币");
        this.mConfig.addDefault("WordNothing","没有");
        this.mConfig.addDefault("WordPartNumb","份数");
        this.mConfig.addDefault("WordPermssion","权限");
        this.mConfig.addDefault("WordPlayerCommand","玩家命令");
        this.mConfig.addDefault("WordPlayerName","玩家名");
        this.mConfig.addDefault("WordPoints","点券");
        this.mConfig.addDefault("WordPrice","价格");
        this.mConfig.addDefault("WordPriceType","价格类型");
        this.mConfig.addDefault("WordRevive","接收");
        this.mConfig.addDefault("WordSaleID","物品编号");
        this.mConfig.addDefault("WordSaleShop","寄售商店");
        this.mConfig.addDefault("WordSellType","寄售类型");
        this.mConfig.addDefault("WordShop","商店");
        this.mConfig.addDefault("WordShopName","商店名称");
        this.mConfig.addDefault("WordSigleNumb","每份数量");
        this.mConfig.addDefault("WordTimeCommand","时限命令");
        this.mConfig.addDefault("WordView","查看");
        this.mConfig.addDefault("WordYouBought","你购买了");
        this.mConfig.addDefault("YmalNodeBuyItemName","购买的商品名字");
        this.mConfig.addDefault("YmalNodeBuyNumb","购买数量");
        this.mConfig.addDefault("YmalNodeBuyShop","购买的商店");
        this.mConfig.addDefault("YmalNodeBuyTime","购买时间");
        this.mConfig.addDefault("YmalNodePlayerName","玩家名");
    }

}
