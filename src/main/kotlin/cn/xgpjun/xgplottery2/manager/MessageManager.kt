package cn.xgpjun.xgplottery2.manager

import cn.xgpjun.xgplottery2.XgpLottery
import org.bukkit.ChatColor
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object MessageManager {
    private var config:YamlConfiguration? = null

    fun load(){
        val file =File(XgpLottery.instance.dataFolder,"message.yml")
        config = if (file.exists()){
            YamlConfiguration.loadConfiguration(file)
        }else{
            XgpLottery.instance.saveResource("message.yml",false)
            YamlConfiguration.loadConfiguration(file)
        }
        Message.entries.forEach {it.string = loadMessage(it.path) }

        MessageL.entries.forEach{it.stringList = loadMessageList(it.path)}
    }
    private fun loadMessage(path:String):String{
        return config?.getString(path)?: "${ChatColor.RED}Missing Message:$path"
    }
    private fun loadMessageList(path:String):MutableList<String>{
        return config?.getStringList(path)?: arrayListOf("${ChatColor.RED}Missing Message:$path")
    }
}

enum class Message(val path: String){
    MissingNode("error.missingNode") {
        override fun get(vararg placeholders: String?): String {
            return string.replace("{file.name}",placeholders[0].toString()).replace("{node}",placeholders[1].toString())
        }
                                     },
    Gained("message.gained"),
    AwardNull("error.awardNull"),
    Prefix("message.prefix"),
    Success("message.success"),
    Processing("message.processing"),
    OnlyPlayer("error.onlyPlayer"),
    NotFoundLottery("error.notFoundLottery"),
    CrateExisted("error.crateExisted"),
    RemoveFailed("error.removeFailed"),
    NotSupport("error.notSupport"),
    NotFoundPlayer("error.notFoundPlayer"),
    InvFull("error.invFull"),
    NoEnoughKeys("error.noEnoughKeys"){
        override fun get(vararg placeholders: String?): String {
            return string.replace("{name}",placeholders[0].toString())
        }
                                      },
    TotalWeightNegative("error.totalWeightNegative"),
    MaxTimeLimit("error.maxTimeLimit"){
        override fun get(vararg placeholders: String?): String {
            return string.replace("{count}",placeholders[0].toString())
                .replace("{time}",placeholders[1].toString())
                .replace("{maxTime",placeholders[2].toString())
        }
                                      },
    RequireParticleLib("error.requireParticleLib"),
    ReceiveRewardFailed("error.receiveRewardFailed"){
        override fun get(vararg placeholders: String?): String {
            return string.replace("{count}",placeholders[0].toString())
        }
    },
    ReceiveMaximum("error.receiveMaximum"),
    VersionOutDate("message.versionOutDate"){
        override fun get(vararg placeholders: String?): String {
            return string.replace("{newVersion}",placeholders[0].toString())
        }
                                            },
    CreateCrateSuccessfully("message.createCrateSuccessfully"),
    CreateCrate("message.createCrate"){
        override fun get(vararg placeholders: String?): String {
            return string.replace("{name}",placeholders[0].toString())
        }
                                      },
    RemoveCrate("message.removeCrate"),
    CountClearConfirm("message.countClearConfirm"){
        override fun get(vararg placeholders: String?): String {
            return string.replace("{player}",placeholders[0].toString())
        }
                                                  },
    CountInquire("message.countInquire"){
        override fun get(vararg placeholders: String?): String {
            return string.replace("{lottery}",placeholders[0].toString()).replace("{count}",placeholders[1].toString())
        }
                                        },
    KeyClearConfirm("message.keyClearConfirm"){
        override fun get(vararg placeholders: String?): String {
            return string.replace("{player}",placeholders[0].toString())
        }
    },
    KeyInquire("message.keyInquire"){
        override fun get(vararg placeholders: String?): String {
            return string.replace("{key}",placeholders[0].toString()).replace("{count}",placeholders[1].toString())
        }
    },
    AllItemGained("message.allItemGained"),
    NoFreeDraw("message.noFreeDraw"),
    FreeDrawTip("message.freeDrawTip"){
        override fun get(vararg placeholders: String?): String {
            return string.replace("{time}",placeholders[0].toString())
        }
                                      },
    FreeDraw("message.freeDraw"),
    ManageTitle("gui.admin.manage.title"),
    PreviewTitle("gui.user.preview.title"){
        override fun get(vararg placeholders: String?): String {
            return string.replace("{name}",placeholders[0].toString())
        }
    },
    ItemBorderName( "gui.items.border.name"),
    ItemBorder2Name( "gui.items.border2.name"),
    ItemExitName("gui.items.exit.name"),
    ItemPreviousName("gui.items.previous.name"),
    ItemNextPageName("gui.items.nextPage.name"),
    ItemPreviousPageName("gui.items.previousPage.name"),

