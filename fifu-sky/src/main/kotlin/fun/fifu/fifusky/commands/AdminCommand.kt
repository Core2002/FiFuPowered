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
import `fun`.fifu.fifusky.Sky
import `fun`.fifu.fifusky.listeners.function.FiFuItems
import `fun`.fifu.fifusky.operators.SkyOperator.addMember
import `fun`.fifu.fifusky.operators.SkyOperator.build
import `fun`.fifu.fifusky.operators.SkyOperator.getIsland
import `fun`.fifu.fifusky.operators.SkyOperator.isFiFuAdmin
import `fun`.fifu.fifusky.operators.SkyOperator.isSkyWorld
import `fun`.fifu.fifusky.operators.SkyOperator.removeMember
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaType

/**
 * 管理命令
 * @author NekokeCore
 */
class AdminCommand : TabExecutor {
    private val fiFuItems = mutableListOf<String>()

    private val helpMassage = mapOf(
        "build-island" to "/fs-admin build-island <SkyLoc> 来build一个岛",
        "make-member" to "/fs-admin make-member 将自己纳入该岛屿的成员",
        "exit-member" to "/fs-admin exit-member 将自己退出该岛屿成员",
        "get-item" to "/fs-admin get-item <FiFuItem> 获得FiFuItem",
        "view-player-inventory" to "/fs-admin view-player-inventory <玩家名> 查看玩家的背包",
        "view-player-ender-chest" to "fs-admin view-player-ender-chest <玩家名> 查看玩家的末影箱"
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
            "get-item" -> fiFuItems
            "view-player-inventory", "view-player-ender-chest" -> playersName
            else -> ml
        }
    }


    override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>): Boolean {
        println(
            """
            p0:$p0
            p1:$p1
            p2:$p2
            p3:${p3.contentToString()}
        """.trimIndent()
        )
        if (p0 !is Player || !p0.isFiFuAdmin()) {
            p0.sendMessage("你必须是FiFu管理员才能在游戏内使用此命令")
            return true
        }
        if (p3.isEmpty()) return onHelp(p0, p3)
        try {
            if (!p0.world.isSkyWorld()) {
                p0.sendMessage("你必须在空岛世界才能使用这条命令")
                return true
            }
            val re = when (p3[0]) {
                "help" -> onHelp(p0, p3)
                "build-island" -> onBuild(p3)
                "make-member" -> onMakeMember(p0)
                "exit-member" -> onExitMember(p0)
                "get-item" -> onGetItem(p0, p3)
                "view-player-inventory" -> onViewPlayerInventory(p0, p3)
                "view-player-ender-chest" -> onViewPlayerEnderChest(p0, p3)
                "test" -> onTest(p0, p3)
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

    private fun onTest(p0: Player, p3: Array<out String>): Boolean {
        return true
    }

    private fun onViewPlayerEnderChest(p0: Player, p3: Array<out String>): Boolean {
        if (p3[1].isEmpty())
            return true
        p0.closeInventory()
        Bukkit.getPlayer(p3[1])?.enderChest?.let { p0.openInventory(it) }
        return true
    }

    private fun onViewPlayerInventory(p0: Player, p3: Array<out String>): Boolean {
        if (p3[1].isEmpty())
            return true
        p0.closeInventory()
        Bukkit.getPlayer(p3[1])?.inventory?.let { p0.openInventory(it) }
        return true
    }

    private fun onGetItem(p0: Player, p3: Array<out String>): Boolean {
        if (!fiFuItems.contains(p3[1]))
            return true
        FiFuItems::class.companionObject?.functions?.filter { it.name == p3[1] }?.forEach {
            println("反射到物品 ${it.name}")
            val awa = it.call(FiFuItems::class.companionObjectInstance) as ItemStack
            p0.inventory.addItem(awa)
        }
        return true
    }

    private fun onExitMember(p0: Player): Boolean {
        p0.location.getIsland().removeMember(p0)
        p0.sendMessage("已将你从岛屿 ${p0.location.getIsland()} 中作为成员移除")
        return true
    }

    private fun onMakeMember(p0: Player): Boolean {
        p0.location.getIsland().addMember(p0)
        p0.sendMessage("已将你作为成员加入岛屿 ${p0.location.getIsland()}")
        return true
    }

    private fun onBuild(p3: Array<out String>): Boolean {
        Sky.getIsland(p3[1]).build()
        return true
    }

    private fun onHelp(player: Player, p3: Array<out String>): Boolean {
        if (p3.size == 1) {
            val sb = StringBuffer()
            helpMassage.values.forEach { sb.append(it).append("\n") }
            player.sendMessage("帮助：/fs-admin <命令>\n$sb")
            return true
        } else {
            helpMassage[p3[1]]?.let { player.sendMessage(it) }
        }
        return true
    }

    init {
        FiFuItems::class.companionObject?.functions?.filter {
            it.returnType.javaType.typeName == "org.bukkit.inventory.ItemStack"
        }?.forEach {
            fiFuItems.add(it.name)
        }
    }
}