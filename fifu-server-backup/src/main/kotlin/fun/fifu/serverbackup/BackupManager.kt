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

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import java.io.BufferedOutputStream
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread
import kotlin.io.path.isDirectory


object BackupManager {
    var isTimeToDoBackup = false
    var doBackup: Runnable
    var checkCanBackup: Runnable
    val configFileName = "ServerBackupConfig"
    var configPojo: ConfigPojo

    init {
        ConfigCenter.makeDefaultConfig(configFileName, ConfigPojo())
        configPojo = ConfigCenter.readSnapshot(configFileName, ConfigPojo::class.java)
        doBackup = Runnable {
            thread(start = true) {
                val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val backupName = "./backup_${LocalDateTime.now().format(timeFormatter)}.tar.gz"
                createTarGzipByFolder(Paths.get(configPojo.backupServerDirPath), Paths.get(backupName))
                println("The compression is complete, and the file is saved in ${backupName}.")
            }
        }
        checkCanBackup = Runnable {
            isTimeToDoBackup = configPojo.enableBackup && configPojo.nextBackupTime > System.currentTimeMillis()
            if (isTimeToDoBackup) {
                doBackup.run()
            }
        }
    }

    /**
     * Creates a tar.gz compressed file from the contents of a specified folder.
     *
     * @param source The path of the folder to be packed.
     * @param target The path of the resulting tar.gz file.
     * This function walks through all files and subfolders in the source folder,
     * packaging them into a single tar.gz compressed file.
     * It handles recursive packing of files and folders as well as symbolic link processing.
     */
    fun createTarGzipByFolder(source: Path, target: Path) {
        Files.newOutputStream(target).use { fileOut ->
            BufferedOutputStream(fileOut).use { buffOut ->
                GzipCompressorOutputStream(buffOut).use { gzOut ->
                    TarArchiveOutputStream(gzOut).use { tarOut ->
                        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX)
                        if (source.isDirectory()) {
                            Files.walkFileTree(source, object : SimpleFileVisitor<Path>() {
                                override fun visitFile(path: Path, attributes: BasicFileAttributes): FileVisitResult {
                                    // Keep symbolicLink and target
                                    if (attributes.isSymbolicLink || path == target) return FileVisitResult.CONTINUE
                                    val tarEntry = TarArchiveEntry(path.toFile(), source.relativize(path).toString())
                                    tarOut.putArchiveEntry(tarEntry)
                                    Files.copy(path, tarOut)
                                    tarOut.closeArchiveEntry()
                                    return FileVisitResult.CONTINUE
                                }
                            })
                        } else {
                            tarOut.putArchiveEntry(TarArchiveEntry(source))
                            Files.copy(source, tarOut)
                            tarOut.closeArchiveEntry()
                        }
                        tarOut.finish()
                    }
                }
            }
        }
    }

}