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