    SingleAnimChange("gui.message.singleAnimChange"){
        override fun get(vararg placeholders: String?): String {
            return string.replace("{name}",placeholders[0].toString())
        }
   },
    MultipleAnimChange("gui.message.multipleAnimChange"){
        override fun get(vararg placeholders: String?): String {
            return string.replace("{name}",placeholders[0].toString())
        }
    },
    ValueChange("gui.message.valueChange"),
    SellTypeChange("gui.message.sellTypeChange"){
        override fun get(vararg placeholders: String?): String {
            return string.replace("{name}",placeholders[0].toString())
        }
    },
    Cancel("gui.message.cancel"),
    AwardGuiTitle("gui.awardGui.title"),
    ClickToChange("gui.message.clickToChange"),
    NewAward("gui.message.newAward"),
    Existed("error.existed"),
    LotteryInfoName("gui.items.lotteryInfo.name"){
        override fun get(vararg placeholders: String?): String {
            return string.replace("{name}",placeholders[0].toString())
        }
                                                 },
    Point("noun.point"),
    Money("noun.money"),
    EXP("noun.exp"),
    ShowProbability("gui.showProbability"){
        override fun get(vararg placeholders: String?): String {
            return string.replace("{num}",placeholders[0].toString())
        }
    },
    DecimalFormat("gui.decimalFormat"),
    ChangeAwardItem("message.changeAwardItem"),
    ChangeAnimationName("gui.items.changeAnimation.name"),
    ChangeMultipleAnimationName("gui.items.changeMultipleAnimation.name"),
    GetKeyName("gui.items.getKey.name"),
    EditAwardName("gui.items.editAward.name"),
    UnusedName("gui.items.unused.name"),
    //动画
    DefaultSingleAnimTitle("gui.singleAnim.default.title"),
    DefaultSingleAnimName("gui.singleAnim.default.name"),
    ColorfulAnimTitle("gui.singleAnim.colorful.title"),
    ColorfulAnimName("gui.singleAnim.colorful.name"),
    ColorfulAnimClick("gui.singleAnim.colorful.click"),
    MarqueeAnimTitle("gui.singleAnim.marquee.title"),
    MarqueeAnimName("gui.singleAnim.marquee.name"),
    MarqueeAnimSelected("gui.singleAnim.marquee.selected"),
    SelectAnimTitle("gui.singleAnim.select.title"),
    SelectAnimName("gui.singleAnim.select.name"),
    SelectAnimClick("gui.singleAnim.select.click"),
    VoidAnimName("gui.singleAnim.void.name"),

    DefaultMultipleAnimTitle("gui.multipleAnim.default.title"),
    DefaultMultipleAnimName("gui.multipleAnim.default.name"),
    BoxMultipleAnimTitle("gui.multipleAnim.box.title"),
    BoxMultipleAnimName("gui.multipleAnim.box.name"),
    SelectMultipleAnimTitle("gui.multipleAnim.select.title"),
    SelectMultipleAnimName("gui.multipleAnim.select.name"),
    SimpleMultipleAnimTitle("gui.multipleAnim.simple.title"),
    SimpleMultipleAnimName("gui.multipleAnim.simple.name"),
    ;

    var string:String = "${ChatColor.RED}Not Init Yet:${name}"

    open fun get(vararg placeholders:String?):String{
        return string
    }
}

enum class MessageL(val path: String){
    HelpMessage("help.help"),
    CrateHelp("help.crate"),
    DrawHelp("help.draw"),
    PaidDrawHelp("help.paidDraw"),
    CountHelp("help.count"),
    KeyHelp("help.key"),
    GiveHelp("help.give"),
    PreviewHelp("help.preview"),
    ConvertHelp("help.convert"),
    ParticleHelp("help.particle"),
    ItemBorderLore("gui.items.border.lore"),
    ItemBorder2Lore("gui.items.border2.lore"),
    CrateListInfo("message.crateListInfo"){
        override fun get(vararg placeholders: String?): MutableList<String> {
            val result = ArrayList<String>()
            stringList.forEach{
                val string = it
                    .replace("{world}",placeholders[0].toString())
                    .replace("{x}",placeholders[1].toString())
                    .replace("{y}",placeholders[2].toString())
                    .replace("{z}",placeholders[3].toString())
                    .replace("{lotteryName}",placeholders[4].toString())
                result.add(string)
            }
            return result
        }
    },
    LotteryInfoLore("gui.items.lotteryInfo.lore"){
        override fun get(vararg placeholders: String?): MutableList<String> {
            val result = ArrayList<String>()
            stringList.forEach{
                val string = it
                    .replace("{animation}",placeholders[0].toString())
                    .replace("{multipleAnimation}",placeholders[1].toString())
                result.add(string)
            }
            return result
        }
    },
    DrawTips("message.drawTips"){
        override fun get(vararg placeholders: String?): MutableList<String> {
            val result = ArrayList<String>()
            stringList.forEach{
                val string = it
                    .replace("{name}",placeholders[0].toString())
                    .replace("{amount}",placeholders[1].toString())
                result.add(string)
            }
            return result
        }
    },
    ;
    var stringList:MutableList<String> = mutableListOf("${ChatColor.RED}Not Init Yet")
    open fun get(vararg placeholders:String?):MutableList<String>{
        return stringList
    }
}

private fun String?.toString():String{
    return this?:"noValue"
}
