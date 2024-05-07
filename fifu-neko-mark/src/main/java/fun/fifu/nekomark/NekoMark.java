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
                player.sendMessage("已刻上了" + args[1] + "的印记:" + ownerUUID);
                return true;
            }
            return true;
        }
        return false;
    }

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

    public static void makeMark(List<String> lore, String uuid) {
        for (String l : lore) {
            if (!l.contains(uuid)) continue;
            return;
        }
        lore.add("小白印记:" + uuid);
    }

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

    public void onEnable() {
        this.getLogger().info("小白印记插件已启动");
        this.getServer().getPluginCommand("neko-mark").setExecutor(this);
        this.getServer().getPluginManager().registerEvents(this, this);
    }

}
