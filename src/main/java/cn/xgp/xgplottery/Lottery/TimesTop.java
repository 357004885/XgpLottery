package cn.xgp.xgplottery.Lottery;

import cn.xgp.xgplottery.XgpLottery;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TimesTop {
    public List<String> top = new ArrayList<>();
    public List<LotteryTimes> times = new CopyOnWriteArrayList<>();
    public TimesTop(boolean isTotal,String lotteryName){
        List<LotteryTimes> allTimes = isTotal? XgpLottery.totalTime:XgpLottery.currentTime;
        if(XgpLottery.lotteryList.containsKey(lotteryName)){
            for(LotteryTimes lotteryTimes :allTimes){
                if(lotteryTimes.getLotteryName().equals(lotteryName)){
                    this.times.add(lotteryTimes);
                    createTop();
                }
            }
        }
    }

    private void createTop(){
        if(times.isEmpty())
            return;
        times.sort(new LotteryTimesComparator());
        for(LotteryTimes lotteryTimes:times){
            top.add(ChatColor.GOLD+"玩家 "+Bukkit.getOfflinePlayer(lotteryTimes.getUuid()).getName()+" : "+ChatColor.AQUA+lotteryTimes.getTimes());
        }
    }

    static class LotteryTimesComparator implements Comparator<LotteryTimes> {
        @Override
        public int compare(LotteryTimes l1, LotteryTimes l2) {
            return Integer.compare(l1.getTimes(), l2.getTimes());
        }
    }
}
