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
    var backupServerDirPath: String = "./",

    /**
     * 备份文件在本地保留的最长时间（天数）。默认为7天。
     */
    var backupKeepDay: Int = 7,

    /**
     * 是否将备份文件发送到远程服务器。默认为false。
     */
    var sendToRemoteServer: Boolean = false,

    /**
     * 远程服务器的URL地址。默认为空字符串。
     */
    var sendRemoteServerUrl: String = "http://localhost:8080/upload"
)