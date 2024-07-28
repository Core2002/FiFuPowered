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

    /**
     * 插件启用时执行的函数。
     * 此函数初始化一个定时任务，用于每小时检查是否需要进行备份。
     * 它利用Bukkit的调度系统，在游戏服务器中安排了一个任务，该任务每小时执行一次，检查当前时间是否适合进行备份操作。
     */
    override fun onEnable() {
        loopCheckTask = object : BukkitRunnable() {
            /**
             * 定时任务的执行逻辑。
             * 每次执行时，都会调用[checkCanBackup]检查是否可以进行备份，并通过日志告知当前是否需要进行备份。
             */
            override fun run() {
                checkCanBackup.run()
                logger.info("正在检查备份，现在${if (isTimeToDoBackup) "需要" else "不需要"}备份")
            }
        }.runTaskLater(this, 20 * 60 * 60)

    }
}