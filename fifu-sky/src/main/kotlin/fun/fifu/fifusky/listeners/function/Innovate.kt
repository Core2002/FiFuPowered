package `fun`.fifu.fifusky.listeners.function

import org.bukkit.event.Listener
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import `fun`.fifu.fifusky.operators.SkyOperator.havePermission
import `fun`.fifu.fifusky.operators.SkyOperator.inSpawn
import `fun`.fifu.fifusky.operators.SkyOperator.isSkyWorld
import `fun`.fifu.fifusky.operators.SkyOperator.sendActionbarMessage
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler

import org.bukkit.event.player.PlayerSwapHandItemsEvent

import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent

/**
 * 模块：黑曜石变岩浆
 * @author NekokeCore
 */
class Innovate : Listener {

    /**
     * 当玩家交换副手时触发本事件.
     */
    @EventHandler
    fun onSwapHand(event: PlayerSwapHandItemsEvent) {
        if (!event.player.world.isSkyWorld()) return
        if (event.offHandItem != null && event.offHandItem!!.type == Material.BUCKET && event.player.isSneaking) {
            if (event.player.havePermission()) {
                val l: Location = event.player.location
                val location = Location(
                    event.player.world, l.blockX.toDouble(), (l.blockY - 1).toDouble(),
                    l.blockZ.toDouble()
                )
                val block: Block = location.block
                if (block.type == Material.OBSIDIAN) {
                    block.type = Material.LAVA
                    event.player.sendActionbarMessage("你成功将黑曜石转化成了岩浆！")
                }
            } else {
                event.player.sendActionbarMessage("你没权限")
            }
        }
    }


    /**
     * 玩家死亡
     *
     * @param event
     */
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (event.entity.location.inSpawn()) {
            if (event.entity.location.blockY < -150) {
                event.entity.sendActionbarMessage("这个世界虽然不完美，我们仍可以治愈自己")
            }
        }
    }

    /**
     * 点击牌子
     *
     * @param event
     */
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.hasBlock() && event.clickedBlock != null) {
            if (event.clickedBlock!!.state is Sign) {
                val sign: Sign = event.clickedBlock!!.state as Sign
                val lines: Array<String> = sign.lines
                if (lines[0].equals("[命令]", ignoreCase = true)) {
                    event.player.chat(lines[1])
                }
            }
        }
    }

}