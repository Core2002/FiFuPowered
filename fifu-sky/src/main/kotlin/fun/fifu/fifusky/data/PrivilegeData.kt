package `fun`.fifu.fifusky.data

import `fun`.fifu.fifusky.Island
import java.util.*

/**
 * 代表权限信息
 * @author NekokeCore
 */
data class PrivilegeData(
    val island: Island,
    var Owner: ArrayList<PlayerData>,
    var Member: ArrayList<PlayerData>
)
