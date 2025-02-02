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

package `fun`.fifu.fifusky.operators

import cn.hutool.core.date.DateUtil
import `fun`.fifu.fifusky.FiFuSky
import `fun`.fifu.fifusky.Island
import `fun`.fifu.fifusky.Sky
import `fun`.fifu.fifusky.data.IslandData
import `fun`.fifu.fifusky.data.Jsoner
import `fun`.fifu.fifusky.data.PlayerData
import `fun`.fifu.fifusky.data.SQLiteer
import `fun`.fifu.utils.ActionbarUtil
import org.bukkit.*
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * 岛屿操作者单例，内聚了很多操作以及方法扩展
 * @author NekokeCore
 */
object SkyOperator {
    val Spawn = Location(
        Bukkit.getWorld(Sky.WORLD),
        359.0,
        109.0,
        295.0,
        180f,
        0f
    )

    private const val SpawnProtectionRadius = 3

    /**
     * 把玩家传送至某岛屿
     * @param island 岛屿
     */
    fun Player.tpIsland(island: Island) {
        val islandCenter = Sky.getIslandCenter(island)
        teleport(
            Location(
                Bukkit.getWorld(Sky.WORLD),
                islandCenter.first.toDouble(),
                65.0,
                islandCenter.second.toDouble(),
                location.yaw,
                location.pitch
            )
        )
        sendTitle(island.toString(), "主人 ${island.getOwnersList()}", 2, 20 * 3, 6)
        SoundPlayer.playCat(this)

        val bedrock = Location(
            Bukkit.getWorld(Sky.WORLD),
            islandCenter.first.toDouble(),
            60.0,
            islandCenter.second.toDouble()
        ).block
        if (bedrock.type == Material.AIR) bedrock.type = Material.BEDROCK
    }

    /**
     * 构建一个岛屿，将模板岛屿复制到目标岛屿
     */
    fun Island.build() {
        val startTime = System.currentTimeMillis()
        val ic = Sky.getIslandCenter(this)
        val world = Bukkit.getWorld(Sky.WORLD) ?: return

        // 偏移量
        val (xx, yy, zz) = Triple(-3, -4, -1)

        // 顶点坐标
        val (x1, y1, z1) = Triple(508, 60, 510)
        val (x2, y2, z2) = Triple(515, 69, 516)

        // 目标原点
        val (xxx, yyy, zzz) = Triple(
            X + Sky.SIDE / 2 + xx.toDouble(),
            64 + yy.toDouble(),
            Y + Sky.SIDE / 2 + zz.toDouble()
        )

        // 生成执行命令
        val command = "clone $x1 $y1 $z1 $x2 $y2 $z2 ${xxx.toInt()} ${yyy.toInt()} ${zzz.toInt()}"

        // 自动加载区块
        loadChunks(world, xxx, yyy, zzz, 16)

        // 开始拷贝初始空岛
        Bukkit.getScheduler().runTask(FiFuSky.fs, Runnable {
            FiFuSky.fs.logger.info("开始拷贝初始空岛: $command")
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command)
            world.getBlockAt(ic.first, 60, ic.second).setType(Material.BEDROCK, true)
            FiFuSky.fs.logger.info("复制完毕！耗时 ${System.currentTimeMillis() - startTime} ms。")
        })

