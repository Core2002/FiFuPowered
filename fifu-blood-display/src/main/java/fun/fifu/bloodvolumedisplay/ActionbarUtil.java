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

package fun.fifu.bloodvolumedisplay;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ActionbarUtil {
    private static final String NMS_VERSION = Bukkit.getServer().getClass().getPackage().getName().substring(Bukkit.getServer().getClass().getPackage().getName().lastIndexOf(".") + 1);
    private static Class<?> craftPlayerClass;
    private static Constructor<?> ppoc;
    private static Class<?> packet;
    private static Class<?> chat;
    private static Class<?> chatBaseComponent;

    /**
     * 发送消息到玩家的游戏聊天栏。
     *
     * @param player  需要接收消息的玩家对象。
     * @param message 需要发送的消息内容。
     *                此方法支持向兼容 Minecraft 1.8 和 1.9 及以上的玩家发送消息。
     *                对于不同版本的 Minecraft，消息发送的方式有所不同，因此需要通过版本检查来选择合适的方法。
     *                对于 1.9 及以上版本，使用 Spigot API 的 sendMessage 方法发送消息到操作栏。
     *                对于 1.8 及以下版本，需要通过反射调用原版 Minecraft 的 NMS 发送消息。
     */
    public static void sendMessage(Player player, String message) {
        // 检查玩家对象和消息内容是否为 null，如果是则直接返回。
        if (player == null || message == null) {
            return;
        }
        // 如果版本不是 1.9_R1 或 1.8_R1，则使用 Spigot API 发送消息到操作栏。
        if (!NMS_VERSION.startsWith("v1_9_R") && !NMS_VERSION.startsWith("v1_8_R")) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
            return;
        }
        try {
            // 将 Bukkit 的 Player 对象转换为 CraftPlayer 对象，以便访问 NMS 方法。
            Object craftPlayer = craftPlayerClass.cast(player);
            // 根据 Minecraft 版本创建一个 ChatComponent 对象，该对象包含消息内容。
            Object object = NMS_VERSION.equalsIgnoreCase("v1_8_R1") ? chatBaseComponent.cast(chat.getDeclaredMethod("a", String.class).invoke(chat, "{'text': '" + message + "'}")) : chat.getConstructor(String.class).newInstance(message);
            // 创建一个 Packet 对象，用于发送消息到玩家。
            Object packetPlayOutChat = ppoc.newInstance(object, (byte) 2);
            // 获取 CraftPlayer 对象的 handle 字段，该字段是一个原版 Minecraft 的 Player 对象。
            Method handle = craftPlayerClass.getDeclaredMethod("getHandle");
            Object iCraftPlayer = handle.invoke(craftPlayer);
            // 获取 PlayerConnection 对象，该对象用于发送 Packet 到玩家。
            Field playerConnectionField = iCraftPlayer.getClass().getDeclaredField("playerConnection");
            Object playerConnection = playerConnectionField.get(iCraftPlayer);
            // 调用 sendPacket 方法，将消息发送到玩家。
            Method sendPacket = playerConnection.getClass().getDeclaredMethod("sendPacket", packet);
            sendPacket.invoke(playerConnection, packetPlayOutChat);
        } catch (Exception ex) {
            // 如果在发送消息的过程中发生异常，则打印异常堆栈跟踪。
            ex.printStackTrace();
        }
    }

    static {
        try {
            if (NMS_VERSION.startsWith("v1_8_R") || NMS_VERSION.startsWith("v1_9_R")) {
                craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + NMS_VERSION + ".entity.CraftPlayer");
                chatBaseComponent = Class.forName("net.minecraft.server." + NMS_VERSION + ".IChatBaseComponent");
                ppoc = Class.forName("net.minecraft.server." + NMS_VERSION + ".PacketPlayOutChat").getConstructor(chatBaseComponent, Byte.TYPE);
                packet = Class.forName("net.minecraft.server." + NMS_VERSION + ".Packet");
                chat = Class.forName("net.minecraft.server." + NMS_VERSION + (NMS_VERSION.equalsIgnoreCase("v1_8_R1") ? ".ChatSerializer" : ".ChatComponentText"));
            }
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
