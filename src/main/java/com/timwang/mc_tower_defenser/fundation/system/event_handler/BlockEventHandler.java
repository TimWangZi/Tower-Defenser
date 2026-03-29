package com.timwang.mc_tower_defenser.fundation.system.event_handler;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.system.GlobalNationManager;
import com.timwang.mc_tower_defenser.fundation.system.NationManager;
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

        GlobalNationManager manager = GlobalNationManager.get(serverLevel);
        NationManager territoryNation = manager.getNationByTerritory(event.getPos());
        if (territoryNation == null) {
            return;
        }

        if (event.getPlayer() == null) {
            event.setCanceled(true);
            MinecraftTowerDefenser.LOGGER.info("[test] Cancelled block break at {} because no player context was available", event.getPos());
            return;
        }

        String playerName = event.getPlayer().getGameProfile().getName();
        NationManager playerNation = manager.getNationByPlayer(playerName);
        if (playerNation != null && territoryNation.getNationName().equals(playerNation.getNationName())) {
            return;
        }

        event.setCanceled(true);
        event.getPlayer().displayClientMessage(
                Component.literal("[test] Only members of nation " + territoryNation.getNationName() + " can break blocks here"),
                true
        );
        MinecraftTowerDefenser.LOGGER.info(
                "[test] Cancelled block break at {} because player {} does not belong to nation {}",
                event.getPos(),
                playerName,
                territoryNation.getNationName()
        );
    }
}
