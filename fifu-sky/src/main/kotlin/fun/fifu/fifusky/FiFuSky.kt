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

package `fun`.fifu.fifusky

import `fun`.fifu.fifusky.commands.AdminCommand
import `fun`.fifu.fifusky.commands.SkyCommand
import `fun`.fifu.fifusky.data.SQLiteer
import `fun`.fifu.utils.PackageUtil
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

/**
 * FiFuSky 插件主类
 * @author NekokeCore
 */
class FiFuSky : JavaPlugin() {

    companion object {
        lateinit var fs: FiFuSky
    }

    /**
     * 插件加载后的初始化
     */
    override fun onLoad() {
        fs = this
        val pluginDir = File("plugins/FiFuSky")
        if (!pluginDir.exists() || !pluginDir.isDirectory) {
            saveResource("FiFuSky.db", false)
            saveResource("db.setting", false)
        }
    }

    /**
     * 插件启动后的初始化
     */
    override fun onEnable() {
        //注册命令
        Bukkit.getPluginCommand("s")?.setExecutor(SkyCommand())
        Bukkit.getPluginCommand("fs-admin")?.setExecutor(AdminCommand())

        //注册监听器
        PackageUtil.getClassName("fun.fifu.fifusky.listeners")?.forEach {
            try {
                val any = Class.forName(it).newInstance()
                if (any is Listener){
                    logger.info("扫描 $it")
                    any.register()
                }
            }catch (_:Exception){
            }
        }

        logger.info(SQLiteer.getIslandData(Sky.getIsland("(0,0)")).toString())
        logger.info("FiFu 空岛插件已启动！")
    }

    /**
     * 注册该监听器
     */
    private fun Listener.register() {
        server.pluginManager.registerEvents(this, this@FiFuSky)
        logger.info("监听器 ${this.javaClass.simpleName} 注册完毕")
    }
}