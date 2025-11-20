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

import `fun`.fifu.fifusky.Sky
import org.junit.jupiter.api.Test
import kotlin.random.Random


class FiFuSkyTest {
    @Test
    fun test() {
        println("OK")
    }

    @Test
    fun testCoordinateTransformation() {
        for (x in 1..14514) {
            val rX = Random.nextInt(-Sky.MAX_ISLAND, Sky.MAX_ISLAND)
            val rY = Random.nextInt(-Sky.MAX_ISLAND, Sky.MAX_ISLAND)

            val island = Sky.getIsland(rX, rY)
            for (x1 in 1..14514) {
                val rx = Random.nextInt(island.X, island.XX)
                val ry = Random.nextInt(island.Y, island.YY)
                val island1 = Sky.getIsland(rx, ry)
                if (island != island1)
                    throw RuntimeException("坐标转换模块异常！")
            }
            val area = (island.XX - island.X) * (island.YY - island.Y)
            if (area != 1023 * 1023)
                println("岛屿${island.SkyLoc} 面积不一致：$area")
        }
    }

    @Test
    fun foreachAllIsland() {
        val center = Pair(0, 0)
        var skyLoc = Sky.nextIsLand(center)
        for (i in 0..Sky.MAX_ISLAND * Sky.MAX_ISLAND) {
            skyLoc = Sky.nextIsLand(skyLoc)

            val island = Sky.getIsland(skyLoc)
            val area = (island.XX - island.X) * (island.YY - island.Y)
            if (area != 1023 * 1023)
                println("岛屿${island.SkyLoc} 面积不一致：$area")
        }
    }
}