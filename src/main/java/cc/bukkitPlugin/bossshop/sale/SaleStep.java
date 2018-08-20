package cc.bukkitPlugin.bossshop.sale;

public enum SaleStep{
    // /BossShop sale <售卖类型> <单次数量> <份数> <价格类型> <价格>
    GoodsType(1),
    SingleNumb(2),
    PartNumb(3),
    PriceType(4),
    Price(5),
    Finish(6);
    
    private final int mStepNumb;
    
    private SaleStep(int pStepNumb){
        this.mStepNumb=pStepNumb;
    }
    
    /**
     * 获取基于当前步奏的下一步奏
     * @param pStep
     * @return
     */
    public static SaleStep getNext(SaleStep pStep){
        if(pStep==null) throw new IllegalArgumentException("获取下一步寄售步奏时,当前步奏不能为null");
        if(pStep==SaleStep.Finish) return null;
        int next=pStep.mStepNumb+1;
        for(SaleStep sStep : SaleStep.values()){
            if(sStep.mStepNumb==next) return sStep;
        }
        return null;
    }
    
}
