package `fun`.fifu.mininglist

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Statistic
import org.json.simple.JSONObject
import org.json.simple.JSONValue
import java.io.File
import java.math.BigInteger
import java.nio.charset.Charset
import java.util.*

/**
 * 中间件对象，负责处理与排名相关的数据操作。
 * 包括读取、存储和计算玩家的挖掘数量，以及管理玩家名称和UUID之间的映射。
 */
object Middleware {
    var data: JSONObject // 存储玩家挖掘数量的数据对象
    var uuid2name: JSONObject // 存储玩家UUID和名称映射的数据对象
    var ignore: JSONObject // 存储被忽略的玩家UUID的数据对象
    const val pluginName = "FiFuMiningList" // 插件名称
    lateinit var ranking: ArrayList<String> // 排名列表

    /**
     * 初始化中间件，加载配置文件。
     */
    init {
        data = initConfigFile("data")
        uuid2name = initConfigFile("uuid2name")
        ignore = initConfigFile("ignore")
    }

    /**
     * 初始化排名列表。
     */
    fun init() {
        ranking = calLeaderboard()
    }

    /**
     * 退出时保存所有数据。
     */
    fun unInit() {
        saveAll()
    }

    // 用于快速排序的辅助列表
    private val arr = arrayListOf<BigInteger>()

    // 临时存储匹配到的UUID列表
    private val over = arrayListOf<String>()

    // 最终的排名列表
    private val end = ArrayList<String>()

    /**
     * 计算并返回排名列表。
     */
    fun calLeaderboard(): ArrayList<String> {
        arr.clear()
        for (x in data.keys)
            arr.add(BigInteger(readData(x.toString())))
        quickSort(arr, 0, arr.size - 1)
        over.clear()
        for (x in arr) {
            for (u in data.keys) {
                val num = BigInteger(data[u] as String)
                if (num == x)
                    over.add(u.toString())
            }
        }
        end.clear()
        for (x in 0 until over.size)
            if (!end.contains(over[over.size - 1 - x]))
                end.add(over[over.size - 1 - x])

        return end
    }

    /**
     * 读取指定UUID的挖掘数量。
     */
    fun readData(uuid: String): String {
        if (data[uuid] == null)
            return "0"
        return data[uuid] as String
    }

    /**
     * 更新指定UUID的挖掘数量。
     */
    fun putData(uuid: String, num: String) {
        data[uuid] = num
        makeChangeFlag()
    }

    /**
     * 根据UUID获取玩家名称。
     */
    fun uuid2name(uuid: String): String {
        if (uuid2name[uuid] == null)
            return "<null>"
        return uuid2name[uuid] as String
    }

    /**
     * 更新UUID和玩家名称的映射。
     */
    fun putUuid2Name(uuid: String, name: String) {
        if (!uuid2name.contains(uuid)) {
            var oldMineBlockNum = 0
            for (m in Material.entries) {
                if (m.isBlock) {
                    oldMineBlockNum +=
                        Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getStatistic(Statistic.MINE_BLOCK, m)
                }
            }
            uuid2name[uuid] = name
            MiningList.plugin.logger.info("成功为玩家 $name 加载统计数据")
            val mineBlockNum = BigInteger(oldMineBlockNum.toString())
            putData(uuid, mineBlockNum.toString())
        }
        uuid2name[uuid] = name
        makeChangeFlag()
    }

    /**
     * 检查指定UUID是否被忽略。
     */
    fun inIgnore(uuid: String): Boolean {
        for (x in ignore.keys) {
            if (x as String == uuid)
                return true
        }
        return false
    }

    /**
     * 将指定UUID添加到忽略列表。
     */
    fun addIgnore(uuid: String) {
        ignore[uuid] = System.currentTimeMillis().toString()
        makeChangeFlag()
    }

    /**
     * 从忽略列表中移除指定UUID。
     */
    fun removeIgnore(uuid: String) {
        ignore.remove(uuid)
    }

    // 上次数据修改的时间戳
    private var lastChangeTime = System.currentTimeMillis()

