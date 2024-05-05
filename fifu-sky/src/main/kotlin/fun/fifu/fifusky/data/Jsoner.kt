package `fun`.fifu.fifusky.data

import cn.hutool.cache.file.LFUFileCache
import cn.hutool.json.JSONUtil
import java.io.File

/**
 * Json造作者单例，负责操作Json数据
 * @author NekokeCore
 */
object Jsoner {
    var cache: LFUFileCache = LFUFileCache(1000, 50, 8 * 1000L)
    private val PlayerLastGet: File = File("plugins/FiFuSky/PlayerLastGet.json")
    private val FiFuAdminList: File = File("plugins/FiFuSky/FiFuAdminList.json")

    init {
        val dir = File("plugins/FiFuSky")
        if (!dir.isDirectory) dir.mkdirs()
        if (!PlayerLastGet.isFile) PlayerLastGet.writeText("{}")
        if (!FiFuAdminList.isFile) FiFuAdminList.writeText("[]")
    }

    /**
     * 判断玩家是否是FiFuAdmin
     * @param uuid 玩家的uuid
     * @return true：是    false：不是
     */
    fun judUUIDisFiFuAdmin(uuid: String): Boolean {
        val arr = JSONUtil.parseArray(String(cache.getFileBytes(FiFuAdminList)))
        if (uuid in arr) {
            return true
        }
        return false
    }

    /**
     * 设置玩家为玩家FiFuAdmin
     * @param uuid 玩家的uuid
     */
    fun addFiFuAdminUUID(uuid: String) {
        val arr = JSONUtil.parseArray(FiFuAdminList.readText())
        if (uuid !in arr) {
            arr.add(uuid)
        }
        FiFuAdminList.writeText(arr.toJSONString(4))
    }


    /**
     * 获取玩家上次领取岛的时间
     * @param uuid 玩家的uuid
     * @return 该玩家上次获取岛屿的时间的时间戳
     */
    fun getPlayerLastGet(uuid: String): Long {
        val obj = JSONUtil.parseObj(String(cache.getFileBytes(PlayerLastGet)))
        if (uuid in obj.keys) {
            return obj[uuid].toString().toLong()
        }
        return 0L
    }

    /**
     * 设置玩家上次领取岛的时间的时间戳
     * @param uuid 玩家的uuid
     * @param time 时间戳
     */
    fun setPlayerLastGet(uuid: String, time: Long) {
        val obj = JSONUtil.parseObj(PlayerLastGet.readText())
        obj[uuid] = time
        PlayerLastGet.writeText(obj.toJSONString(4))
    }
}