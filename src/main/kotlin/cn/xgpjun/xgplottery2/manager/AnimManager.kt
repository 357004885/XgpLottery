package cn.xgpjun.xgplottery2.manager

import cn.xgpjun.xgplottery2.lottery.anim.multiple.MultipleAnim
import cn.xgpjun.xgplottery2.lottery.anim.multiple.impl.BoxMultipleAnim
import cn.xgpjun.xgplottery2.lottery.anim.multiple.impl.DefaultMultipleAnim
import cn.xgpjun.xgplottery2.lottery.anim.multiple.impl.SelectMultipleAnim
import cn.xgpjun.xgplottery2.lottery.anim.multiple.impl.SimpleMultipleAnim
import cn.xgpjun.xgplottery2.lottery.anim.single.SingleAnim
import cn.xgpjun.xgplottery2.lottery.anim.single.impl.*
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.wrappers.BlockPosition
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Lidded
import org.bukkit.entity.Player


object AnimManager {
    val singleAnim = HashMap<String,Class<out SingleAnim>>()
    val multipleAnim = HashMap<String,Class<out MultipleAnim>>()
    var protocolLibSupport = false
    fun register(){
        DefaultAnim().register()
        ColorfulAnim().register()
        MarqueeAnim().register()
        SelectAnim().register()
        VoidAnim().register()

        DefaultMultipleAnim().register()
        BoxMultipleAnim().register()
        SelectMultipleAnim().register()
        SimpleMultipleAnim().register()
        protocolLibSupport = Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")
    }

    fun getSingleI18Name(name:String):String{
        return singleAnim[name]?.getDeclaredConstructor()?.newInstance()?.i18nName?:""
    }
    fun getMultipleI18Name(name:String):String{
        return multipleAnim[name]?.getDeclaredConstructor()?.newInstance()?.i18nName?:""
    }

    fun getSingleAnimObject(name: String) = singleAnim[name]?.getConstructor()?.newInstance()?:DefaultAnim()


    fun getMultipleAnimObject(name:String) = multipleAnim[name]?.getConstructor()?.newInstance()?:DefaultMultipleAnim()

}

fun Class<out SingleAnim>.new(): SingleAnim = this.getDeclaredConstructor().newInstance()

fun Location?.openChest() {
    this?.block?.let {
        if (AnimManager.protocolLibSupport){
            if (block.type == Material.CHEST||block.type == Material.ENDER_CHEST){
                try {
                    val pm = ProtocolLibrary.getProtocolManager()
                    val packet = pm.createPacket(PacketType.Play.Server.BLOCK_ACTION)
                    packet.blockPositionModifier.write(0, BlockPosition(it.x,it.y,it.z))
                    packet.integers.write(0,1)
                    packet.integers.write(1,1)
                    this.getNearbyPlayer(10).forEach{ player ->
                        pm.sendServerPacket(player,packet)
                    }
                }catch (_:Exception){}
            }
        }else{
            //1.16+
            try {
                if(NMSManager.versionToInt>=16&&it.state is Lidded){
                    (it.state as Lidded).open()
                }
            }catch (_:Exception){
            }
        }
    }
}

fun Location?.closeChest() {
    this?.block?.let {
        if (AnimManager.protocolLibSupport){
            if (block.type == Material.CHEST||block.type == Material.ENDER_CHEST){
                val pm = ProtocolLibrary.getProtocolManager()
                val packet = pm.createPacket(PacketType.Play.Server.BLOCK_ACTION)
                packet.blockPositionModifier.write(0, BlockPosition(it.x,it.y,it.z))
                packet.integers.write(0,1)
                packet.integers.write(1,0)
                this.getNearbyPlayer(10).forEach{ player ->
                    pm.sendServerPacket(player,packet)
                }
            }
        }else{
            //1.16+
            try {
                if(NMSManager.versionToInt>=16&&it.state is Lidded){
                    (it.state as Lidded).close()
                }
            }catch (_:Exception){
            }
        }
    }
}

fun Location.getNearbyPlayer(paramDouble: Int): Set<Player> {
    val hashSet: HashSet<Player> = HashSet()
    for (player in Bukkit.getOnlinePlayers()) {
        if (player.location.world !== this.world) continue
        val d = this.distanceSquared(player.location)
        if (d <= paramDouble) hashSet.add(player)
    }
    return hashSet
}
