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
                val currentHealth = health.toInt()
                val maxHealth = getAttribute(Attribute.MAX_HEALTH)!!.value.toInt()
                val ratio = currentHealth.toDouble() / maxHealth
                val colorMap = mapOf(
                    0.825..1.0 to "§a",
                    0.66..0.825 to "§2",
                    0.495..0.66 to "§e",
                    0.33..0.495 to "§6",
                    0.165..0.33 to "§c",
                    0.0..0.165 to "§4"
                )
                val color = colorMap.entries.firstOrNull { it.key.contains(ratio) }?.value ?: "§f"
                player.sendTitle("", "$color$name->HP:$currentHealth/$maxHealth", 2, 20, 6)
            }
        }.runTaskLater(BloodVolumeDisplay.plugin, 1)
    }
}