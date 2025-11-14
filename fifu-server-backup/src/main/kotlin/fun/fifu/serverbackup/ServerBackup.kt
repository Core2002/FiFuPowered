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

package `fun`.fifu.serverbackup

import `fun`.fifu.serverbackup.BackupManager.checkCanBackup
import `fun`.fifu.serverbackup.BackupManager.isTimeToDoBackup
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class ServerBackup : JavaPlugin() {
    lateinit var loopCheckTask: BukkitTask
    private lateinit var commandManager: CommandManager

    override fun onEnable() {
        // 初始化命令管理器
        commandManager = CommandManager()
        
        // 注册命令
        getCommand("backup")?.setExecutor(commandManager)
        getCommand("backup")?.setTabCompleter(commandManager)
        getCommand("fifu")?.setExecutor(commandManager)
        getCommand("fifu")?.setTabCompleter(commandManager)

        // 加载配置
        try {
            ConfigCenter.makeDefaultConfig("ServerBackupConfig", BackupManager.configPojo)
            logger.info("配置加载完成")
        } catch (e: Exception) {
            logger.severe("配置加载失败: ${e.message}")
        }

        // 启动定时任务
        val interval = BackupManager.configPojo.backupCheckIntervalHours * 20L * 60L * 60L
        loopCheckTask = object : BukkitRunnable() {
            override fun run() {
                try {
                    checkCanBackup.run()
                    logger.info("正在检查备份，现在${if (isTimeToDoBackup) "需要" else "不需要"}备份")
                } catch (e: Exception) {
                    logger.severe("备份检查过程中发生错误: ${e.message}")
                    e.printStackTrace()
                }
            }
        }.runTaskTimer(this, interval, interval)

        logger.info("FiFuServerBackup插件已启用")
    }

    override fun onDisable() {
        // 插件禁用时取消定时任务
        if (::loopCheckTask.isInitialized) {
            loopCheckTask.cancel()
            logger.info("备份检查任务已取消")
        }
        
        // 保存配置
        try {
            ConfigCenter.saveAll()
            logger.info("配置已保存")
        } catch (e: Exception) {
            logger.severe("配置保存失败: ${e.message}")
        }

        logger.info("FiFuServerBackup插件已禁用")
    }
}