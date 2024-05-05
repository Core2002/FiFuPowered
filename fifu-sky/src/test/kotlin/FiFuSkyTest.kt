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
        }
    }
}