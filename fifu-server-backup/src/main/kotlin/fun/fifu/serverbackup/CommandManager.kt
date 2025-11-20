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

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

/**
 * 命令管理器，处理插件的所有用户命令
 */
class CommandManager : CommandExecutor, TabCompleter {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        when (command.name.lowercase()) {
            "backup" -> handleBackupCommand(sender, args)
            "fifu" -> handleFiFuCommand(sender, args)
            else -> return false
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {
        return when (command.name.lowercase()) {
            "backup" -> getBackupTabComplete(args)
            "fifu" -> getFiFuTabComplete(args)
            else -> null
        }
    }

    private fun handleBackupCommand(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("fifu.backup.use")) {
            sender.sendMessage("§c 你没有权限使用此命令！")
            return
        }

        when {
            args.isEmpty() -> showBackupHelp(sender)
            args[0].equals("start", true) -> startBackup(sender)
            args[0].equals("status", true) -> showBackupStatus(sender)
            args[0].equals("config", true) -> showBackupConfig(sender)
            args[0].equals("reload", true) -> reloadBackupConfig(sender)
            else -> showBackupHelp(sender)
        }
    }

    private fun handleFiFuCommand(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("fifu.admin")) {
            sender.sendMessage("§c 你没有权限使用此命令！")
            return
        }

        when {
            args.isEmpty() -> showFiFuHelp(sender)
            args[0].equals("version", true) -> showVersion(sender)
            args[0].equals("info", true) -> showInfo(sender)
            else -> showFiFuHelp(sender)
        }
    }

    private fun startBackup(sender: CommandSender) {
        try {
            sender.sendMessage("§e 正在启动手动备份...")
            BackupManager.checkCanBackup.run()
            sender.sendMessage("§a 备份任务已启动！")
        } catch (e: Exception) {
            sender.sendMessage("§c 备份启动失败：${e.message}")
        }
    }

    private fun showBackupStatus(sender: CommandSender) {
        sender.sendMessage("§6=== 备份状态 ===")
        sender.sendMessage("§f 备份功能：${if (BackupManager.configPojo.enableBackup) "§a启用" else "§c禁用"}")
        sender.sendMessage("§f 远程传输：${if (BackupManager.configPojo.sendToRemoteServer) "§a启用" else "§c禁用"}")
        sender.sendMessage("§f 备份目录：§7${BackupManager.configPojo.backupServerDirPath}")
        sender.sendMessage("§f 保留天数：§7${BackupManager.configPojo.backupKeepDay}天")
        sender.sendMessage("§f 是否需要备份：${if (BackupManager.isTimeToDoBackup) "§a是" else "§7否"}")
    }

    private fun showBackupConfig(sender: CommandSender) {
        sender.sendMessage("§6=== 备份配置 ===")
        sender.sendMessage("§f 启用备份：§7${BackupManager.configPojo.enableBackup}")
        sender.sendMessage("§f 备份目录：§7${BackupManager.configPojo.backupServerDirPath}")
        sender.sendMessage("§f 保留天数：§7${BackupManager.configPojo.backupKeepDay}")
        sender.sendMessage("§f 远程传输：§7${BackupManager.configPojo.sendToRemoteServer}")
        sender.sendMessage("§f 远程服务器：§7${BackupManager.configPojo.sendRemoteServerUrl}")
        sender.sendMessage("§f 最大上传大小：§7${BackupManager.configPojo.maxUploadSizeMB}MB")
        sender.sendMessage("§f 检查间隔：§7${BackupManager.configPojo.backupCheckIntervalHours}小时")
    }

    private fun reloadBackupConfig(sender: CommandSender) {
        try {
            ConfigCenter.makeDefaultConfig("ServerBackupConfig", BackupManager.configPojo)
            sender.sendMessage("§a 配置已重新加载！")
        } catch (e: Exception) {
            sender.sendMessage("§c 配置加载失败：${e.message}")
        }
    }

    private fun showVersion(sender: CommandSender) {
        sender.sendMessage("§6=== FiFuServerBackup ===")
        sender.sendMessage("§f 版本：§71.0.0")
        sender.sendMessage("§f 作者：§7NekokeCore")
        sender.sendMessage("§f 许可：§7Mulan PSL v2")
    }

    private fun showInfo(sender: CommandSender) {
        sender.sendMessage("§6=== FiFuServerBackup 信息 ===")
        sender.sendMessage("§f 这是一个 Minecraft 服务器备份插件")
        sender.sendMessage("§f 支持自动备份、加密传输、远程同步等功能")
        showVersion(sender)
    }

    private fun showBackupHelp(sender: CommandSender) {
        sender.sendMessage("§6=== 备份命令帮助 ===")
        sender.sendMessage("§f/backup start §7- 立即开始备份")
        sender.sendMessage("§f/backup status §7- 查看备份状态")
        sender.sendMessage("§f/backup config §7- 查看备份配置")
        sender.sendMessage("§f/backup reload §7- 重新加载配置")
    }

    private fun showFiFuHelp(sender: CommandSender) {
        sender.sendMessage("§6=== FiFu 命令帮助 ===")
        sender.sendMessage("§f/fifu version §7- 查看版本信息")
        sender.sendMessage("§f/fifu info §7- 查看插件信息")
    }

    private fun getBackupTabComplete(args: Array<out String>): List<String> {
        return when (args.size) {
            1 -> listOf("start", "status", "config", "reload").filter { it.startsWith(args[0], true) }
            else -> emptyList()
        }
    }

    private fun getFiFuTabComplete(args: Array<out String>): List<String> {
        return when (args.size) {
            1 -> listOf("version", "info").filter { it.startsWith(args[0], true) }
            else -> emptyList()
        }
    }
}