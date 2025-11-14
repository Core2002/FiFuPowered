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

/**
 * 配置类，用于存储和管理备份和远程传输相关的设置。
 */
data class ConfigPojo(
    /**
     * 是否启用备份功能。默认为false。
     */
    var enableBackup: Boolean = false,

    /**
     * 备份文件存储目录的路径。默认为当前目录。
     */
    var backupServerDirPath: String = "./backups",

    /**
     * 备份文件在本地保留的最长时间（天数）。默认为7天。
     */
    var backupKeepDay: Int = 7,

    /**
     * 是否将备份文件发送到远程服务器。默认为false。
     */
    var sendToRemoteServer: Boolean = false,

    /**
     * 远程服务器的URL地址。默认为空字符串，需要用户手动配置。
     */
    var sendRemoteServerUrl: String = "",

    /**
     * TOTP密钥，用于验证身份。默认为空字符串，首次使用时自动生成。
     */
    var sendRemoteServerSecret: String = "",

    /**
     * 最大文件上传大小（MB）。默认为1024MB（1GB）。
     */
    var maxUploadSizeMB: Int = 1024,

    /**
     * 备份检查间隔（小时）。默认为1小时。
     */
    var backupCheckIntervalHours: Int = 1
)