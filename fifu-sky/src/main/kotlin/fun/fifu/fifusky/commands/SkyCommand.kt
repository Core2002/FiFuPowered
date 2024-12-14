/*
 * Copyright (c) 2023 NekokeCore(Core2002@aliyun.com)
 * FiFuPowered is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package `fun`.fifu.fifusky.commands

import `fun`.fifu.fifusky.FiFuSky
import `fun`.fifu.fifusky.Island
import `fun`.fifu.fifusky.Sky
import `fun`.fifu.fifusky.data.PlayerData
import `fun`.fifu.fifusky.data.SQLiteer
import `fun`.fifu.fifusky.operators.SkyOperator
import `fun`.fifu.fifusky.operators.SkyOperator.Spawn
import `fun`.fifu.fifusky.operators.SkyOperator.addOwner
import `fun`.fifu.fifusky.operators.SkyOperator.build
import `fun`.fifu.fifusky.operators.SkyOperator.currentIsland
import `fun`.fifu.fifusky.operators.SkyOperator.isOwnedIsland
import `fun`.fifu.fifusky.operators.SkyOperator.canGetIsland
import `fun`.fifu.fifusky.operators.SkyOperator.getAllowExplosion
import `fun`.fifu.fifusky.operators.SkyOperator.getIslandData
import `fun`.fifu.fifusky.operators.SkyOperator.getIslandHomes
import `fun`.fifu.fifusky.operators.SkyOperator.getMembersList
import `fun`.fifu.fifusky.operators.SkyOperator.getOwnersList
import `fun`.fifu.fifusky.operators.SkyOperator.havePermission
import `fun`.fifu.fifusky.operators.SkyOperator.isSkyWorld
import `fun`.fifu.fifusky.operators.SkyOperator.isUnclaimed
import `fun`.fifu.fifusky.operators.SkyOperator.removeOwner
import `fun`.fifu.fifusky.operators.SkyOperator.setAllowExplosion
import `fun`.fifu.fifusky.operators.SkyOperator.toChunkLoc
import `fun`.fifu.fifusky.operators.SkyOperator.tpIsland
import `fun`.fifu.fifusky.operators.SoundPlayer
import `fun`.fifu.fifusky.operators.Tpaer
import cn.hutool.cache.Cache
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import kotlin.random.Random
import cn.hutool.cache.CacheUtil
import org.bukkit.Chunk
import org.bukkit.block.Biome
import org.bukkit.block.BlockFace
import java.lang.StringBuilder
import java.util.*


/**
 * 玩家命令
 * @author NekokeCore
 */
class SkyCommand : TabExecutor {
    private val lruCache: Cache<UUID, String> = CacheUtil.newLRUCache(8 * 1000)

