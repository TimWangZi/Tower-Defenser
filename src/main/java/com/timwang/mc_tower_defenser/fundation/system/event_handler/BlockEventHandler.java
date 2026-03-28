package com.timwang.mc_tower_defenser.fundation.system.event_handler;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.system.GlobalNationManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * 方块相关事件处理器。
 * 当前用于演示 UrbanCore 领地保护效果。
 */
@EventBusSubscriber(modid = MinecraftTowerDefenser.MODID)
public class BlockEventHandler {
    @SubscribeEvent
    private static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        // 目前的保护规则是“落在任意国家 UrbanCore 领地半径内就禁止破坏”。
        GlobalNationManager manager = GlobalNationManager.get(serverLevel);
        if (manager.isInAnyTerritory(event.getPos())) {
            event.setCanceled(true);
            if (event.getPlayer() != null) {
                event.getPlayer().displayClientMessage(Component.literal("[test] This block is protected by UrbanCore"), true);
            }
            MinecraftTowerDefenser.LOGGER.info("[test] Cancelled block break at {} because it is inside UrbanCore territory", event.getPos());
        }
    }
}
