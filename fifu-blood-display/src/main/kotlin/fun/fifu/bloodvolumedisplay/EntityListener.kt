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

package `fun`.fifu.bloodvolumedisplay

import org.bukkit.attribute.Attribute
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.scheduler.BukkitRunnable

/**
 * 实体监听器
 * @author NekokeCore
 */
class EntityListener : Listener {
    /**
     * 实体被攻击时触发
     * 显血
     * @param event
     */
    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val entity: Entity = event.entity
        if (entity is LivingEntity) {
            if (event.damager is Player) {
                val player = event.damager as Player
                entity.showDamage(player)
            } else if (event.damager is Projectile) {
                if ((event.damager as Projectile).shooter is Player) {
                    val player = (event.damager as Projectile).shooter as Player?
                    if (player != null) entity.showDamage(player)
                }
            }
        }
    }

    /**
     * 交互实体时触发
     * 显血
     * @param event
     */
    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        val entity: Entity = event.rightClicked
        if (entity is LivingEntity) entity.showDamage(event.player)
    }

    /**
     * 显示一个实体的血量给玩家
     * @param player 要显示的玩家
     */
    private fun LivingEntity.showDamage(player: Player) {
        object : BukkitRunnable() {
            override fun run() {
                val i = health.toInt()
                val j = getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value.toInt()
                var color = "§f"
                val c = i / 1.0 / j
                if (c in 0.825..1.0) {
                    color = "§a"
                } else if (c < 0.825 && c >= 0.66) {
                    color = "§2"
                } else if (c < 0.66 && c >= 0.495) {
                    color = "§e"
                } else if (c < 0.495 && c >= 0.33) {
                    color = "§6"
                } else if (c < 0.33 && c >= 0.165) {
                    color = "§c"
                } else if (c < 0.165) {
                    color = "§4"
                }
//                player.sendTitle("", "$color$name->HP:$i/$j", 2, 20, 6)
                ActionbarUtil.sendMessage(player, "$color$name->HP:$i/$j")
            }
        }.runTaskLater(BloodVolumeDisplay.plugin, 1)
    }
}