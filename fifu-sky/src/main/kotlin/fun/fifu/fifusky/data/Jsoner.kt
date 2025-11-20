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

package `fun`.fifu.fifusky.data
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import cn.hutool.json.JSONUtil

/**
 * Json 造作者单例，负责操作 Json 数据
 * @author NekokeCore
 */
object Jsoner {
    private val cache: ConcurrentHashMap<String, ByteArray> = ConcurrentHashMap()
    private val PlayerLastGet: File = File("plugins/FiFuSky/PlayerLastGet.json")
    private val FiFuAdminList: File = File("plugins/FiFuSky/FiFuAdminList.json")
    private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    init {
        val dir = File("plugins/FiFuSky")
        if (!dir.isDirectory) dir.mkdirs()
        if (!PlayerLastGet.isFile) PlayerLastGet.writeText("{}")
        if (!FiFuAdminList.isFile) FiFuAdminList.writeText("[]")

        // 初始化缓存
        cache["PlayerLastGet"] = PlayerLastGet.readBytes()
        cache["FiFuAdminList"] = FiFuAdminList.readBytes()

        // 定期将缓存写回文件
        executor.scheduleAtFixedRate({
            PlayerLastGet.writeBytes(cache["PlayerLastGet"] ?: "{}".toByteArray())
            FiFuAdminList.writeBytes(cache["FiFuAdminList"] ?: "[]".toByteArray())
        }, 1, 1, TimeUnit.MINUTES) // 每分钟写回一次
    }

    /**
     * 判断玩家是否是 FiFuAdmin
     * @param uuid 玩家的 uuid
     * @return true：是    false：不是
     */
    fun judUUIDisFiFuAdmin(uuid: String): Boolean {
        val arr = JSONUtil.parseArray(String(cache["FiFuAdminList"] ?: "[]".toByteArray()))
        return uuid in arr
    }

    /**
     * 设置玩家为玩家 FiFuAdmin
     * @param uuid 玩家的 uuid
     */
    fun addFiFuAdminUUID(uuid: String) {
        val arr = JSONUtil.parseArray(String(cache["FiFuAdminList"] ?: "[]".toByteArray()))
        if (uuid !in arr) {
            arr.add(uuid)
            cache["FiFuAdminList"] = arr.toJSONString(4).toByteArray()
        }
    }

    /**
     * 获取玩家上次领取岛的时间
     * @param uuid 玩家的 uuid
     * @return 该玩家上次获取岛屿的时间的时间戳
     */
    fun getPlayerLastGet(uuid: String): Long {
        val obj = JSONUtil.parseObj(String(cache["PlayerLastGet"] ?: "{}".toByteArray()))
        return obj[uuid]?.toString()?.toLongOrNull() ?: 0L
    }

    /**
     * 设置玩家上次领取岛的时间的时间戳
     * @param uuid 玩家的 uuid
     * @param time 时间戳
     */
    fun setPlayerLastGet(uuid: String, time: Long) {
        val obj = JSONUtil.parseObj(String(cache["PlayerLastGet"] ?: "{}".toByteArray()))
        obj[uuid] = time
        cache["PlayerLastGet"] = obj.toJSONString(4).toByteArray()
    }
}
