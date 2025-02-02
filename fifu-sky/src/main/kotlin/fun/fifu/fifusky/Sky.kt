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

package `fun`.fifu.fifusky

import kotlin.math.abs


/**
 * SIDE 单个岛屿边长
 * MAX_INLAND 世界最大岛屿数的单个边长的和
 *
 * Pair<Int, Int>一律为真实坐标
 * Island.SkyLoc一律为岛坐标
 * @author NekokeCore
 */
object Sky {
    const val SIDE = 1024
    const val MAX_ISLAND = 29296
    const val WORLD = "world"
    val SPAWN = getIsland("(0,0)")
    private fun getR(SkyR: Int): Int = SIDE * SkyR
    private fun getRR(SkyR: Int): Int = SIDE * (SkyR + 1) - 1

    /**
     * 获取指定坐标所在的岛屿坐标
     * @param rr 指定的坐标
     * @return 其所在的岛屿坐标
     */
    private fun getSkyR(rr: Int) = if (rr >= 0) rr / SIDE else -((-rr + SIDE - 1) / SIDE)

    /**
     * 把岛坐标字符串转化成坐标元组
     * @param skyLoc 岛坐标字符串
     * @return 岛坐标元组
     */
    private fun getSkyLocPair(skyLoc: String): Pair<Int, Int> {
        if (skyLoc == "null" || '(' !in skyLoc || ',' !in skyLoc || ')' !in skyLoc)
            throw java.lang.RuntimeException("SkyLoc 不合法！  ->  $skyLoc")
        val c = skyLoc.indexOf(',')
        val x = skyLoc.substring(1, c)
        val y = skyLoc.substring(c + 1, skyLoc.indexOf(')'))
        return Pair(x.toInt(), y.toInt())
    }

    /**
     * 使用岛坐标元组获取岛屿对象
     * @param skyLoc 岛坐标元组
     * @return 岛屿对象
     */
    fun getIsland(skyLoc: Pair<Int, Int>): Island {
        if (abs(skyLoc.first) > MAX_ISLAND || abs(skyLoc.second) > MAX_ISLAND)
            throw  java.lang.RuntimeException("SkyLoc 不合法！  ->  $skyLoc")
        return Island(skyLoc, getR(skyLoc.first), getRR(skyLoc.first), getR(skyLoc.second), getRR(skyLoc.second))
    }


    /**
     * 使用岛坐标字符串获取岛屿对象
     * @param skyLoc 岛坐标字符串
     * @return 岛屿对象
     */
    fun getIsland(skyLoc: String) = getIsland(getSkyLocPair(skyLoc))

    /**
     * 使用坐标串获取岛屿对象
     * @param xx x坐标
     * @param zz z坐标
     * @return 岛屿对象
     */
    fun getIsland(xx: Int, zz: Int) = getIsland(Pair(getSkyR(xx), getSkyR(zz)))

    /**
     * 获取岛屿中心坐标元组
     * @param island 岛屿对象
     * @return 岛屿中心坐标元组
     */
    fun getIslandCenter(island: Island) =
        Pair((island.XX - island.X) / 2 + island.X, (island.YY - island.Y) / 2 + island.Y)

    /**
     * 判断坐标元组是否在指定岛屿内
     * @param xx x坐标
     * @param zz z坐标
     * @param island 岛屿对象
     * @return 返回True则真，False则假
     */
    fun isInIsland(xx: Int, zz: Int, island: Island) =
        xx in island.X..island.XX && zz in island.Y..island.YY

    /**
     * 根据当前岛屿位置计算下一个岛屿的位置
     * 该函数通过比较当前位置的横纵坐标来决定下一个岛屿的位置，旨在模拟在天空中移动以寻找岛屿的过程
     *
     * @param skyLoc 当前天空中的位置，使用Pair表示，first为横坐标，second为纵坐标
     * @return 下一个岛屿的位置，同样使用Pair表示
     */
    fun nextIsLand(skyLoc: Pair<Int, Int>): Pair<Int, Int> {
        // 当横坐标大于负的纵坐标时，表示当前位置在模拟的“天空”区域的上半部分
        return if (skyLoc.first > -skyLoc.second) {
            // 如果横坐标小于纵坐标，向右移动；否则向下移动
            // 这里反映了在“天空”区域上半部分移动的逻辑
            if (skyLoc.first < skyLoc.second) Pair(skyLoc.first + 1, skyLoc.second)
            else Pair(skyLoc.first, skyLoc.second - 1)
        } else {
            // 当横坐标不大于纵坐标时，向上移动；否则向左移动
            // 这里反映了在“天空”区域下半部分移动的逻辑
            if (skyLoc.first <= skyLoc.second) Pair(skyLoc.first, skyLoc.second + 1)
            else Pair(skyLoc.first - 1, skyLoc.second)
        }
    }

}
