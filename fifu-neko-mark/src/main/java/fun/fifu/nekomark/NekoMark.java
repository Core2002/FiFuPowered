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

package fun.fifu.nekomark;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class NekoMark extends JavaPlugin implements Listener {
    public static Plugin plugin;

    /**
     * 处理命令执行的函数。
     * 当玩家发送特定的命令时，这个函数将被调用，用于解析并执行相应的操作。
     *
     * @param sender  命令的发送者，可以是玩家或服务器中的其他实体。
     * @param command 被发送的命令对象。
     * @param label   命令的标签，即命令的名称。
     * @param args    命令的参数，用于指定具体的命令动作。
     * @return 命令处理的结果，true 表示命令被成功处理，false 表示处理失败或不适用。
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ("neko-mark".equalsIgnoreCase(command.getName())) {
            Player player = (Player) sender;
            if (sender == null) {
                return true;
            }
            String uuid = player.getUniqueId().toString();
            if (!"3e79580d-cfdb-4b80-999c-99bc2740d194".equals(uuid)) {
                player.sendMessage("这个命令只有小白才可以用哦");
                return true;
            }
            if (args == null || args.length < 2 || args[0].isEmpty() || args[1].isEmpty()) {
                return false;
            }
            if (args[0].equalsIgnoreCase("无法破坏")) {
                PlayerInventory inventory = player.getInventory();
                ItemStack mainHandItemStack = inventory.getItemInMainHand();
                ItemMeta itemMeta = mainHandItemStack.getItemMeta();
                if (itemMeta == null)
                    return true;
                itemMeta.setUnbreakable(true);
                mainHandItemStack.setItemMeta(itemMeta);
                inventory.setItemInMainHand(mainHandItemStack);
                player.sendMessage("已添加无法破坏");
                return true;
            } else if (args[0].equalsIgnoreCase("小白印记")) {
                PlayerInventory inventory = player.getInventory();
                ItemStack mainHandItemStack = inventory.getItemInMainHand();
                ItemMeta itemMeta = mainHandItemStack.getItemMeta();
                if (itemMeta == null)
                    return true;
                //itemMeta.setUnbreakable(true);
                List<String> lore = itemMeta.getLore();
                if (lore == null) {
                    lore = new ArrayList<>();
                }
                Player owner = this.getServer().getPlayer(args[1]);
                String ownerUUID = (Objects.requireNonNull(owner)).getUniqueId().toString();
                NekoMark.makeMark(lore, ownerUUID);
                itemMeta.setLore(lore);
                mainHandItemStack.setItemMeta(itemMeta);
                inventory.setItemInMainHand(mainHandItemStack);
                player.sendMessage("已刻上了" + args[1] + "的印记：" + ownerUUID);
                return true;
            }
            return true;
        }
        return false;
    }

    /**
     * 处理玩家使用物品的事件。
     * 当玩家使用物品时，此事件会被触发。此方法检查玩家所使用的物品是否具有合法的 NekoMark 标记。
     * 如果物品不合法，玩家将被踢出游戏。
     *
     * @param event 此事件包含有关玩家交互的所有信息，如玩家、使用的物品等。
     */
    @EventHandler
    public static void onUse(PlayerInteractEvent event) {
        if (event.getItem() == null) {
            return;
        }
        ItemMeta itemMeta = event.getItem().getItemMeta();
        if (itemMeta == null)
            return;
        List<String> lore = itemMeta.getLore();
        if (lore == null)
            return;
        if (!NekoMark.hasMark(lore, event.getPlayer().getUniqueId().toString())) {
            event.getPlayer().kickPlayer("持有物品不合法");
            event.setCancelled(true);
        }
    }

    /**
     * 为指定的 UUID 在 lore 列表中添加标记。
     * 如果 lore 中已经存在包含该 UUID 的字符串，则不进行任何操作。
     * 如果 lore 中不存在包含该 UUID 的字符串，则将新字符串添加到 lore 列表末尾。
     *
     * @param lore 一个字符串列表，用于存储标记信息。
     * @param uuid 需要添加标记的唯一标识符。
     */
    public static void makeMark(List<String> lore, String uuid) {
        for (String l : lore) {
            if (!l.contains(uuid)) continue;
            return;
        }
        lore.add("小白印记：" + uuid);
    }

    /**
     * 检查 lore 列表中是否包含指定的 uuid 标记。
     *
     * @param lore 一个字符串列表，代表了某种记录或描述。
     * @param uuid 要查找的特定标记，是一个唯一标识符。
     * @return 如果 lore 中存在包含"小白印记"且同时包含指定 uuid 的字符串，则返回 true；否则返回 false。
     */
    public static boolean hasMark(List<String> lore, String uuid) {
        for (String l : lore) {
            if (!l.contains("小白印记")) continue;
            for (String i : lore) {
                if (!i.contains(uuid)) continue;
                return true;
            }
            return false;
        }
        return true;
    }

    public void onLoad() {
        plugin = this;
    }

    public void onDisable() {
        super.onDisable();
    }

    /**
     * 当插件启用时调用此方法。
     * 插件启用时的主要任务包括：
     * 1. 输出启动信息，让管理员知道插件已经成功启动。
     * 2. 注册命令执行器，使得可以通过命令行调用插件的功能。
     * 3. 注册事件监听器，以便插件可以响应服务器中发生的各种事件。
     */
    public void onEnable() {
        this.getLogger().info("小白印记插件已启动");
        this.getServer().getPluginCommand("neko-mark").setExecutor(this);
        this.getServer().getPluginManager().registerEvents(this, this);
    }

}
