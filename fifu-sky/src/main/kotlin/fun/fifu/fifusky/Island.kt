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
