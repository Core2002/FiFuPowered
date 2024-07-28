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

package `fun`.fifu.bookedit

import `fun`.fifu.bookedit.BookOperator.copyBook
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class BookEdit : JavaPlugin(), Listener {
    /**
     * 当插件加载时执行的函数。
     * 此函数主要负责初始化文件系统并创建插件目录。
     * onLoad函数没有参数和返回值。
     */
    override fun onLoad() {
        fs = this
        File("plugins/$pluginName/").mkdirs()
    }

    /**
     * 插件启用时的初始化函数。
     * 当插件被服务器加载并启用时，此函数将被调用，用于执行插件的初始化逻辑。
     *
     * 主要进行了以下操作：
     * 1. 输出欢迎信息，包含插件名称和作者，方便开发者识别和调试。
     * 2. 注册事件监听器，使得插件能够响应服务器中的各种事件，如玩家交互、方块改变等。
     * 3. 设置插件命令处理器，使得玩家可以通过命令行界面与插件交互，执行特定的功能。
     */
    override fun onEnable() {
        logger.info("欢迎使用$pluginName，author: NekokeCore")
        server.pluginManager.registerEvents(this, this)
        Bukkit.getPluginCommand("book")?.setExecutor(BookCommand())
    }

    /**
     * 处理玩家使用书籍的交互事件。
     * 此函数仅在玩家右键点击空气同时按住潜行键且视角朝下时触发。
     * 它的目的是将玩家手中的书籍替换为一本复制的书籍。
     *
     * @param event PlayerInteractEvent 事件对象，包含玩家交互的所有信息。
     */
    @EventHandler
    fun onShiftBook(event: PlayerInteractEvent) {
//        println("qwq action=${event.action} isSneaking=${event.player.isSneaking} yaw=${event.player.location.yaw} pitch=${event.player.location.pitch}")
        if (!(event.action == Action.RIGHT_CLICK_AIR && event.player.isSneaking && event.player.location.pitch == -90f))
            return
        val bookMeta = (event.item ?: return).itemMeta ?: return
        if (bookMeta is BookMeta) {
            event.player.inventory.setItemInMainHand(bookMeta.copyBook())
        }
    }

    companion object {
        lateinit var fs: BookEdit
        val pluginName = "FiFuBookEdit"
    }
}