    private val pc = arrayListOf(
        'a', 'b', 'c', 'd', 'e', 'f', 'g',
        'h', 'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    )

    private val helpMassage = mapOf(
        "help" to "/s help [命令] 查看帮助",
        "get" to "/s get <SkyLoc> 领取一个岛屿，两个月只能领一次",
        "info" to "/s info 查询当前岛屿信息，/s info u 使用uuid查看",
        "homes" to "/s homes 查询你有权限的岛屿",
        "go" to "/s go <SkyLoc> 传送到目标岛屿",
        "add-member" to "/s add-member <玩家名> 把目标玩家添加到你所在的岛的成员里",
        "remove-member" to "/s remove-member <玩家名> 把目标玩家从你所在的岛里移除",
        "renounce" to "/s renounce 放弃你所在的岛屿",
        "biome" to "/s biome [生物群系/编号] 修改当前区块的生物群系，不填则是查看",
        "chunk" to "/s chunk AllowExplosion <on/off> 来修改区块可爆炸属性，其他以此类推",
        "set-home" to "/s set-home 变更/s的默认传送岛屿为当前所在的岛屿",
        "tpa" to "/s tpa [玩家名] 接受传送/请求传送到[玩家名]"
    )

    override fun onTabComplete(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): MutableList<String> {
        if (p0 !is Player) return mutableListOf()
        if (p3.size == 1) return helpMassage.keys.toMutableList()
        val ml = mutableListOf<String>()
        val playersName = mutableListOf<String>()
        Bukkit.getOnlinePlayers().forEach {
            playersName.add(it.name)
        }
        return when (p3[0]) {
            "biome" -> {
                if (p3.size == 2) Biome.values().forEach { ml.add(it.name()) };ml
            }
            "chunk" -> {
                if (p3.size == 2) ml.add("AllowExplosion")
                if (p3.size == 3) {
                    ml.add("on")
                    ml.add("off")
                }
                ml
            }
            "tpa", "add-member", "remove-member" -> {
                if (p3.size == 2)
                    ml.addAll(playersName)
                ml
            }
            "go" -> {
                if (p3.size == 2) {
                    ml.addAll(p0.getIslandHomes().first.split(' '))
                    ml.addAll(p0.getIslandHomes().second.split(' '))
                }
                ml
            }
            "help" -> {
                if (p3.size == 2)
                    ml.addAll(helpMassage.keys)
                ml
            }
            else -> ml
        }
    }

    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
        if (p0 !is Player) {
            p0.sendMessage("你必须是一名玩家")
            return true
        }
        if (p3.isEmpty()) return onS(p0)
        try {
            if (!p0.world.isSkyWorld() && !arrayOf("help", "?", "homes", "go", "tpa").contains(p3[0])) {
                p0.sendMessage("你必须在空岛世界才能使用这条命令")
                return true
            }
            val re = when (p3[0]) {
                "help" -> onHelp(p0, p3)
                "?" -> onHelp(p0, p3)
                "get" -> onGet(p0, p3)
                "info" -> onInfo(p0, p3)
                "homes" -> onHomes(p0)
                "go" -> onGo(p0, p3)
                "add-member" -> onAddMember(p0, p3)
                "remove-member" -> onRemoveMember(p0, p3)
                "renounce" -> onRenounce(p0, p3)
                "biome" -> onBiome(p0, p3)
                "chunk" -> onChunk(p0, p3)
                "tpa" -> onTpa(p0, p3)
                "set-home" -> onSetHome(p0)
                else -> false
            }
            if (!re) onHelp(p0, arrayOf("help", p3[0]))
        } catch (e: Exception) {
            onHelp(p0, arrayOf("help", p3[0]))
            FiFuSky.fs.logger.warning("$p0 的命令 /s ${p3.contentToString()} 导致了一个异常：")
            e.printStackTrace()
            return true
        }
        return true
    }

    private fun onSetHome(p0: Player): Boolean {
        val island = Sky.getIsland(p0.location.blockX, p0.location.blockZ).toString()
        val homeOwners = p0.getIslandHomes().first
        val homeMembers = p0.getIslandHomes().second
        if (homeOwners.contains(island) || homeMembers.contains(island)){
            SQLiteer.savePlayerIndex(p0.uniqueId.toString(), island)
            p0.sendMessage("成功变更默认传送岛屿为：$island ，使用/s可来回传送")
        }else{
            p0.sendMessage("你不是岛屿 $island 的所有者或成员，无权操作")
            return true
        }
        return true
    }

    private fun onTpa(p0: Player, p3: Array<out String>): Boolean {
        if (p3.size == 1)
            Tpaer.tpa(p0)
        else if (p3.size == 2) {
            val goto = Bukkit.getPlayer(p3[1])
            if (goto?.isOnline == true)
                Tpaer.tpa(p0, goto)
            else
                p0.sendMessage("玩家 ${p3[1]} 不在线")
        }
        return true
    }

    private fun onChunk(p0: Player, p3: Array<out String>): Boolean {
        if (p3.size < 3) return false
        if (!p0.isOwnedIsland(p0.currentIsland())) {
            p0.sendMessage("你不是该岛屿的所有者，无权操作")
            return false
        }
        if (p3[1] == "AllowExplosion") {
            when (p3[2]) {
                "on" -> {
                    p0.chunk.setAllowExplosion(true)
                    p0.sendMessage("区块 ${p0.chunk.toChunkLoc()} ${if (p0.chunk.getAllowExplosion()) "允许" else "不允许"} 爆炸")
                }
                "off" -> {
                    p0.chunk.setAllowExplosion(false)
                    p0.sendMessage("区块 ${p0.chunk.toChunkLoc()} ${if (p0.chunk.getAllowExplosion()) "允许" else "不允许"} 爆炸")
                }
                else -> p0.sendMessage(
                    """
                    当前所在区块是 ${p0.chunk.toChunkLoc()} 
                    该区块 ${if (p0.chunk.getAllowExplosion()) "允许" else "不允许"} 爆炸
                """.trimIndent()
                )
            }
        }
        return true
    }


