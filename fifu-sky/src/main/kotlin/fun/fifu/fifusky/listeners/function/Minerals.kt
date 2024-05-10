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

import `fun`.fifu.fifusky.operators.SkyOperator.isSkyWorld
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockFormEvent

/**
 * 模块：挖石出矿
 * @author NekokeCore
 */
class Minerals : Listener {
    private fun getBlock(m: Material, b: Int): Material {
        val minerals = (Math.random() * 100).toInt()
        val chufalv = 45 + b
        if (minerals <= chufalv) {
            var luck = (Math.random() * 100).toInt()
            if (chufalv > 80 && luck > 80) {
                luck = (Math.random() * 80).toInt()
            }

            return when (luck) {
                in 21..34 -> Material.COAL_ORE // 煤炭
                in 35..44 -> Material.REDSTONE_ORE // 红石
                in 45..47 -> Material.IRON_ORE // 铁
                in 48..49 -> Material.GOLD_ORE // 金
                50 -> Material.DIAMOND_ORE // 钻石
                in 51..52 -> Material.LAPIS_ORE // 青金石
                in 53..56 -> Material.EMERALD_ORE // 绿宝石
                else -> m
            }
        } else {
            return m
        }
    }

    @EventHandler
    fun onBlockCanBuild(event: BlockFormEvent) {
        if (!event.block.world.isSkyWorld()) return
        if (event.newState.type == Material.COBBLESTONE) { //当破坏原石
            event.newState.type = getBlock(event.newState.type, 40)
        } else {
            if (event.newState.type == Material.STONE) { //当破坏石头
                if (event.block.world.hasStorm()) { //当下雨天
                    event.newState.type = getBlock(event.newState.type, 30)
                } else { //当非下雨天
                    event.newState.type = getBlock(event.newState.type, 20)
                }
            }
        }
    }
}