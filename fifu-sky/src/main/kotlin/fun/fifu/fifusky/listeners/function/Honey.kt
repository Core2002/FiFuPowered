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

import `fun`.fifu.fifusky.FiFuSky
import `fun`.fifu.fifusky.operators.SkyOperator.isSkyWorld
import `fun`.fifu.fifusky.operators.SkyOperator.sendActionbarMessage
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack

import java.util.ArrayList

/**
 * 模块:喝蜂蜜起飞
 * @author NekokeCore
 */
class Honey : Listener {
    companion object {
        var honeyPlayer: MutableList<String> = ArrayList()
    }


    @EventHandler
    fun onItemConsumeEvent(event: PlayerItemConsumeEvent) {
        val item: ItemStack = event.item
        if (item.type == Material.HONEY_BOTTLE) {
            val player = event.player
            if (!player.world.isSkyWorld()) return
            val uuid = player.uniqueId.toString()
            if (honeyPlayer.contains(uuid)) {
                player.sendActionbarMessage("小蜜蜂~嗡嗡嗡~")
                event.isCancelled = true
                return
            }
            honeyPlayer.add(uuid)
            player.allowFlight = true
            player.isFlying = true
            player.sendActionbarMessage("你现在是一只小蜜蜂，可以短暂飞行了(500tick)")
            object : BukkitRunnable() {
                override fun run() {
                    object : BukkitRunnable() {
                        override fun run() {
                            player.isFlying = false
                            player.allowFlight = false
                            honeyPlayer.remove(uuid)
                        }
                    }.runTaskLater(FiFuSky.fs, 200)
                    player.sendActionbarMessage("短暂飞行将在200个tick后结束")
                }
            }.runTaskLater(FiFuSky.fs, 300)
        }
    }
}