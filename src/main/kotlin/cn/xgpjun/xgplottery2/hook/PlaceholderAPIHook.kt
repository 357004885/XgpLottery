package cn.xgpjun.xgplottery2.hook

import cn.xgpjun.xgplottery2.color
import cn.xgpjun.xgplottery2.listener.PlayerListener
import cn.xgpjun.xgplottery2.manager.DatabaseManager
import cn.xgpjun.xgplottery2.manager.LotteryManager
import cn.xgpjun.xgplottery2.manager.Message
import cn.xgpjun.xgplottery2.utils.Config
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

object PlaceholderAPIHook {
    val enable:Boolean by lazy {
        Bukkit.getPluginManager().getPlugin("PlaceholderAPI")!=null
    }
    fun register(){
        if (enable){
            XLPlaceholder.register()
            XLPlaceholder.subRequest["total"] = Total
            XLPlaceholder.subRequest["individual"] = Individual
            XLPlaceholder.subRequest["ngt"] = NGT
            XLPlaceholder.subRequest["lottery"] = LotteryInfo
            XLPlaceholder.subRequest["key"] = Key
            XLPlaceholder.subRequest["freedraw"] = FreeDraw
        }
    }
}

object XLPlaceholder:PlaceholderExpansion(){

    val subRequest = HashMap<String,SubRequest>()
    override fun getIdentifier() = "XL2"

    override fun getAuthor() = "xgpjun"

    override fun getVersion() = Config.version

    override fun canRegister() = true

    override fun onRequest(p: OfflinePlayer?, params: String): String? {
        val args = params.split("_")
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
        return subRequest[args[0].lowercase(locale = Locale.getDefault())]?.onRequest(p,args)
    }
}

fun String.setPlaceholder(player: OfflinePlayer):String{
    return if (PlaceholderAPIHook.enable){
        PlaceholderAPI.setPlaceholders(player,this)
    }else this
}

interface SubRequest{
    fun onRequest(p: OfflinePlayer?, args:Array<String>?): String?
}

object Total:SubRequest{
    override fun onRequest(p: OfflinePlayer?, args: Array<String>?): String? {
        return p?.uniqueId?.let { DatabaseManager.getPlayerData(it).totalDrawCount.toString() }
    }
}

object Individual:SubRequest{
    override fun onRequest(p: OfflinePlayer?, args: Array<String>?): String? {
        return args?.getOrNull(1)?.let {
            p?.let { it1 ->
                DatabaseManager.getPlayerData(it1.uniqueId).individualPoolDrawCount.getOrDefault(it,0).toString()
            }
        }
    }
}

object NGT:SubRequest{
    override fun onRequest(p: OfflinePlayer?, args: Array<String>?): String? {
        return args?.getOrNull(1)?.let {
            p?.let { it1 ->
                DatabaseManager.getPlayerData(it1.uniqueId).customData.getOrDefault("nonGuaranteed$it",0).toString()
            }
        }
    }
}

object LotteryInfo:SubRequest{
    override fun onRequest(p: OfflinePlayer?, args: Array<String>?): String? {
        return args?.getOrNull(1)?.let {
            LotteryManager.getLottery(it)?.customTags?.getOrDefault("guaranteedCount",0)?.toString()
        }
    }
}

object Key:SubRequest{
    override fun onRequest(p: OfflinePlayer?, args: Array<String>?): String? {
        return args?.getOrNull(1)?.let {
            p?.uniqueId?.let {  uuid ->
                DatabaseManager.getPlayerData(uuid).keyCount.getOrDefault(it,0).toString()
            }
        }
    }
}

object FreeDraw:SubRequest{
    override fun onRequest(p: OfflinePlayer?, args: Array<String>?): String? {
        if (p ==null)
            return null
        val lottery = args?.getOrNull(1)?.let {
            LotteryManager.getLottery(it)
        }?:return null

        val playerData = DatabaseManager.getPlayerData(p.uniqueId)
        val last = playerData.customData.getOrDefault("freedraw-${lottery.name}","").toString()
        if (last==""){
            //无记录
            return Message.FreeDraw.get().color()
        }else{
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            var date = LocalDateTime.parse(last,formatter)
            val now = LocalDateTime.now()
            var duration = Duration.between(date,now)
            val minutesApart = duration.toMinutes()
            val permission = PlayerListener.getFreePermission(lottery, p.player!!)
            if (permission == 0){
                return Message.NoFreeDraw.get().color()
            }
            return if (minutesApart>permission){
                Message.FreeDraw.get().color()
            }else{
                date = date.plusMinutes(permission.toLong())
                duration = Duration.between(now,date)
                val hours = duration.toHours()
                val minutesInHour = duration.minusHours(hours).toMinutes()
                val time = String.format("%02d:%02d", hours, minutesInHour)
                Message.FreeDrawTip.get(time).color()
            }
        }
    }
}


