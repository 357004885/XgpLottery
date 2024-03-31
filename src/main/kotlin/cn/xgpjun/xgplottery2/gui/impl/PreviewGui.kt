package cn.xgpjun.xgplottery2.gui.impl

import cn.xgpjun.xgplottery2.color
import cn.xgpjun.xgplottery2.gui.LotteryGui
import cn.xgpjun.xgplottery2.lottery.calculator.impl.int
import cn.xgpjun.xgplottery2.lottery.pojo.Lottery
import cn.xgpjun.xgplottery2.manager.DatabaseManager
import cn.xgpjun.xgplottery2.manager.Message
import cn.xgpjun.xgplottery2.utils.MyItemBuilder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import java.text.DecimalFormat
import kotlin.math.ceil
import kotlin.math.min

class PreviewGui(val lottery: Lottery,val player: Player) :LotteryGui(null){

    override val inv: Inventory = Bukkit.createInventory(this,54,Message.PreviewTitle.get(lottery.name).color())

    override fun handleClick(e: InventoryClickEvent) {
        val player = e.whoClicked as Player
        when(e.rawSlot){
            45->{
                page -= 1
                reloadGui()
            }
            53->{
                page +=1
                reloadGui()
            }
            8 -> getPreviousInventory(player)
        }
    }

    override fun reloadGui() {
        inv.clear()
        size = ceil(lottery.awards.size.toDouble() / 28).toInt().coerceAtLeast(1)
        page = 1.coerceAtLeast(page.coerceAtMost(size))
        pageItem()

        val weightSum = lottery.getTotalWeight()
        val df = DecimalFormat(Message.DecimalFormat.get())
        val single = lottery.calculator == "SingleGuaranteed"
        val playerData = if (single){
            DatabaseManager.getPlayerData(player.uniqueId)
        }else{
            null
        }
        lottery.awards
            .toList()
            .slice((page - 1) * 28 until min(page*28,lottery.awards.size))
            .forEachIndexed { index,(_,award) ->
                val gained = (playerData?.customData?.getOrDefault("Gained-${lottery.name}-${award.name}",0)?.int() != 0)
                val item = if (lottery.showProbability){
                    val str = if (award.weight == 0) Message.DecimalFormat.get() else df.format(award.weight.toDouble() / weightSum)
                    MyItemBuilder(award.item)
                        .addLore(Message.ShowProbability.get(str).color())
                }else{
                    MyItemBuilder(award.item)
                }
                if (single&&gained){
                    item.addLore(Message.Gained.get().color())
                }
                inv.setItem(slot[index],item.getItem())

        }
    }

    override fun getInventory() = inv

    fun openGui(){
        val top = player.openInventory.topInventory.holder
        if (top !is Player){
            previousInventory = top
        }
        reloadGui()
        player.openInventory(inv)
    }
}
