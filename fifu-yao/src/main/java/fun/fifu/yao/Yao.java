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

package fun.fifu.yao;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Yao extends JavaPlugin implements Listener {
    /**
     * 当插件启用时调用此方法。
     * 此方法用于初始化插件，注册事件监听器，并输出启动信息到日志。
     * 调用超类的onEnable方法以确保插件的正确启用。
     */
    @Override
    public void onEnable() {
        // 注册当前类为事件监听器，以便处理服务器中的事件。
        getServer().getPluginManager().registerEvents(this, this);
        
        // 输出插件启动信息到服务器日志，方便管理员确认插件已成功启动。
        getServer().getLogger().info("小瑶瑶插件已启动,author: NekokeCore");
        
        // 调用父类的onEnable方法，完成插件启用的必要步骤。
        super.onEnable();
    }


    /**
     * 处理玩家与实体交互的事件。
     * 当玩家不在载具中且手持箭矢时，如果玩家与点击的实体距离小于特定值，
     * 玩家将骑上该实体，并发送一条消息。此事件用于实现特定的游戏逻辑。
     *
     * @param event 交互事件的详细信息，包括玩家、点击的实体等。
     */
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        // 获取交互事件中的玩家和被点击的实体
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        // 检查玩家是否在载具中且手持的是箭矢
        if (!player.isInsideVehicle() && player.getInventory().getItemInMainHand().getType() == Material.ARROW) {
            // 定义允许玩家骑上实体的最大距离
            double range = 3;
            // 如果玩家与实体的距离超过最大距离，则取消事件处理
            if (player.getLocation().distance(entity.getLocation()) > range)
                return;

            // 将玩家添加为实体的乘客
            entity.addPassenger(player);
            // 发送消息通知玩家骑上了实体
            player.sendMessage(player.getName() + "变成了小瑶瑶");
            // 取消原事件，防止默认行为发生（如打开交互菜单等）
            event.setCancelled(true);
        }
    }


    /**
     * 处理玩家退出游戏事件。
     * 当玩家决定离开游戏时，此事件会被触发。此方法的目的是在玩家退出游戏前，
     * 确保他们离开任何载具，以避免在游戏重新加入时出现错误的载具状态。
     *
     * @param event 代表玩家退出游戏的事件对象。此对象提供了关于事件的详细信息，
     *              如退出游戏的玩家等。
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 强制玩家离开当前载具。
        event.getPlayer().leaveVehicle();
    }

}
