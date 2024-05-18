package cn.xgpjun.xgplottery2.listener

import cn.xgpjun.xgplottery2.enums.Sounds
import cn.xgpjun.xgplottery2.lottery.pojo.Lottery
import cn.xgpjun.xgplottery2.manager.*
import cn.xgpjun.xgplottery2.manager.DatabaseManager.save
import cn.xgpjun.xgplottery2.send
import cn.xgpjun.xgplottery2.utils.getItemInMainHand
import cn.xgpjun.xgplottery2.utils.isMainHand
import cn.xgpjun.xgplottery2.utils.setItemInMainHand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.permissions.PermissionAttachmentInfo
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern


object PlayerListener:Listener {

    var pluginOutDate :String? =null
    @EventHandler
    fun join(e:PlayerJoinEvent){
        if (e.player.isOp){
            pluginOutDate?.let {
                Message.VersionOutDate.get(pluginOutDate).send(e.player)
            }
        }
        SchedulerManager.getScheduler().runTaskLaterAsynchronously(40L){
            e.player.uniqueId.let {
                DatabaseManager.onlinePlayerData[it] = DatabaseManager.getPlayerData(it)
            }
        }
    }

    @EventHandler
    fun quit(e:PlayerQuitEvent){
        SchedulerManager.getScheduler().runTaskAsynchronously{
            val uuid = e.player.uniqueId
            DatabaseManager.onlinePlayerData[uuid]?.save()
            DatabaseManager.onlinePlayerData.remove(uuid)
        }
    }


    private val coolDown = HashMap<UUID,Long>()
    @EventHandler
    fun keyUse(e:PlayerInteractEvent){
        val player = e.player
        val items = player.getItemInMainHand()
        if (!items.hasItemMeta()){
            return
        }

        val nowTime = System.currentTimeMillis()
        val lastTime = coolDown.getOrDefault(e.player.uniqueId,nowTime - 114514)
        if (nowTime - lastTime < 50 * 4||!e.isMainHand()|| (e.action != Action.RIGHT_CLICK_AIR&&e.action != Action.RIGHT_CLICK_BLOCK)){
            return
        }
        coolDown[player.uniqueId] = nowTime
        items.getTag("XL2KEY")?.let {
            if (it == "")
                return
            e.isCancelled = true
            if (items.amount==1){
                player.setItemInMainHand(null)
            }else{
                items.amount = items.amount-1
                player.setItemInMainHand(items)
            }
            player.playSound(player.location, Sounds.PLING.get(), 1.0f, 1f)
            SchedulerManager.getScheduler().runTaskAsynchronously{
                val data = DatabaseManager.getPlayerData(player.uniqueId)
                data.keyCount[it] = data.keyCount.getOrDefault(it,0) + 1
                if (player.isOnline){
                    DatabaseManager.onlinePlayerData[player.uniqueId] = data
                }else{
                    data.save()
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun draw(e:PlayerInteractEvent){
        if (!e.isMainHand()){
            return
        }
        e.clickedBlock?.let {block->
            CrateManager.getLottery(block.location)?.let {
                e.isCancelled = true
                if (e.action == Action.RIGHT_CLICK_BLOCK){
                    if (e.player.isSneaking){
                        DrawManager.draw(e.player, it, isConsumeKeys = true, isMultiple =  true, crateLocation = block.location)
                    }else{
                        val permission = getFreePermission(it, e.player)
                        if (permission!=0&&permission!=9999999){
                           //得到上次免费抽奖时间。
                            val playerData = DatabaseManager.getPlayerData(e.player.uniqueId)
                            val last = playerData.customData.getOrDefault("freedraw-${it.name}","").toString()
                            if (last==""){
                                //可抽奖 记录当前时间
                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                playerData.customData["freedraw-${it.name}"] = LocalDateTime.now().format(formatter)
                                DrawManager.draw(e.player, it, isConsumeKeys = false, crateLocation = block.location)
                                return
                            }else{
                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                val date = LocalDateTime.parse(last,formatter)
                                val now = LocalDateTime.now()
                                val duration = Duration.between(date,now)
                                val minutesApart = duration.toMinutes()
                                if (minutesApart>permission){
                                    //可抽奖 记录当前时间
                                    playerData.customData["freedraw-${it.name}"] = LocalDateTime.now().format(formatter)
                                    DrawManager.draw(e.player, it, isConsumeKeys = false, crateLocation = block.location)
                                    return
                                }
                            }
                        }
                        DrawManager.draw(e.player, it, isConsumeKeys = true, crateLocation = block.location)
                    }
                }else{
                    MessageL.DrawTips.get(it.name, DatabaseManager.getPlayerData(e.player.uniqueId).keyCount.getOrDefault(it.virtualKeyName,0).toString()).send(e.player)
                    val permission = getFreePermission(it, e.player)
                    if (permission!=0&&permission!=9999999){
                        //获取权限,时间。
                        //得到上次免费抽奖时间。
                        val playerData = DatabaseManager.getPlayerData(e.player.uniqueId)
                        val last = playerData.customData.getOrDefault("freedraw-${it.name}","").toString()
                        if (last==""){
                            Message.FreeDraw.get().send(e.player)
                        }else{
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            var date = LocalDateTime.parse(last,formatter)
                            val now = LocalDateTime.now()
                            var duration = Duration.between(date,now)
                            val minutesApart = duration.toMinutes()

                            if (minutesApart>permission){
                                Message.FreeDraw.get().send(e.player)
                            }else{
                                date = date.plusMinutes(permission.toLong())
                                duration = Duration.between(now,date)
                                val hours = duration.toHours()
                                val minutesInHour = duration.minusHours(hours).toMinutes()
                                val time = String.format("%02d:%02d", hours, minutesInHour)
                                Message.FreeDrawTip.get(time).send(e.player)
                            }
                        }
                    }
                }
            }
        }
    }

    fun getFreePermission(lottery: Lottery,player: Player):Int{
        return player.effectivePermissions
            .filter { p: PermissionAttachmentInfo ->
                p.permission.startsWith("xl2.freedraw.${lottery.name}.")
            }
            .mapNotNull { obj: PermissionAttachmentInfo ->
                var result = 9999999
                val regex = "xl2\\.freedraw\\.${lottery.name}\\.(\\d+)"
                val pattern = Pattern.compile(regex)
                val matcher = pattern.matcher(obj.permission)
                if (matcher.find()) {
                    result = matcher.group(1).toInt()
                }
                result
            }
            .minOrNull()?:0
    }
}


