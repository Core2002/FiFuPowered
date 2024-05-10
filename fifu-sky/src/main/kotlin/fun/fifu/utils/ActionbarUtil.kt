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

package `fun`.fifu.utils

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.lang.reflect.Constructor

object ActionbarUtil {
    private val NMS_VERSION = Bukkit.getServer().javaClass.getPackage().name.substring(
        Bukkit.getServer().javaClass.getPackage().name.lastIndexOf(".") + 1
    )
    private var craftPlayerClass: Class<*>? = null
    private var ppoc: Constructor<*>? = null
    private var packet: Class<*>? = null
    private var chat: Class<*>? = null
    private var chatBaseComponent: Class<*>? = null

    init {
        if (NMS_VERSION.startsWith("v1_8_R") || NMS_VERSION.startsWith("v1_9_R")) {
            craftPlayerClass = Class.forName("org.bukkit.craftbukkit.$NMS_VERSION.entity.CraftPlayer")
            chatBaseComponent = Class.forName("net.minecraft.server.$NMS_VERSION.IChatBaseComponent")
            ppoc = Class.forName("net.minecraft.server.$NMS_VERSION.PacketPlayOutChat").getConstructor(
                chatBaseComponent, java.lang.Byte.TYPE
            )
            packet = Class.forName("net.minecraft.server.$NMS_VERSION.Packet")
            chat = Class.forName(
                "net.minecraft.server.$NMS_VERSION" + if (NMS_VERSION.equals(
                        "v1_8_R1",
                        ignoreCase = true
                    )
                ) ".ChatSerializer" else ".ChatComponentText"
            )
        }
    }

    /**
     * 发送操作栏信息
     * @param player 要显示信息的玩家
     * @param message 信息内容
     */
    fun sendMessage(player: Player, message: String) {
        if (!NMS_VERSION.startsWith("v1_9_R") && !NMS_VERSION.startsWith("v1_8_R")) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(message))
            return
        }

        val craftPlayer = craftPlayerClass!!.cast(player)

        val messageObj = if (NMS_VERSION.equals("v1_8_R1", ignoreCase = true)) chatBaseComponent!!.cast(
            chat!!.getDeclaredMethod("a", String::class.java).invoke(chat, "{'text': '$message'}")
        ) else chat!!.getConstructor(
            String::class.java
        ).newInstance(message)

        val packetPlayOutChat = ppoc!!.newInstance(messageObj, 2.toByte())
        val iCraftPlayer = craftPlayerClass!!.getDeclaredMethod("getHandle").invoke(craftPlayer)

        val playerConnection = iCraftPlayer.javaClass.getDeclaredField("playerConnection")[iCraftPlayer]
        val sendPacket = playerConnection.javaClass.getDeclaredMethod("sendPacket", packet)

        sendPacket.invoke(playerConnection, packetPlayOutChat)
    }

}