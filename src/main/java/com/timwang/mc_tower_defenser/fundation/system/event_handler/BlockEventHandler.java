package com.timwang.mc_tower_defenser.fundation.system.event_handler;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.system.GlobalNationManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = MinecraftTowerDefenser.MODID)
public class BlockEventHandler {
    @SubscribeEvent
    private static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        // 测试: 如果方块位于任何UrbanCore领地，则取消破坏
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
