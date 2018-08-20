package org.black_ixx.bossshop.core;

import cc.commons.util.StringUtil;

public class BSEnums{

    public enum RewardType{
        Item("WordItem"),
        Enchantment("WordEnchantment"),
        OpCommand("WordCommand","command"),
        ConsoleCommand("WordConsoleCommand"),
        TimeCommand("WordTimeCommand"),
        Permission("WordPermssion"),
        Money("WordMoney"),
        Points("WordPoints"),
        Shop("WordShop"),
        PlayerCommand("WordPlayerCommand"),
        Lottery("WordLottery"),
        Nothing("WordNothing");

        private String mNameKey;
        private String mNickName="";

        private RewardType(String pNameKey){
            this(pNameKey,"");
        }

        private RewardType(String pNameKey,String pNickName){
            this.mNameKey=pNameKey;
            this.mNickName=pNickName==null?"":pNickName;
        }

        public String getNameKey(){
            return this.mNameKey;
        }

    }

    public enum PriceType{

        Money("WordMoney"),
        Item("WordItem"),
        Points("WordPoints"),
        Exp("WordExp"),
        Nothing("WordNothing"),
        Free("WordFree");

        private String mNameKey;
        private String mNickName="";

        private PriceType(String pNameKey){
            this(pNameKey,"");
        }

        private PriceType(String pNameKey,String pNickName){
            this.mNameKey=pNameKey;
            this.mNickName=pNickName==null?"":pNameKey;
        }

        public String getNameKey(){
            return this.mNameKey;
        }

    }

    public static RewardType getRewardType(String pName){
        if(StringUtil.isEmpty(pName))
            return null;
        for(RewardType t : BSEnums.RewardType.values()){
            if(pName.equalsIgnoreCase(t.name())||(!t.mNickName.isEmpty()&&t.mNickName.equalsIgnoreCase(pName)))
                return t;
        }
        return null;
    }

    public static PriceType getPriceType(String pName){
        if(StringUtil.isEmpty(pName))
            return null;
        for(PriceType t : BSEnums.PriceType.values()){
            if(pName.equalsIgnoreCase(t.name())||(!t.mNickName.isEmpty()&&t.mNickName.equalsIgnoreCase(pName)))
                return t;
        }
        return null;
    }

}