    /**
     * 标记数据已修改，需要保存。
     */
    private fun makeChangeFlag() {
        if (canDoSave()) {
            saveAll()
        }
        lastChangeTime = System.currentTimeMillis()
    }

    /**
     * 检查是否达到保存数据的条件。
     */
    private fun canDoSave(): Boolean {
        return System.currentTimeMillis() > lastChangeTime + 1000 * 5
    }

    /**
     * 保存所有相关数据到配置文件中。
     *
     * 此函数负责调用[saveConfigFile]来保存不同类型的配置数据，包括排行榜数据、UUID到名称的映射，以及忽略列表。
     * 通过统一调用[saveConfigFile]函数，减少了重复代码，提高了代码的维护性和可读性。
     *
     * @see saveConfigFile
     * @see calLeaderboard
     */
    private fun saveAll() {
        ranking = calLeaderboard()
        saveConfigFile(data, "data")
        saveConfigFile(uuid2name, "uuid2name")
        saveConfigFile(ignore, "ignore")
    }

    /**
     * 保存配置文件。
     *
     * 该函数用于将JSONObject序列化为JSON字符串，并写入指定名称的配置文件中。如果文件不存在，则会初始化文件。
     *
     * @param jsonObject 要保存的JSONObject对象，它将被转换为JSON字符串并写入文件。
     * @param name 配置文件的名称，不包括后缀。该名称将用于生成文件路径。
     */
    private fun saveConfigFile(jsonObject: JSONObject, name: String) {
        val file = File("./plugins/${pluginName}/${name}.json")
        if (!file.isFile)
            initConfigFile(name)
        file.writeText(jsonObject.toJSONString())
    }


    /**
     * 初始化配置文件。
     *
     * 此函数用于创建并初始化一个指定名称的配置文件。如果文件不存在，它会创建一个新文件并写入空的JSON对象。
     * 这确保了后续操作可以安全地读取和修改配置文件的内容。
     *
     * @param name 配置文件的名称，不包括后缀.json。
     * @return 返回一个JSONObject，表示已初始化的配置文件内容。
     */
    private fun initConfigFile(name: String): JSONObject {
        val file = File("./plugins/${pluginName}/${name}.json")
        if (!file.isFile) {
            file.parentFile.mkdirs()
            file.createNewFile()
            file.writeText("{}")
        }
        val text = file.readText(Charset.forName("utf-8"))
        return JSONValue.parse(text) as JSONObject
    }

    /**
     * 使用快速排序算法对BigInteger类型的数组进行排序。
     * 选择数组的第一个元素作为基准值，通过不断地交换元素，将数组分为两部分：左侧的元素都小于等于基准值，右侧的元素都大于等于基准值。
     * 然后对左右两部分分别进行递归排序，最终实现整个数组的排序。
     *
     * @param a 要排序的BigInteger类型数组
     * @param left 排序的起始位置
     * @param right 排序的结束位置
     */
    private fun quickSort(a: ArrayList<BigInteger>, left: Int, right: Int) {
        // 如果起始位置大于结束位置，则说明排序已完成，直接返回
        if (left > right) return
        // 选择数组第一个元素作为基准值
        val pivot = a[left] //定义基准值为数组第一个数
        // 初始化两个指针，分别指向数组的起始和结束位置
        var i = left
        var j = right
        // 当左指针小于右指针时，循环继续
        while (i < j) {
            // 右指针向左移动，直到找到第一个小于等于基准值的元素
            while (pivot <= a[j] && i < j) j--
            // 左指针向右移动，直到找到第一个大于等于基准值的元素
            while (pivot >= a[i] && i < j) i++
            // 如果左指针仍然小于右指针，则交换两个指针指向的元素
            if (i < j) {
                val temp = a[i]
                a[i] = a[j]
                a[j] = temp
            }
        }
        // 将基准值放到正确的位置上
        a[left] = a[i]
        a[i] = pivot
        // 递归地对基准值左侧和右侧的子数组进行排序
        quickSort(a, left, i - 1)
        quickSort(a, i + 1, right)
    }

}