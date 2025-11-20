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

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.samstevens.totp.code.DefaultCodeGenerator
import dev.samstevens.totp.code.DefaultCodeVerifier
import dev.samstevens.totp.code.HashingAlgorithm
import dev.samstevens.totp.time.SystemTimeProvider
import `fun`.fifu.serverbackup.BackupManager.configPojo
import `fun`.fifu.serverbackup.BackupManager.dataPatten
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import java.io.File
import java.nio.file.Paths
import java.io.FileReader
import java.io.FileWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

object DataServer {
    private val vertx = Vertx.vertx()
    private val timeProvider = SystemTimeProvider()
    private val codeGenerator = DefaultCodeGenerator(HashingAlgorithm.SHA512)
    private val verifier = DefaultCodeVerifier(codeGenerator, timeProvider).apply {
        setAllowedTimePeriodDiscrepancy(3)
    }

    data class CacheEntry(val filePath: String, val hash: String)

    private val hashCache = ConcurrentHashMap<String, String>()
    private val cacheFile = File("plugins/hash_cache.json")
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    init {
        loadCache()
        vertx.setPeriodic(12 * 60 * 60 * 1000) {
            scanExpiredFilesAndDeleted()
        }
    }

    private fun loadCache() {
        if (cacheFile.exists()) {
            val reader = FileReader(cacheFile)
            val cacheEntries = gson.fromJson(reader, Array<CacheEntry>::class.java)
            for (entry in cacheEntries) {
                hashCache[entry.filePath] = entry.hash
            }
            reader.close()
        }
    }

    private fun saveCache() {
        val cacheEntries = hashCache.map { CacheEntry(it.key, it.value) }
        val writer = FileWriter(cacheFile)
        gson.toJson(cacheEntries, writer)
        writer.close()
    }

    /**
     * Starts an HTTP server and sets the size limit for uploaded files.
     *
     * @param port The port number the server listens on.
     * @param limitBytes The maximum allowed size of uploaded files (in bytes).
     */
    fun startHTTPServer(port: Int, limitBytes: Long) {
        val server = vertx.createHttpServer(
            HttpServerOptions()
        )

        val router = Router.router(vertx)

        val bodyHandler = BodyHandler.create()
            .setUploadsDirectory("uploads")
            .setBodyLimit(limitBytes)

        // Enable body handling with file uploads going to a temporary directory
        router.route().handler(bodyHandler)

        router.get("/upload/hashtable")
            .handler(this::validateCode)
            .handler(this::getHashTable)

        router.post("/upload")
            .handler(this::validateCode)
            .handler(this::uploadFile)

        server.requestHandler(router).listen(port) { http ->
            if (http.succeeded()) {
                println("HTTP server started on port ${http.result().actualPort()}")
            } else {
                println("HTTP server failed to start")
            }
        }
    }

    private fun handleFileDeletion(routingContext: RoutingContext) {
        routingContext.cancelAndCleanupFileUploads()
        val clientIp = routingContext.request().remoteAddress().host()
        val fileUploads = routingContext.fileUploads()
        for (fileUpload in fileUploads) {
            val uploadedFileName = fileUpload.uploadedFileName()
            println("An unverified attacker has been intercepted. The upload of the file <$uploadedFileName> has been canceled. Their IP address is <$clientIp>.")
            vertx.fileSystem().delete(uploadedFileName)
        }
    }

    /**
     * Handles file upload requests.
     *
     * This function first checks if there are any file uploads in the RoutingContext.
     * If not, it ends the response with a 400 status code and an error message.
     * If files are uploaded, it iterates through each file, logs the file name and size,
     * and moves the uploaded file to a permanent storage location.
     * If the file movement is successful, it ends the response with a success message.
     * If the file movement fails, it ends the response with a 200 status code and a failure message.
     *
     * @param routingContext The context of the routing request, containing the uploaded file information.
     */
    private fun uploadFile(routingContext: RoutingContext) {
        val fileUploads = routingContext.fileUploads()
        if (fileUploads.isEmpty()) {
            routingContext.response().setStatusCode(400).end("No file uploaded")
            return
        }

        // 检查备份目录是否存在
        val backupDir = File(configPojo.backupServerDirPath)
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }

        var successCount = 0
        var failureCount = 0
        val maxFileSize = 1024L * 1024L * 1024L // 1GB 限制

        for (fileUpload in fileUploads) {
            val uploadedFileName = fileUpload.uploadedFileName()
            val fileName = fileUpload.fileName()
            val fileSize = fileUpload.size()

            println("接收到文件：$fileName (大小：$fileSize bytes)")

            // 文件大小检查
            if (fileSize > maxFileSize) {
                println("文件过大，拒绝上传：$fileName")
                vertx.fileSystem().delete(uploadedFileName)
                failureCount++
                continue
            }

            // 文件名安全检查
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                println("文件名包含非法字符，拒绝上传：$fileName")
                vertx.fileSystem().delete(uploadedFileName)
                failureCount++
                continue
            }

