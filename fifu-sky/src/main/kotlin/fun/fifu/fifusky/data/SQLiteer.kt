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

import `fun`.fifu.fifusky.Island
import `fun`.fifu.fifusky.Sky
import cn.hutool.db.Db
import cn.hutool.db.DbUtil
import cn.hutool.db.Entity
import cn.hutool.json.JSONArray
import cn.hutool.json.JSONUtil
import java.io.File
import java.util.*

/**
 * 用来管理数据库的单例
 * @author NekokeCore
 */
object SQLiteer {
    private const val ChunkData = "ChunkData"
    private const val PlayerData = "PlayerData"
    private const val SkyIsland = "SkyIsland"

    init {
        DbUtil.setDbSettingPathGlobal(File("plugins/FiFuSky/db.setting").absolutePath)
    }


    /**
     * 获取指定的岛屿的岛屿信息
     * @param island 岛屿
     * @return 岛屿信息
     */
    fun getIslandData(island: Island): IslandData {
        val temp = Db.use().findAll(Entity.create(SkyIsland).set("SkyLoc", island.toString()))
        return if (temp.isNotEmpty()) {
            val ownerPlayersData = getPlayersData(temp[0].getStr("OwnersList").toString())
            val memberPlayersData = getPlayersData(temp[0].getStr("MembersList").toString())
            val privilegeData = PrivilegeData(island, ownerPlayersData, memberPlayersData)
            IslandData(island, privilegeData)
        } else {
            IslandData(island, PrivilegeData(island, ArrayList(), ArrayList()))
        }

    }

    /**
     * 把玩家主岛存入数据库
     * @param playerUUID 玩家UUID
     * @param playerIsland 玩家主岛
     */
    fun savePlayerIndex(playerUUID: String, playerIsland: String) {
        if (Db.use().findAll(Entity.create(PlayerData).set("UUID", playerUUID)).isEmpty()) {
            Db.use().insert(
                Entity.create(PlayerData)
                    .set("UUID", playerUUID)
                    .set("IndexSkyLoc", playerIsland)
            )
        } else {
            Db.use().update(
                Entity.create().set("IndexSkyLoc", playerIsland),
                Entity.create(PlayerData).set("UUID", playerUUID)
            )
        }

    }


    /**
     * 从数据文件获取玩家的主岛
     * @param playerUUID 玩家UUID
     * @return 玩家主岛
     */
    fun getPlayerIndex(playerUUID: String): Island {
        val temp = Db.use().findAll(Entity.create(PlayerData).set("UUID", playerUUID))
        if (temp.isNotEmpty()) {
            return Sky.getIsland(temp[0].getStr("IndexSkyLoc"))
        } else {
            throw RuntimeException("查询玩家的主岛失败  --  $playerUUID")
        }

    }


    /**
     * 把玩家的ip存入数据库
     * @param playerUUID 玩家UUID
     * @param playerIP 玩家IP
     */
    fun savePlayerIp(playerUUID: String, playerIP: String) {
        if (Db.use().findAll(Entity.create(PlayerData).set("UUID", playerUUID)).isEmpty()) {
            val jsArr = JSONUtil.createArray()
            jsArr.add(playerIP)
            Db.use().insert(
                Entity.create(PlayerData)
                    .set("UUID", playerUUID)
                    .set("IPList", jsArr)
            )
        } else {
            val playerIps = getPlayerIp(playerUUID)
            if (playerIP !in playerIps)
                playerIps.add(playerIP)
            Db.use().update(
                Entity.create().set("IPList", playerIps),
                Entity.create(PlayerData).set("UUID", playerUUID)
            )
        }

    }


    /**
     * 从数据库获取玩家IP列表
     * @param playerUUID 玩家UUID
     * @return 玩家IP列表
     */
    private fun getPlayerIp(playerUUID: String): JSONArray {
        val temp = Db.use().findAll(Entity.create(PlayerData).set("UUID", playerUUID))
        if (temp.isNotEmpty()) {
            val jsArrStr = temp[0].getStr("IPList")
            val jsArr = JSONUtil.parseArray(jsArrStr)
            if (jsArr.isNotEmpty())
                return jsArr
        }
        return JSONUtil.createArray()
    }

    /**
     * 把存放玩家信息的JSONArray字符串换成玩家信息列表
     * @param jsArrStr 把存放玩家信息的JSONArray字符串
     * @return 指定岛屿文件的JSONObject的指定类别的玩家信息列表
     */
    private fun getPlayersData(jsArrStr: String): ArrayList<PlayerData> {
        val contentsPlayersData = ArrayList<PlayerData>()
        val playerUuids = JSONUtil.parseArray(jsArrStr)
        playerUuids.forEach {
            if (it is String) {
                contentsPlayersData.add(PlayerData(it, getPlayerName(it)))
            }
        }
        return contentsPlayersData
    }

