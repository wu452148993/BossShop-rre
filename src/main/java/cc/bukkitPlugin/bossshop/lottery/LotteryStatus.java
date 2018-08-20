package cc.bukkitPlugin.bossshop.lottery;


public enum LotteryStatus{
    /**
     * 玩家未打开背包时
     */
    UNOPENED,
    /**
     * 等待用户运行抽奖系统,当前状态为默认的,慢速循环显示物品的状态
     */
    WAIT,
    /**
     * 正在运行抽奖状态,快速循环显示物品
     */
    RUNNING,
    /**
     * 抽奖结果显示状态,只有一个物品被显示
     */
    RESULT,
    /**
     * 背包在打开后被关闭状态
     */
    CLOSE
}
