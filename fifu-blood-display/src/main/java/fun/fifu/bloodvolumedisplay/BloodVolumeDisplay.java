package fun.fifu.bloodvolumedisplay;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class BloodVolumeDisplay extends JavaPlugin implements Listener {
    public static BloodVolumeDisplay plugin;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new EntityListener(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("感谢使用本插件");
    }

}
