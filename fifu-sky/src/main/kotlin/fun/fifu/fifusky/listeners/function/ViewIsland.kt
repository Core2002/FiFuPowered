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

package `fun`.fifu.fifusky.listeners.function

import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.IORuntimeException
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import `fun`.fifu.fifusky.FiFuSky
import `fun`.fifu.fifusky.Island
import `fun`.fifu.fifusky.Sky
import `fun`.fifu.fifusky.data.SQLiteer
import `fun`.fifu.fifusky.operators.SkyOperator.currentIsland
import `fun`.fifu.fifusky.operators.SkyOperator.getIsland
import `fun`.fifu.fifusky.operators.SkyOperator.getOwnersList
import `fun`.fifu.fifusky.operators.SkyOperator.tpIsland
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.scheduler.BukkitRunnable


/**
 * 参观岛屿模块
 * @author NekokeCore
 */
class ViewIsland : Listener {
    /**
     * 岛屿参观操作者单例，负责带领玩家参观全部的岛屿
     */
    companion object {
        val canViewIsland: MutableList<Island> = SQLiteer.getAllSkyLoc()
        val viewingAllIndex: MutableMap<String, Int> = mutableMapOf()

        const val starIslandPath = "~/star_island.json"
        var stars = mutableSetOf<Island>()
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()

        init {
            try {
                val readUtf8String = FileUtil.readUtf8String(starIslandPath)
                if (readUtf8String.isNotEmpty())
                    stars = gson.fromJson(readUtf8String, Array<Island>::class.java).toMutableSet()
            } catch (_: IORuntimeException) {
                FileUtil.touch(starIslandPath)
            }

        }

        /**
         * 带领参观所有的岛屿
         * @param player 要参观岛屿的玩家
         * @param index 从哪里开始参观
         */
        fun startViewAll(player: Player, index: Int = 0) {
            if (viewingAllIndex.contains(player.uniqueId.toString())) {
                player.sendMessage("冷却中... ${((viewingAllIndex[player.uniqueId.toString()]!! + 1.0) / canViewIsland.size) * 100} %")
                return
            }
            viewingAllIndex[player.uniqueId.toString()] = index
            val listIterator = if (index < canViewIsland.size) {
                canViewIsland.listIterator(index)
            } else {
                canViewIsland.listIterator(0)
            }

            player.sendMessage("你正在参观岛屿，退出游戏即可退出。")
            object : BukkitRunnable() {
                override fun run() {
                    if (!listIterator.hasNext()) {
                        this.cancel()
                        viewingAllIndex.remove(player.uniqueId.toString())
                        player.tpIsland(Sky.SPAWN)
                        player.gameMode = GameMode.SURVIVAL
                        player.sendMessage("参观结束")
                        return
                    }
                    player.gameMode = GameMode.SPECTATOR
                    viewingAllIndex[player.uniqueId.toString()] = listIterator.nextIndex()
                    val next = listIterator.next()
                    player.tpIsland(next)
                    player.sendMessage("${canViewIsland.indexOf(next)} / ${canViewIsland.indices.last} : ${next.SkyLoc} ${next.getOwnersList()}")
                }
            }.runTaskTimer(FiFuSky.fs, 0, 20L * 10)
        }
    }

    private val starViewingIndex: MutableMap<String, Int> = mutableMapOf()

    /**
     * 副手手持弈颗星，滚轮参观全服岛屿
     */
    @EventHandler
    fun starView(event: PlayerItemHeldEvent) {
        if (event.player.inventory.itemInOffHand != FiFuItems.theStar() || !event.player.hasPermission("fifu.sky.starview"))
            return

        if (viewingAllIndex.contains(event.player.uniqueId.toString())) {
            event.player.sendMessage("冷却中... ${((viewingAllIndex[event.player.uniqueId.toString()]!! + 1.0) / canViewIsland.size) * 100} %")
            return
        }

        if (!starViewingIndex.containsKey(event.player.name)) {
            starViewingIndex[event.player.name] = 0
        }

        var index = starViewingIndex[event.player.name]!!
        if (event.newSlot == 0 && event.previousSlot == 8 || !(event.newSlot == 8 && event.previousSlot == 0) && event.newSlot > event.previousSlot) {
            index++
            if (index !in canViewIsland.indices)
                index = canViewIsland.indices.first
        } else if ((event.newSlot == 8 && event.previousSlot == 0) || event.newSlot < event.previousSlot) {
            index--
            if (index !in canViewIsland.indices)
                index = canViewIsland.indices.last
        }

        starViewingIndex[event.player.name] = index

        event.player.gameMode = GameMode.ADVENTURE
        event.player.tpIsland(canViewIsland[starViewingIndex[event.player.name]!!])
        event.player.sendMessage(
            "index $index / ${canViewIsland.indices.last} island:${event.player.location.getIsland()} 主人:${
                event.player.location.getIsland().getOwnersList()
            }"
        )

    }

    /**
     * 标记岛屿的功能
     * 副手手持弈颗星，左键标记，右键取消，左击方块输出到控制台
     */
    @EventHandler
    fun makeStar(event: PlayerInteractEvent) {
        val player = event.player
        if (player.inventory.itemInOffHand != FiFuItems.theStar())
            return

        val island = player.location.getIsland()
        when (event.action) {
            Action.LEFT_CLICK_AIR -> {
                if (stars.contains(island)) {
                    stars.remove(island)
                    player.sendMessage("已将 $island 添加至Star，目前共 ${stars.size} 个岛屿")
                } else {
                    stars.add(island)
                    player.sendMessage("已将 $island 从Star移除，目前共 ${stars.size} 个岛屿")
                }
                FileUtil.writeUtf8String(gson.toJson(stars).toString(), starIslandPath)
            }

            Action.RIGHT_CLICK_BLOCK -> {
                val currentIsland = player.currentIsland()
                if (stars.contains(currentIsland)) {
                    val currentIndex = stars.indexOf(currentIsland)
                    val nextIndex = if (currentIndex + 1 >= stars.size) 0 else currentIndex + 1
                    player.tpIsland(stars.elementAt(nextIndex))
                    player.sendMessage("[ ${nextIndex + 1} / ${stars.size} ] ${island.getOwnersList()} -> $island")
                }
            }

            else -> return
        }
    }

}