package `fun`.fifu.fifusky.listeners.function

import `fun`.fifu.fifusky.Sky
import `fun`.fifu.fifusky.data.SQLiteer
import `fun`.fifu.fifusky.operators.SoundPlayer
import `fun`.fifu.fifusky.operators.SkyOperator.tpIsland
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * 模块:欢迎萌新
 * @author NekokeCore
 */
class Welcome : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        SQLiteer.savePlayerIp(event.player.uniqueId.toString(), event.player.address.hostName)
        SQLiteer.savePlayerName(event.player.uniqueId.toString(), event.player.name)
        event.player.gameMode = GameMode.SURVIVAL
        try {
            SQLiteer.getPlayerIndex(event.player.uniqueId.toString())
        } catch (e: Exception) {
            event.player.tpIsland(Sky.SPAWN)
            event.player.sendTitle(
                "§a欢迎萌新owo ${event.player.displayName}",
                "§a使用/s以开始你的空岛生涯",
                10, 20 * 60 * 60 * 24, 20
            )
        }

        SoundPlayer.playCat(event.player)
    }
}