            // 移动文件到永久位置
            val targetPath = Paths.get(configPojo.backupServerDirPath, fileName).toString()
            vertx.fileSystem().move(uploadedFileName, targetPath) {
                if (it.succeeded()) {
                    println("文件上传成功：$fileName")
                    successCount++
                } else {
                    println("文件移动失败：$fileName, 错误：${it.cause()?.message}")
                    vertx.fileSystem().delete(uploadedFileName)
                    failureCount++
                }
            }
        }

        val responseMessage = "上传完成 - 成功：$successCount, 失败：$failureCount"
        routingContext.response().setStatusCode(if (failureCount == 0) 200 else 207).end(responseMessage)
    }

    /**
     * Validates the client's access authorization.
     *
     * This function checks if the client has provided a valid authorization code. If not, it returns an unauthorized error.
     * If the authorization is valid, it allows the request to proceed to the next handler.
     *
     * @param routingContext The Vert.x routing context containing the request and response objects, among others.
     */
    private fun validateCode(routingContext: RoutingContext) {
        // 检查授权头是否存在
        val codeForClient = routingContext.request().getHeader("Authorization")
        if (codeForClient == null || codeForClient.isBlank()) {
            println("缺少授权头，来自 IP: ${routingContext.request().remoteAddress().host()}")
            handleFileDeletion(routingContext)
            routingContext.response().setStatusCode(401).end("Missing authorization header")
            return
        }

        // 检查密钥是否已配置
        if (configPojo.sendRemoteServerSecret.isBlank()) {
            println("服务器未配置 TOTP 密钥")
            handleFileDeletion(routingContext)
            routingContext.response().setStatusCode(500).end("Server not configured")
            return
        }

        try {
            if (!verifier.isValidCode(configPojo.sendRemoteServerSecret, codeForClient)) {
                println("授权验证失败，来自 IP: ${routingContext.request().remoteAddress().host()}")
                handleFileDeletion(routingContext)
                routingContext.response().setStatusCode(401).end("Unauthorized")
                return
            }
        } catch (e: Exception) {
            println("授权验证异常：${e.message}, 来自 IP: ${routingContext.request().remoteAddress().host()}")
            handleFileDeletion(routingContext)
            routingContext.response().setStatusCode(401).end("Invalid token")
            return
        }

        // 授权验证通过
        println("授权验证成功，来自 IP: ${routingContext.request().remoteAddress().host()}")
        routingContext.next()
    }



    /**
     * Get the HASH table of files in the upload directory
     * This function responds to requests by providing the SHA-512 HASH values of all files in the upload directory
     * It first checks if the upload directory exists, then iterates through each file in the directory
     * For each file, it attempts to retrieve the HASH value from the cache; if not found, it calculates the HASH value and saves it to the cache
     * Finally, it returns all HASH values as the response
     *
     * @param routingContext The routing context, containing request and response objects, used to handle HTTP requests and responses
     */
    private fun getHashTable(routingContext: RoutingContext) {
        val uploadsDir = File("uploads")
        val responseList = mutableSetOf<String>()
        if (uploadsDir.isDirectory) {
            for (file in uploadsDir.listFiles()!!) {
                val filePath = file.absolutePath
                val hash = hashCache[filePath] ?: run {
                    val newHash = BackupManager.calculateSha512(filePath)
                    hashCache[filePath] = newHash
                    saveCache()
                    newHash
                }
                responseList.add(hash)
            }
        }
        routingContext.response().setStatusCode(200).end(gson.toJson(responseList))
    }

    val datePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}")

    /**
     * 从文件名中解析日期
     * 支持格式：backup_YYYY-MM-DD.zip 或类似格式
     *
     * @param filename 文件名
     * @return 解析出的日期，如果无法解析则返回 null
     */
    fun parseDateFromFilename(filename: String): LocalDate? {
        try {
            // 使用正则表达式匹配日期格式
            val pattern = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})")
            val matcher = pattern.matcher(filename)
            
            if (matcher.find()) {
                val year = matcher.group(1).toInt()
                val month = matcher.group(2).toInt()
                val day = matcher.group(3).toInt()
                
                // 验证日期的有效性
                return try {
                    LocalDate.of(year, month, day)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            // 忽略解析错误
        }
        return null
    }

    /**
     * Scans the backup directory for expired files and deletes them.
     *
     * This method iterates through all files in the specified backup directory,
     * checks if each file has exceeded the configured retention period, and deletes
     * any files that are older than the retention period. It logs the deletion of
     * each expired file and prints a completion message at the end.
     *
     * @see ConfigPojo.backupServerDirPath The path to the backup directory.
     * @see ConfigPojo.backupKeepDay The number of days files should be retained before deletion.
     */
    private fun scanExpiredFilesAndDeleted() {
        try {
            val backupDir = File(configPojo.backupServerDirPath)
            if (!backupDir.exists() || !backupDir.isDirectory) {
                println("备份目录不存在或不是目录：${configPojo.backupServerDirPath}")
                return
            }

            val files = backupDir.listFiles()
            if (files == null) {
                println("无法读取备份目录文件列表")
                return
            }

            val currentDate = LocalDate.now()
            var deletedCount = 0
            var errorCount = 0

            for (file in files) {
                if (file.isFile) {
                    try {
                        val fileDate = parseDateFromFilename(file.name)
                        if (fileDate != null) {
                            val daysBetween = ChronoUnit.DAYS.between(fileDate, currentDate)
                            if (daysBetween >= configPojo.backupKeepDay) {
                                if (file.delete()) {
                                    deletedCount++
                                    println("已删除过期文件：${file.name} (日期：$fileDate)")
                                } else {
                                    errorCount++
                                    println("删除文件失败：${file.name}")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        errorCount++
                        println("处理文件时出错：${file.name}, 错误：${e.message}")
                    }
                }
            }
            
            println("过期文件扫描完成 - 删除：$deletedCount, 错误：$errorCount")
        } catch (e: Exception) {
            println("扫描过期文件时发生错误：${e.message}")
        }
    }
}