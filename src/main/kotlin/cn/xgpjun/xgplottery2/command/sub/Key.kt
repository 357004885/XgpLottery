package cn.xgpjun.xgplottery2.command.sub

import cn.xgpjun.xgplottery2.command.filter
import cn.xgpjun.xgplottery2.manager.*
import cn.xgpjun.xgplottery2.manager.DatabaseManager.save
import cn.xgpjun.xgplottery2.send
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

object Key :TabExecutor{

    /**
     * /xl key inquire playerName
     * /xl key add playerName keyName amount
     * /xl key sub playerName keyName amount
     * /xl key clear playerName (confirm)
     */
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): MutableList<String>? {
        return when(args.size){
            2 -> mutableListOf("inquire","add","sub","clear").filter(args)
            3 -> null
            4 -> if(args[2] != "inquire") {
                val set = HashSet<String>()
                LotteryManager.lotteryMap.values.forEach{set.add(it.virtualKeyName)}
                set.toMutableList().filter(args)
            }else arrayListOf()
            else -> arrayListOf()
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.isOp){
            return true
        }
        if (args.size<3){
            help(sender)
            return true
        }
        val player = Bukkit.getOfflinePlayer(args[2])
        SchedulerManager.getScheduler().runTaskAsynchronously{
            val data = DatabaseManager.getPlayerData(player.uniqueId)
            when(args[1]){
                "inquire" ->{
                    data.keyCount.forEach{
                        Message.KeyInquire.get(it.key,it.value.toString()).send(sender)
                    }
                }
                "add" ->{
                    if (args.size!=5){
                        help(sender)
                        return@runTaskAsynchronously
                    }
                    data.keyCount[args[3]] = data.keyCount.getOrDefault(args[3],0) + args[4].toInt()
                    if (player.isOnline){
                        DatabaseManager.onlinePlayerData[player.uniqueId] = data
                    }else{
                        data.save()
                    }
                    Message.Success.get().send(sender)
                }
                "sub" ->{
                    if (args.size!=5){
                        help(sender)
                        return@runTaskAsynchronously
                    }
                    data.keyCount[args[3]] = data.keyCount.getOrDefault(args[3],0) - args[4].toInt()
                    if (player.isOnline){
                        DatabaseManager.onlinePlayerData[player.uniqueId] = data
                    }else{
                        data.save()
                    }
                    Message.Success.get().send(sender)
                }
                "clear" ->{
                    if (args.getOrNull(3)!="confirm"){
                        Message.KeyClearConfirm.get(args[2]).send(sender)
                        return@runTaskAsynchronously
                    }
                    data.keyCount.clear()
                    if (player.isOnline){
                        DatabaseManager.onlinePlayerData[player.uniqueId] = data
                    }else{
                        data.save()
                    }
                    Message.Success.get().send(sender)
                }
            }
        }
        return true
    }
    fun help(sender: CommandSender){
        MessageL.KeyHelp.get().send(sender)
    }
}
