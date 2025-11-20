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

import com.google.gson.*
import java.io.File

object ConfigCenter {
    private val pluginName = "FiFuServerBackup"
    val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private var jsonFiles = HashMap<String, Pair<File, JsonObject>>()

    private fun getConfigObject(fileName: String): JsonObject {
        return if (jsonFiles.containsKey(fileName)) {
            jsonFiles[fileName]!!.second
        } else {
            val configFile = getConfigFile(fileName)
            val configObject = if (configFile.isFile) {
                val jsonText = configFile.readText()
                gson.fromJson(jsonText, JsonObject::class.java)
            } else {
                JsonObject()
            }
            jsonFiles[fileName] = Pair(configFile, configObject)
            configObject
        }
    }

    fun makeDefaultConfig(fileName: String, default: Any) {
        val defaultObject = gson.fromJson(gson.toJson(default), JsonObject::class.java)
        val configObject = getConfigObject(fileName)

        defaultObject.keySet().forEach {
            if (!configObject.has(it)) {
                configObject.add(it, defaultObject[it])
            }
        }
        save(fileName)
    }

    fun <T> readSnapshot(fileName: String, clazz: Class<T>): T {
        return try {
            gson.fromJson(gson.toJson(getConfigObject(fileName)), clazz)
        } catch (e: Exception) {
            throw RuntimeException("读取配置快照失败：$fileName", e)
        }
    }

    /**
     * 验证配置文件的有效性
     */
    fun validateConfig(fileName: String, validator: (JsonObject) -> Boolean): Boolean {
        return try {
            val config = getConfigObject(fileName)
            validator(config)
        } catch (e: Exception) {
            println("配置验证失败：$fileName, 错误：${e.message}")
            false
        }
    }

    private fun getConfigFile(fileName: String): File {
        return File("./plugins/$pluginName/${fileName}.json")
    }

    fun getValue(fileName: String, key: String): JsonElement {
        var element = getConfigObject(fileName)[key]
        if (element == null) {
            element = JsonObject()
        }
        return element
    }

    /**
     * Converts the given value to a JsonPrimitive object.
     * This function supports conversion of Boolean, Number, String, and Char types to JsonPrimitive.
     * If the value passed in is of an unsupported type, an IllegalArgumentException is thrown.
     *
     * @param value The value to convert to JsonPrimitive.
     * @return The resulting JsonPrimitive object.
     * @throws IllegalArgumentException if the type of the value is not supported.
     */
    fun convertToJsonPrimitive(value: Any): JsonPrimitive {
        return when (value) {
            is Boolean -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is String -> JsonPrimitive(value)
            is Char -> JsonPrimitive(value)
            else -> throw IllegalArgumentException("Unsupported type: ${value::class.java.name}")
        }
    }

    fun setValue(fileName: String, key: String, value: JsonElement) {
        getConfigObject(fileName).add(key, value)
        save(fileName)
    }

    fun saveAll() {
        jsonFiles.keys.forEach {
            save(it)
        }
    }

    private fun save(fileName: String) {
        if (jsonFiles.containsKey(fileName)) {
            val file = jsonFiles[fileName]!!.first
            val obj = jsonFiles[fileName]!!.second
            file.parentFile.mkdirs()
            file.writeText(gson.toJson(obj))
//            println(obj)
        } else {
            println("保存失败：$fileName")
        }
    }

}