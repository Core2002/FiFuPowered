/*
 * FiFuServerBackup 单元测试
 */

package `fun`.fifu.serverbackup

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class BackupManagerTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var testConfig: ConfigPojo

    @BeforeEach
    fun setUp() {
        testConfig = ConfigPojo(
            enableBackup = true,
            backupServerDirPath = tempDir.toString(),
            backupKeepDay = 7,
            sendToRemoteServer = false,
            sendRemoteServerUrl = "",
            sendRemoteServerSecret = "test_secret_key_1234567890123456789012345678901234567890123456789012345678901234",
            maxUploadSizeMB = 100,
            backupCheckIntervalHours = 1
        )
    }

    @Test
    fun `test SHA-512 calculation`() {
        // 创建测试文件
        val testFile = tempDir.resolve("test.txt").toFile()
        testFile.writeText("Hello, World!")

        // 计算哈希值
        val hash = BackupManager.calculateSha512(testFile.absolutePath)
        
        // 验证哈希值不为空且长度正确
        assertNotNull(hash)
        assertEquals(128, hash.length) // SHA-512 产生 128 位十六进制字符串
        
        // 验证相同文件产生相同哈希
        val hash2 = BackupManager.calculateSha512(testFile.absolutePath)
        assertEquals(hash, hash2)
    }

    @Test
    fun `test file encryption and decryption`() {
        // 创建测试文件
        val testFile = tempDir.resolve("test.txt").toFile()
        val originalContent = "这是一个测试文件内容，包含中文字符。"
        testFile.writeText(originalContent)

        val encryptedFile = tempDir.resolve("test.enc").toFile()
        val decryptedFile = tempDir.resolve("test_decrypted.txt").toFile()

        // 生成密钥和 IV
        val key = ByteArray(32) { it.toByte() }
        val iv = ByteArray(16) { it.toByte() }

        // 加密文件
        assertDoesNotThrow {
            BackupManager.encryptFile(testFile.absolutePath, encryptedFile.absolutePath, key, iv)
        }
        assertTrue(encryptedFile.exists())
        assertTrue(encryptedFile.length() > 0)

        // 解密文件
        assertDoesNotThrow {
            BackupManager.decryptFile(encryptedFile.absolutePath, decryptedFile.absolutePath, key, iv)
        }
        assertTrue(decryptedFile.exists())

        // 验证解密后的内容
        val decryptedContent = decryptedFile.readText()
        assertEquals(originalContent, decryptedContent)
    }

    @Test
    fun `test file not found exception`() {
        assertThrows(RuntimeException::class.java) {
            BackupManager.calculateSha512("non_existent_file.txt")
        }
    }

    @Test
    fun `test config validation`() {
        // 测试有效配置
        assertTrue(testConfig.backupKeepDay > 0)
        assertTrue(testConfig.maxUploadSizeMB > 0)
        assertTrue(testConfig.backupCheckIntervalHours > 0)

        // 测试备份目录路径
        val backupDir = File(testConfig.backupServerDirPath)
        assertTrue(backupDir.exists() || backupDir.mkdirs())
    }
}

class DataServerTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `test date parsing from filename`() {
        // 测试正常日期格式
        val date1 = DataServer.parseDateFromFilename("backup_2023-12-25.zip")
        assertNotNull(date1)
        assertEquals(2023, date1!!.year)
        assertEquals(12, date1.monthValue)
        assertEquals(25, date1.dayOfMonth)

        // 测试无日期格式
        val date2 = DataServer.parseDateFromFilename("backup.zip")
        assertNull(date2)

        // 测试无效日期格式（月份超出范围）
        val date3 = DataServer.parseDateFromFilename("backup_2023-13-45.zip")
        assertNull(date3)

        // 测试其他日期格式
        val date4 = DataServer.parseDateFromFilename("server_backup_2024-01-15.tar.gz")
        assertNotNull(date4)
        assertEquals(2024, date4!!.year)
        assertEquals(1, date4.monthValue)
        assertEquals(15, date4.dayOfMonth)

        // 测试边界情况
        val date5 = DataServer.parseDateFromFilename("backup_2024-02-29.zip") // 2024 年是闰年
        assertNotNull(date5)
        assertEquals(2024, date5!!.year)
        assertEquals(2, date5.monthValue)
        assertEquals(29, date5.dayOfMonth)

        // 测试非闰年的 2 月 29 日
        val date6 = DataServer.parseDateFromFilename("backup_2023-02-29.zip")
        assertNull(date6)
    }
}

class ConfigCenterTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `test JSON primitive conversion`() {
        // 测试布尔值
        val boolPrimitive = ConfigCenter.convertToJsonPrimitive(true)
        assertTrue(boolPrimitive.isBoolean)
        assertTrue(boolPrimitive.asBoolean)

        // 测试数字
        val numberPrimitive = ConfigCenter.convertToJsonPrimitive(42)
        assertTrue(numberPrimitive.isNumber)
        assertEquals(42, numberPrimitive.asInt)

        // 测试字符串
        val stringPrimitive = ConfigCenter.convertToJsonPrimitive("test")
        assertTrue(stringPrimitive.isString)
        assertEquals("test", stringPrimitive.asString)

        // 测试字符
        val charPrimitive = ConfigCenter.convertToJsonPrimitive('A')
        assertTrue(charPrimitive.isString)
        assertEquals("A", charPrimitive.asString)
    }

    @Test
    fun `test unsupported type conversion`() {
        assertThrows(IllegalArgumentException::class.java) {
            ConfigCenter.convertToJsonPrimitive(listOf(1, 2, 3))
        }
    }
}