    private fun onBiome(p0: Player, p3: Array<out String>): Boolean {
        fun work(chunk: Chunk, biome: Biome) {
            for (x in 0..15) for (y in 0..255) for (z in 0..15) chunk.getBlock(x, y, z).biome = biome
        }
        if (!p0.isOwnedIsland(p0.currentIsland())) {
            p0.sendMessage("你不是该岛屿的所有者，无权操作")
            return false
        }
        if (p3.size == 1) {
            val sb = StringBuilder().append("可用的生物群系有：\n")
            Biome.values()
                .forEachIndexed { index, biome -> sb.append(index).append('：').append(biome.name()).append('\n') }
            sb.append("你脚下的方块的生物群系是：").append(p0.location.block.getRelative(BlockFace.DOWN).biome.name())
            p0.sendMessage(sb.toString())
            return true
        }
        try {
            val biome = if (p3[1].isInt()) {
                Biome.values()[p3[1].toInt()]
            } else {
                Biome.valueOf(p3[1])
            }
            work(p0.chunk, biome)
            p0.sendMessage("你已成功将区块 ${p0.chunk.toChunkLoc()} 的生物群系改为 ${biome.name()}")
        } catch (e: Exception) {
            p0.sendMessage("输入有误： ${p3[1]} 不是一个有效的生物群系或编号")
        }
        return true
    }

    private fun onRenounce(p0: Player, p3: Array<out String>): Boolean {
        val island = p0.currentIsland()
        if (!p0.isOwnedIsland(island)) {
            p0.sendMessage("你不是该岛屿的所有者，无权操作")
            return false
        }
        val canGet = p0.canGetIsland()
        val islandNum = p0.getIslandHomes().first.split(' ').size
        if (canGet.first || islandNum > 1) {
            if (p3.size == 2 && p3[1] == lruCache[p0.uniqueId]) {
                island.removeOwner(p0)
                p0.sendMessage("操作完毕，玩家 ${p0.name} 放弃了岛屿 $island ")
            } else {
                val captcha = getCAPTCHA()
                lruCache.put(p0.uniqueId, captcha)
                p0.sendMessage(
                    """
                    注意！操作危险！该操作将放弃的所在的岛！
                    若要继续，请输入：
                    /s renounce ${lruCache[p0.uniqueId]}
                    注意！操作危险！该操作将放弃你所在的岛！
                """.trimIndent()
                )
            }

        } else {
            p0.sendMessage("因为你现在不能领取岛而又没有多余的岛，所以不能放弃岛，下次可以领取岛的时间：${canGet.second}")
        }
        return true
    }

    private fun onRemoveMember(p0: Player, p3: Array<out String>): Boolean {
        if (p3.size == 1) return false
        val member = Bukkit.getPlayer(p3[1])
        if (member == null) {
            p0.sendMessage("玩家 ${p3[1]} 不在线，无法操作")
            return true
        }
        val island = p0.currentIsland()
        if (!p0.isOwnedIsland(island)) {
            p0.sendMessage("你不是该岛屿的所有者，无权操作")
            return false
        }
        val memberUuid = member.uniqueId.toString()
        val islandData = SQLiteer.getIslandData(island)
        val playerData = PlayerData(memberUuid, member.name)
        val mems = islandData.Privilege.Member
        if (playerData in mems) {
            mems.remove(playerData)
            p0.sendMessage("操作完毕，已将成员 ${member.name} 从岛屿 $island 移除")
            SQLiteer.saveIslandData(islandData)
        } else {
            p0.sendMessage("玩家 ${member.name} 不是岛 $island 的成员")
        }
        return true
    }

    private fun onAddMember(p0: Player, p3: Array<out String>): Boolean {
        if (p3.size == 1) return false
        val member = Bukkit.getPlayer(p3[1])
        if (member == null) {
            p0.sendMessage("玩家 ${p3[1]} 不在线，无法操作")
            return true
        }
        val island = p0.currentIsland()
        if (!p0.isOwnedIsland(island)) {
            p0.sendMessage("你不是该岛屿的所有者，无权操作")
            return false
        }
        val memberUuid = member.uniqueId.toString()
        val islandData = SQLiteer.getIslandData(island)
        val playerData = PlayerData(memberUuid, member.name)
        val mems = islandData.Privilege.Member
        if (playerData in mems) {
            p0.sendMessage("玩家 ${member.name} 已经是岛 $island 的成员")
            return true
        } else {
            mems.add(playerData)
        }
        SQLiteer.saveIslandData(islandData)
        p0.sendMessage("操作完毕，已将成员 ${member.name} 添加到岛屿 $island")
        return true
    }

