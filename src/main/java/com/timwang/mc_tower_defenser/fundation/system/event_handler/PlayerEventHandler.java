package com.timwang.mc_tower_defenser.fundation.system.event_handler;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.network.NationSyncService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * 玩家相关事件处理器。
 * 当前用于在玩家进入服务器时下发一次国家快照，保证客户端缓存有初始值。
 */
@EventBusSubscriber(modid = MinecraftTowerDefenser.MODID)
public final class PlayerEventHandler {
    private PlayerEventHandler() {
    }

    @SubscribeEvent
    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NationSyncService.syncPlayer(player);
        }
    }
}
