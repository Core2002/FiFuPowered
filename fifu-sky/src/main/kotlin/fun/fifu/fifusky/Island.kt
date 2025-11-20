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

/**
 * 代表一个岛屿单元
 *
 * x 代表 x 坐标起始位置
 * xx 代表 x 坐标终止位置
 * 同理
 * y 代表 y 坐标起始位置
 * yy 代表 y 坐标终止位置
 *
 * r 代表 x，y 相等，以此类推
 * @author NekokeCore
 */
data class Island(
    val SkyLoc: Pair<Int, Int>,
    val X: Int,
    val XX: Int,
    val Y: Int,
    val YY: Int
) {
    override fun toString(): String {
        return "(${SkyLoc.first},${SkyLoc.second})"
    }
}
