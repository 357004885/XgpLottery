package cn.xgpjun.xgplottery2.lottery.calculator.impl

import cn.xgpjun.xgplottery2.lottery.calculator.Calculator
import cn.xgpjun.xgplottery2.lottery.pojo.Award
import cn.xgpjun.xgplottery2.lottery.pojo.Lottery
import cn.xgpjun.xgplottery2.manager.DatabaseManager
import cn.xgpjun.xgplottery2.manager.Message
import cn.xgpjun.xgplottery2.send
import org.bukkit.entity.Player

class SingleGuarantee:Calculator() {
    override fun getAward(player: Player, lottery: Lottery): Award? {
        val playerData = DatabaseManager.getPlayerData(player.uniqueId)
        val nonGuaranteedCount = playerData.customData.getOrDefault("nonGuaranteed${lottery.name}",0).int() + 1
        playerData.addCount(lottery)
        val guaranteedCount = lottery.getGuaranteedCount()
        //奖池拥有保底机制
        if (guaranteedCount is Int && guaranteedCount>0 ){
            //满足保底情况
            if (nonGuaranteedCount>=guaranteedCount){
                //可抽到的物品
                val guaranteedList = lottery.awards.values.filter {
                    //保底物品 且 未得到的
                    it.isGuaranteed()&&(playerData.customData.getOrDefault("Gained-${lottery.name}-${it.name}",0).int() == 0)
                }
                //还有没抽到的东西
                if (guaranteedList.isNotEmpty()){
                    playerData.customData["nonGuaranteed${lottery.name}"] = 0
                    val award  = guaranteedList.getAward()
                    val name = award?.name
                    playerData.customData["Gained-${lottery.name}-$name"] = 1
                    return award
                }else{
                    Message.AllItemGained.get().send(player)
                }
            }else{
                playerData.customData["nonGuaranteed${lottery.name}"] = nonGuaranteedCount
            }
        }
        //其他情况、 提前出了
        //排除抽到的保底物品
        val awardList = lottery.awards.values.filter {
            playerData.customData.getOrDefault("Gained-${lottery.name}-${it.name}",0).int() == 0
        }
        val award = awardList.getAward()
        if (award?.isGuaranteed() == true){
            playerData.customData["nonGuaranteed${lottery.name}"] = 0
            playerData.customData["Gained-${lottery.name}-${award.name}"] = 1
        }
        return award
    }
}