    /**
     * 根据UUID返回玩家名，若查询不到则返回玩家UUID
     * @param playerUUID 玩家UUID
     * @return 玩家名
     */
    fun getPlayerName(playerUUID: String): String {
        val temp = Db.use().findAll(Entity.create(PlayerData).set("UUID", playerUUID))
        return if (temp.isNotEmpty()) {
            temp[0].getStr("Name")
        } else {
            playerUUID
        }
    }

    /**
     * 根据玩家名返回玩家UUID，若查询不到则返回玩家名
     * @param playerName 玩家名
     * @return 玩家UUID
     */
    fun getPlayerUuid(playerName: String): String {
        val temp = Db.use().findAll(Entity.create(PlayerData).set("Name", playerName))
        return if (temp.isNotEmpty()) {
            temp[0].getStr("UUID")
        } else {
            playerName
        }
    }

    /**
     * 存储玩家的UUID-Name数据
     * @param playerUUID 玩家UUID
     * @param playerName 玩家名
     */
    fun savePlayerName(playerUUID: String, playerName: String) {
        if (Db.use().findAll(Entity.create(PlayerData).set("UUID", playerUUID)).isEmpty()) {
            Db.use().insert(
                Entity.create(PlayerData)
                    .set("UUID", playerUUID)
                    .set("Name", playerName)
            )
        } else {
            Db.use().update(
                Entity.create().set("Name", playerName),
                Entity.create(PlayerData).set("UUID", playerUUID)
            )
        }
    }

    /**
     * 把IslandData保存到数据库
     * @param islandData 岛屿数据
     */
    fun saveIslandData(islandData: IslandData) {
        val skyLoc = islandData.Island.toString()

        val membersList = JSONUtil.createArray()
        val ownersList = JSONUtil.createArray()
        islandData.Privilege.Owner.forEach {
            ownersList.add(it.UUID)
            savePlayerName(it.UUID, it.LastName)
        }
        islandData.Privilege.Member.forEach {
            membersList.add(it.UUID)
            savePlayerName(it.UUID, it.LastName)
        }

        if (Db.use().findAll(Entity.create(SkyIsland).set("SkyLoc", skyLoc)).isEmpty()) {
            Db.use().insert(
                Entity.create(SkyIsland)
                    .set("SkyLoc", skyLoc)
                    .set("OwnersList", ownersList.toString())
                    .set("MembersList", membersList.toString())
            )
        } else {
            Db.use().update(
                Entity.create()
                    .set("SkyLoc", skyLoc)
                    .set("OwnersList", ownersList.toString())
                    .set("MembersList", membersList.toString()),
                Entity.create(SkyIsland).set("SkyLoc", skyLoc)
            )
        }
    }

    /**
     * 获取区块数据
     * @param chunkLoc 区块坐标
     * @return 区块数据
     */
    fun getChunkData(chunkLoc: String): ChunkData {
        val temp = Db.use().findAll(Entity.create(ChunkData).set("Chunk", chunkLoc))
        return if (temp.isNotEmpty()) {
            ChunkData(chunkLoc, temp[0].getBool("AllowExplosion"))
        } else {
            ChunkData(chunkLoc, false)
        }
    }

    /**
     * 保存区块信息
     * @param chunkData 区块信息
     */
    fun saveChunkData(chunkData: ChunkData) {
        if (Db.use().findAll(Entity.create(ChunkData).set("Chunk", chunkData.Chunk)).isEmpty()) {
            Db.use().insert(
                Entity.create(ChunkData)
                    .set("Chunk", chunkData.Chunk)
                    .set("AllowExplosion", chunkData.AllowExplosion)
            )
        } else {
            Db.use().update(
                Entity.create()
                    .set("Chunk", chunkData.Chunk)
                    .set("AllowExplosion", chunkData.AllowExplosion),
                Entity.create(ChunkData).set("Chunk", chunkData.Chunk)
            )
        }
    }

    /**
     * 获取玩家的岛屿列表
     * @param uuid 玩家的UUID
     * @return 第一个：玩家所拥有的岛屿    第二个：玩家所加入的岛屿
     */
    fun getHomes(uuid: String): Pair<ArrayList<IslandData>, ArrayList<IslandData>> {
        val forOwner = ArrayList<IslandData>()
        val forMember = ArrayList<IslandData>()
        val temp = Db.use().findAll(SkyIsland)
        if (temp.isNotEmpty()) {
            temp.forEach { entity ->
                val islandData = getIslandData(Sky.getIsland(entity.getStr("SkyLoc")))
                islandData.Privilege.Owner.forEach { if (uuid == it.UUID) forOwner.add(islandData) }
                islandData.Privilege.Member.forEach { if (uuid == it.UUID) forMember.add(islandData) }
            }
        }
        return Pair(forOwner, forMember)
    }

    /**
     * 获得数据库中所有的岛屿
     * @return 所有的岛屿（可变列表）
     */
    fun getAllSkyLoc(): MutableList<Island> {
        val temp = Db.use().findAll(Entity.create(SkyIsland))
        val r = mutableListOf<Island>()
        temp.forEach {
            r.add(Sky.getIsland(it.getStr("SkyLoc")))
        }
        return r
    }
}