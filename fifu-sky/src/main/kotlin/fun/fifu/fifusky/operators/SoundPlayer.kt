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

import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList

/**
 * 小猫的声音播放器单例
 * @author NekokeCore
 */
object SoundPlayer {
    /**
     * 很多的小猫的声音的列表
     */
    var catList = ArrayList<Sound>()

    init {
        catList.add(Sound.ENTITY_CAT_AMBIENT)
        catList.add(Sound.ENTITY_CAT_BEG_FOR_FOOD)
        catList.add(Sound.ENTITY_CAT_DEATH)
        catList.add(Sound.ENTITY_CAT_EAT)
        catList.add(Sound.ENTITY_CAT_HISS)
        catList.add(Sound.ENTITY_CAT_HURT)
        catList.add(Sound.ENTITY_CAT_PURR)
        catList.add(Sound.ENTITY_CAT_AMBIENT)
        catList.add(Sound.ENTITY_CAT_PURREOW)
        catList.add(Sound.ENTITY_CAT_STRAY_AMBIENT)
    }

    /**
     * 播放小猫的声音
     * @param player 播放给某玩家
     */
    fun playCat(player: Player) {
        player.playSound(player.location, extact(catList), 10f, 10f)
    }

    /**
     * 确定播放小猫的声音
     * @param arrayList 声音列表
     */
    private fun extact(arrayList: ArrayList<Sound>): Sound {
        val r = Random(1)
        val temp: Int = r.nextInt(arrayList.size)
        return arrayList[temp]
    }
}