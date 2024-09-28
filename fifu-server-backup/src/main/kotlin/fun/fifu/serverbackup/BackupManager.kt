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

import com.google.gson.JsonElement
import dev.samstevens.totp.code.DefaultCodeGenerator
import dev.samstevens.totp.code.HashingAlgorithm
import dev.samstevens.totp.secret.DefaultSecretGenerator
import dev.samstevens.totp.secret.SecretGenerator
import dev.samstevens.totp.time.SystemTimeProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.concurrent.thread
import kotlin.io.path.isDirectory


object BackupManager {
    var isTimeToDoBackup = false
    var doBackup: Runnable
    var checkCanBackup: Runnable
    val configFileName = "ServerBackupConfig"
    val cacheFileName = "ServerBackupCache"
    val keysFileName = "ServerBackupKeys"
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
                val key = ByteArray(32) { SecureRandom().nextInt().toByte() }
                val iv = ByteArray(16) { SecureRandom().nextInt().toByte() }
                val encryptedBackupName = "$backupName.enc"
                encryptFile(backupName, encryptedBackupName, key, iv)
                File(backupName).delete()
                val sha512 = calculateSha512(encryptedBackupName)
                if (configPojo.sendToRemoteServer) uploadFile(encryptedBackupName, configPojo.sendRemoteServerUrl)
                ConfigCenter.setValue(
                    keysFileName, sha512, ConfigCenter.gson.fromJson(
                        ConfigCenter.gson.toJson(
                            mapOf(
                                "name" to encryptedBackupName,
                                "key" to byteArrayToHexString(key),
                                "iv" to byteArrayToHexString(iv)
                            )
                        ), JsonElement::class.java
                    )
                )
            }
        }
        checkCanBackup = Runnable {
            isTimeToDoBackup = configPojo.enableBackup && System.currentTimeMillis() > ConfigCenter.getValue(
                cacheFileName,
                "nextBackupTime"
            ).asLong
            if (isTimeToDoBackup) {
                doBackup.run()
                ConfigCenter.setValue(
                    cacheFileName,
                    "nextBackupTime",
                    ConfigCenter.convertToJsonPrimitive(System.currentTimeMillis() + 1000 * 60 * 60 * 24)
                )
            }
        }
    }

    fun hexStringToByteArray(hex: String): ByteArray {
        val byteArray = ByteArray(hex.length / 2)
        for (i in hex.indices step 2) {
            byteArray[i / 2] = hex.substring(i, i + 2).toInt(16).toByte()
        }
        return byteArray
    }

    fun byteArrayToHexString(byteArray: ByteArray): String {
        return byteArray.joinToString("") { "%02x".format(it) }
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

    /**
     * Encrypts a file using the AES encryption algorithm.
     *
     * This function encrypts a specified file using the AES/CBC/PKCS5Padding mode and saves the encrypted file to the specified path.
     * CBC mode requires an initialization vector (IV), which is specified through an IvParameterSpec in this function.
     *
     * @param inputPath The path of the input file.
     * @param outputPath The path of the output file.
     * @param key The encryption key, which must be a byte array of 32 bytes, used to generate a SecretKeySpec.
     * @param iv The initialization vector, which must be a byte array of 16 bytes, used to generate an IvParameterSpec.
     */
    fun encryptFile(inputPath: String, outputPath: String, key: ByteArray, iv: ByteArray) {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(key, "AES")
        val ivParameterSpec = IvParameterSpec(iv)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)

        val inputBytes = Files.readAllBytes(Paths.get(inputPath))
        val encryptedBytes = cipher.doFinal(inputBytes)

        Files.write(Paths.get(outputPath), encryptedBytes)
    }

    /**
     * Decrypts a file using AES-CBC mode with PKCS5 padding.
     *
     * @param inputPath The path to the input file that needs to be decrypted.
     * @param outputPath The path to the output file where the decrypted content will be written.
     * @param key The key used for decryption.
     * @param iv The initialization vector used to start the decryption process.
     */
    fun decryptFile(inputPath: String, outputPath: String, key: ByteArray, iv: ByteArray) {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(key, "AES")
        val ivParameterSpec = IvParameterSpec(iv)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)

        val inputBytes = Files.readAllBytes(Paths.get(inputPath))
        val decryptedBytes = cipher.doFinal(inputBytes)

        Files.write(Paths.get(outputPath), decryptedBytes)
    }

    /**
     * Uploads a file to the specified URL.
     *
     * @param filePath The path of the file to be uploaded.
     * @param url The URL to which the file is uploaded.
     * @throws IOException If the network request fails.
     */
    fun uploadFile(filePath: String, url: String) {
        val client = OkHttpClient()

        val file = File(filePath)
        val fileBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, fileBody)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(multipartBody)
            .addHeader("Authorization", getTOTPCode())
            .build()

        client.newCall(request).execute().use { response: Response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            println("ok! ${response.body?.string()}")
        }
    }

    private fun getTOTPCode(): String {
        if (configPojo.sendRemoteServerSecret == "" || configPojo.sendRemoteServerSecret.length < 64) {
            val secretGenerator: SecretGenerator = DefaultSecretGenerator(64)
            val secret = secretGenerator.generate()
            configPojo.sendRemoteServerSecret = secret
            ConfigCenter.setValue(configFileName, "sendRemoteServerSecret", ConfigCenter.convertToJsonPrimitive(secret))
        }
        val codeGenerator = DefaultCodeGenerator(HashingAlgorithm.SHA512)
        val timeProvider = SystemTimeProvider()

        return codeGenerator.generate(configPojo.sendRemoteServerSecret, timeProvider.time.floorDiv(30))
    }

    private fun calculateSha512(filePath: String): String {
        return try {
            val bytes = Files.readAllBytes(Paths.get(filePath))
            val md: MessageDigest = MessageDigest.getInstance("SHA-512")
            val messageDigest = md.digest(bytes)
            messageDigest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            throw RuntimeException("Failed to calculate SHA-512 for $filePath", e)
        }
    }

}