package `fun`.fifu.mininglist

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import `fun`.fifu.mininglist.Middleware.pluginName
import java.io.File

object ConfigCenter {
    private val gson = GsonBuilder().setPrettyPrinting().create()
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
        return gson.fromJson(gson.toJson(getConfigObject(fileName)), clazz)
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
            println(obj)
        }else{
            println("保存失败：$fileName")
        }
    }

}