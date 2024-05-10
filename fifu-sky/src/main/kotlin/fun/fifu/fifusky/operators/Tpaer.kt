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

package `fun`.fifu.fifusky.operators

import `fun`.fifu.fifusky.FiFuSky
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

import org.bukkit.scheduler.BukkitRunnable

/**
 * Tpa操作者单例，负责处理玩家的tpa请求
 * @author NekokeCore
 */
object Tpaer {
    /**
     * 存储所有的tpa请求，方向是从左往右，重启后清空，用完清空，超时清空
     */
    var tpaRequest: MutableMap<String, String> = HashMap()

    fun tpa(from: Player, goto: Player = from) {
        val f = from.name
        val g = goto.name

        if (from == goto) {
            if (tpaRequest.isNotEmpty()) {
                tpaRequest.forEach { (t, u) ->
                    if (f == u) {
                        val fr = Bukkit.getPlayer(t)
                        if (fr?.isOnline == true) {
                            fr.teleport(goto)
                            tpaRequest.remove(t)
                            fr.sendMessage("已传送 $t 到 $u")
                            goto.sendMessage("已传送 $t 到 $u")
                        }
                    }
                }
            } else {
                from.sendMessage("没有玩家向你发送请求")
            }
        } else {
            tpaRequest[f] = g
            from.sendMessage("已向玩家 ${goto.name} 发起传送请求，8秒未回应则销毁")
            goto.sendMessage("玩家 ${from.name} 想传送到你这里来，在8秒内输入/s tpa可同意传送")
            object : BukkitRunnable() {
                override fun run() {
                    if (!tpaRequest.remove(f).isNullOrEmpty())
                        from.sendMessage("向玩家 ${goto.name} 发起传送请求已销毁")
                }
            }.runTaskLater(FiFuSky.fs, 20L * 8)
        }
    }
}