        FiFuSky.fs.logger.info("调度完毕！")
    }

    /**
     * 加载区块
     *
     * @param world 世界对象，用于确定区块所属的世界
     * @param x X坐标，用于确定初始位置
     * @param y Y坐标，用于确定初始位置
     * @param z Z坐标，用于确定初始位置
     * @param chunkSize 区块大小，用于确定加载区块的范围
     */
    private fun loadChunks(world: World, x: Double, y: Double, z: Double, chunkSize: Int) {
        val locations = listOf(
            Location(world, x, y, z),
            Location(world, x + chunkSize, y, z),
            Location(world, x - chunkSize, y, z),
            Location(world, x, y, z + chunkSize),
            Location(world, x, y, z - chunkSize),
            Location(world, x + chunkSize, y, z + chunkSize),
            Location(world, x - chunkSize, y, z - chunkSize),
            Location(world, x + chunkSize, y, z + chunkSize),
            Location(world, x - chunkSize, y, z - chunkSize),
            Location(world, 511.0, 64.0, 511.0),
            Location(world, 511.0 + chunkSize, 64.0, 511.0),
            Location(world, 511.0 - chunkSize, 64.0, 511.0),
            Location(world, 511.0, 64.0, 511.0 + chunkSize),
            Location(world, 511.0, 64.0, 511.0 - chunkSize),
            Location(world, 511.0 + chunkSize, 64.0, 511.0 + chunkSize),
            Location(world, 511.0 - chunkSize, 64.0, 511.0 - chunkSize),
            Location(world, 511.0 + chunkSize, 64.0, 511.0 + chunkSize),
            Location(world, 511.0 - chunkSize, 64.0, 511.0 - chunkSize)
        )

        locations.forEach { location ->
            val chunk = world.getChunkAt(location)
            FiFuSky.fs.logger.info("chunk.load: ${chunk.load(true)}")
        }
    }

    /**
     * 遍历一个三维空间
     * @param sj 空间上界顶点
     * @param xj 空间下界顶点
     * @return 该空间内的点集列表
     */
    fun blp(sj: Triple<Int, Int, Int>, xj: Triple<Int, Int, Int>): ArrayList<Triple<Int, Int, Int>> {
        fun bl(f: Int, c: Int) = if (f <= c) f..c else f downTo c
        val arr = ArrayList<Triple<Int, Int, Int>>()
        for (xx in bl(sj.first, xj.first))
            for (yy in bl(sj.second, xj.second))
                for (zz in bl(sj.third, xj.third))
                    arr.add(Triple(xx, yy, zz))
        return arr
    }

    /**
     * 判断玩家当前是否有权限
     * @return 是否有权限
     */
    fun Player.havePermission(): Boolean {
        if (!location.world.isSkyWorld())
            return true
        if (gameMode == GameMode.SPECTATOR)
            return true
        val island = location.getIsland()
        if (isOwnedIsland(island) || isJoinedIsland(island))
            return true

        return false
    }

    /**
     * 查询区块是否允许爆炸
     * @return 该区块是否允许爆炸
     */
    fun canExplosion(chunckLoc: String): Boolean = SQLiteer.getChunkData(chunckLoc).AllowExplosion


    /**
     * 判断世界是否是空岛世界
     * @return 世界是否是空岛世界
     */
    fun World.isSkyWorld() = name == Sky.WORLD


    /**
     * 检查岛屿是否是无人认领的
     * @return 是否无人认领
     */
    fun Island.isUnclaimed(): Boolean {
        val privilege = SQLiteer.getIslandData(this).Privilege
        if (privilege.Owner.isEmpty())
            return true
        return false
    }

    /**
     * 获取岛屿的主人列表
     * @return 目标岛屿的主人列表
     */
    fun Island.getOwnersList(u: Boolean = false): String {
        val sb = StringBuilder()
        val owner = SQLiteer.getIslandData(this).Privilege.Owner
        owner.forEach {
            if (u) {
                sb.append(it.UUID).append(' ')
            } else {
                sb.append(it.LastName).append(' ')
            }
        }
        return sb.toString()
    }

    /**
     * 获取岛屿的成员列表
     * @return 目标岛屿的成员列表
     */
    fun Island.getMembersList(u: Boolean = false): String {
        val sb = StringBuilder()
        val owner = SQLiteer.getIslandData(this).Privilege.Member
        owner.forEach {
            if (u) {
                sb.append(it.UUID).append(' ')
            } else {
                sb.append(it.LastName).append(' ')
            }
        }
        return sb.toString()
    }

    /**
     * 获取岛屿的信息
     * @return IslandData
     */
    fun Island.getIslandData() = SQLiteer.getIslandData(this)

    /**
     * 获取坐标所在的岛屿
     * @return 坐标所在的岛屿
     */
    fun Location.getIsland() = Sky.getIsland(blockX, blockZ)

    /**
     * 给目标岛屿添加一位主人
     * @param player 要添加的主人
     */
    fun Island.addOwner(player: Player) {
        val uuid = player.uniqueId.toString()
        val islandData = SQLiteer.getIslandData(this)
        if (player.isOwnedIsland(islandData.Island))
            return
        islandData.Privilege.Owner.add(PlayerData(UUID = uuid, LastName = player.name))
        SQLiteer.saveIslandData(islandData)
    }

    /**
     * 给目标岛屿添加一位成员
     * @param player 要添加的成员
     */
    fun Island.addMember(player: Player) {
        val uuid = player.uniqueId.toString()
        val islandData = SQLiteer.getIslandData(this)
        if (player.isJoinedIsland(islandData.Island))
            return
        islandData.Privilege.Member.add(PlayerData(UUID = uuid, LastName = player.name))
        SQLiteer.saveIslandData(islandData)
    }

    /**
     * 从目标岛屿删除一位成员
     * @param player 要移除的成员
     */
    fun Island.removeMember(player: Player) {
        val uuid = player.uniqueId.toString()
        val islandData = SQLiteer.getIslandData(this)
        if (!player.isJoinedIsland(islandData.Island))
            return
        islandData.Privilege.Member.remove(PlayerData(UUID = uuid, LastName = player.name))
        SQLiteer.saveIslandData(islandData)
    }

    /**
     * 判断一个玩家是否可以领取岛
     * @return 第一个：是否可以领取岛，第二个：什么时间后可以领取
     */
    fun Player.canGetIsland(): Pair<Boolean, String> {
        val uuid = uniqueId.toString()
        val lastGetTime = Jsoner.getPlayerLastGet(uuid)
        val currentTime = System.currentTimeMillis()
        val timeSinceLastGet = currentTime - lastGetTime

        // 将 30 天转换为毫秒
        val thirtyDaysInMillis = TimeUnit.DAYS.toMillis(30)

        // 计算剩余时间（毫秒）
        val remainingTimeInMillis = thirtyDaysInMillis - timeSinceLastGet

        // 判断是否可以领取岛
        val canGetIsland = timeSinceLastGet >= thirtyDaysInMillis

        // 格式化剩余时间
        val remainingTimeFormatted = if (canGetIsland) {
            "现在可以领取"
        } else {
            DateUtil.formatBetween(remainingTimeInMillis)
        }

//        println("UUID: $uuid, Time since last get: $timeSinceLastGet ms, 30 days in milliseconds: $thirtyDaysInMillis ms, Remaining time: $remainingTimeFormatted")

        return Pair(canGetIsland, remainingTimeFormatted)
    }

    /**
     * 当玩家get岛屿成功后执行该方法
     * @param player 领取完岛屿的玩家
     */
    fun playerGetOver(player: Player, islandData: IslandData) {
        SQLiteer.saveIslandData(islandData)
        SQLiteer.savePlayerIndex(player.uniqueId.toString(), islandData.Island.toString())
        Jsoner.setPlayerLastGet(player.uniqueId.toString(), System.currentTimeMillis())
    }

    /**
     * 获取玩家的岛屿列表
     * @return 第一个：玩家所拥有的岛屿    第二个：玩家所加入的岛屿
     */
    fun Player.getIslandHomes(): Pair<String, String> {
        val sb = StringBuilder()
        val homes = SQLiteer.getHomes(uniqueId.toString())
        homes.first.forEach {
            sb.append(it.Island).append(' ')
        }
        val forOwner: String = sb.toString()
        sb.clear()
        homes.second.forEach {
            sb.append(it.Island).append(' ')
        }
        val forMember: String = sb.toString()
        sb.clear()
        return Pair(forOwner, forMember)
    }

    /**
     * 把玩家主人从岛屿移除
     * @param player 玩家主人
     */
    fun Island.removeOwner(player: Player) {
        val uuid = player.uniqueId.toString()
        val islandData = SQLiteer.getIslandData(this)
        if (!player.isOwnedIsland(islandData.Island))
            return
        val owners = islandData.Privilege.Owner
        owners.remove(PlayerData(uuid, SQLiteer.getPlayerName(uuid)))
        SQLiteer.saveIslandData(islandData)
    }

    /**
     * 判断玩家是否拥有岛屿
     * @param island 要检测的岛屿
     * @return 玩家是否是岛屿的所有者
     */
    fun Player.isOwnedIsland(island: Island): Boolean {
        val islandData = SQLiteer.getIslandData(island)
        islandData.Privilege.Owner.forEach {
            if (uniqueId.toString() == it.UUID) return true
        }
        return false
    }

    /**
     * 判断玩家是否是岛屿成员
     * @param island 要检测的岛屿
     * @return 玩家是否是岛屿的成员
     */
    fun Player.isJoinedIsland(island: Island): Boolean {
        val islandData = SQLiteer.getIslandData(island)
        islandData.Privilege.Member.forEach {
            if (uniqueId.toString() == it.UUID) return true
        }
        return false
    }

    /**
     * 获取实体当前所在的岛屿
     * @return 当前实体所在的岛屿
     */
    fun Entity.currentIsland(): Island = Sky.getIsland(location.blockX, location.blockZ)

    /**
     * 获取区块的允许爆炸属性
     * @return 是否允许爆炸
     */
    fun Chunk.getAllowExplosion() = SQLiteer.getChunkData(toChunkLoc()).AllowExplosion

    /**
     * 更改区块的允许爆炸属性
     * @param switch 是否允许爆炸
     */
    fun Chunk.setAllowExplosion(switch: Boolean) {
        val chunkData = SQLiteer.getChunkData(toChunkLoc())
        chunkData.AllowExplosion = switch
        SQLiteer.saveChunkData(chunkData)
    }

    /**
     * 将该区块转换成ChunkLoc
     * @return 该区块的ChunkLoc
     */
    fun Chunk.toChunkLoc() = "[${x},${z}]"


    /**
     * 判断坐标是否在主城
     * @return 坐标是否在主城
     */
    fun Location.inSpawn(): Boolean {
        if (!world.isSkyWorld()) return false
        if (Sky.isInIsland(blockX, blockZ, Sky.SPAWN)) return true
        return false
    }

    /**
     * 判断玩家是否为FiFuAdmin
     * @return true：是    false：不是
     */
    fun Player.isFiFuAdmin() = Jsoner.judUUIDisFiFuAdmin(uniqueId.toString())

    /**
     * 判断实体是否在保护区内
     */
    fun Entity.inProtectionRadius(): Boolean {
        if (!location.world.isSkyWorld()) return false
        return location.distance(Spawn) <= SpawnProtectionRadius
    }

    /**
     * 发送操作栏信息
     * @param message 信息内容
     */
    fun Player.sendActionbarMessage(message: String) {
        ActionbarUtil.sendMessage(this, message)
    }

}