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
 * x代表x坐标起始位置
 * xx代表x坐标终止位置
 * 同理
 * y代表y坐标起始位置
 * yy代表y坐标终止位置
 *
 * r代表x，y相等，以此类推
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