    private fun onGo(p0: Player, p3: Array<out String>): Boolean {
        if (p3.size == 1) return false
        val island = Sky.getIsland(p3[1])
        if (island.isUnclaimed()) {
            p0.sendMessage("没有 $island 这个岛屿")
            return true
        } else {
            p0.tpIsland(island)
        }
        return true
    }

    private fun onHomes(p0: Player): Boolean {
        val island = Sky.getIsland(p0.location.blockX, p0.location.blockZ)
        val homes = p0.getIslandHomes()
        val homeInfo = """
            ${p0.name} 现在所在的岛屿的SkyLoc是 $island
            你拥有的岛屿有：
            ${homes.first}
            你加入的岛屿有：
            ${homes.second}
        """.trimIndent()
        p0.sendMessage(homeInfo)
        return true
    }

    private fun onInfo(p0: Player, p3: Array<out String>): Boolean {
        val u = p3.size > 1 && p3[1] == "u"
        val island = Sky.getIsland(p0.location.blockX, p0.location.blockZ)
        val info = """
            ${p0.name} 现在所在的岛屿的SkyLoc是 $island
            该岛屿的X：${island.X}
            该岛屿的XX：${island.XX}
            该岛屿的Y：${island.Y}
            该岛屿的YY：${island.YY}
            该岛屿的主人有：
            ${island.getOwnersList(u)}
            该岛屿的成员有：
            ${island.getMembersList(u)}
            您在此岛屿 ${if (p0.havePermission()) "有" else "没有"} 权限
        """.trimIndent()
        p0.sendMessage(info)
        return true
    }

    private fun onGet(player: Player, p3: Array<out String>): Boolean {
        if (p3.size == 1) return false
        if (!player.canGetIsland().first) {
            player.sendMessage("每48小时只能领取一次岛，${player.canGetIsland().second}后可再次领取")
            return true
        }
        val island = Sky.getIsland(p3[1])
        if (island.isUnclaimed()) {
            island.build()
            island.addOwner(player)
            SkyOperator.playerGetOver(player, island.getIslandData())
            player.tpIsland(island)
        } else {
            player.sendMessage("岛屿 $island 已经有人领过了，主人是${island.getOwnersList()}")
        }
        return true
    }

    private fun onHelp(player: Player, p3: Array<out String>): Boolean {
        if (p3.size == 1) {
            val sb = StringBuffer()
            helpMassage.values.forEach { sb.append(it).append("\n") }
            player.sendMessage("帮助：/s <命令>\n$sb")
            return true
        } else {
            helpMassage[p3[1]]?.let { player.sendMessage(it) }
        }
        return true
    }

    private fun onS(p0: Player): Boolean {
        val island = getOrGenerateIsland(p0)

        if (Sky.isInIsland(p0.location.blockX, p0.location.blockZ, Sky.SPAWN)) {
            p0.sendMessage("欢迎回家")
            p0.tpIsland(island)
        } else {
            p0.sendMessage("欢迎回到主城")
            p0.teleport(Spawn)
        }
        SoundPlayer.playCat(p0)
        return true
    }

    private fun getOrGenerateIsland(player: Player): Island {
        return try {
            SQLiteer.getPlayerIndex(player.uniqueId.toString())
        } catch (e: RuntimeException) {
            generateNewIsland(player)
        }
    }

    private fun generateNewIsland(player: Player): Island {
        var temp: Island
        do {
            val xx = Random.nextInt(-Sky.MAX_ISLAND * Sky.SIDE, Sky.MAX_ISLAND * Sky.SIDE + 1)
            val zz = Random.nextInt(-Sky.MAX_ISLAND * Sky.SIDE, Sky.MAX_ISLAND * Sky.SIDE + 1)
            temp = Sky.getIsland(xx, zz)
        } while (SQLiteer.getIslandData(temp).Privilege.Owner.isNotEmpty())

        val iLD = SQLiteer.getIslandData(temp)
        iLD.Privilege.Owner.add(PlayerData(player.uniqueId.toString(), player.name))
        SkyOperator.playerGetOver(player, iLD)
        temp.build()

        return temp
    }

    /**
     * 获取验证码，范围是a-z 0-9
     * @return 验证码
     */
    private fun getCAPTCHA(): String {
        val sb = StringBuilder()
        for (x in 1..8) sb.append(pc.random())
        return sb.toString()
    }

    /**
     * 判断字符串是否为数字
     * @return true：是数字 false：不是数字
     */
    private fun String.isInt(): Boolean {
        return try {
            this.toInt()
            true
        } catch (e: Exception) {
            false
        }
    }


}