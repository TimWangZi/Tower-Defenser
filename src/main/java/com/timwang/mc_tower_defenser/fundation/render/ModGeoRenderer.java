package com.timwang.mc_tower_defenser.fundation.render;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.blockEntities.ModBlockEntities;
import com.timwang.mc_tower_defenser.fundation.entities.ModEntities;
import com.timwang.mc_tower_defenser.fundation.render.Core.UrbanCoreRenderer;
import com.timwang.mc_tower_defenser.fundation.render.Mobs.NormalSoldierRender;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = MinecraftTowerDefenser.MODID, value = Dist.CLIENT)
public class ModGeoRenderer {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        System.out.println("[UrbanCoreRenderer] registering block entity renderer");
        event.registerBlockEntityRenderer(ModBlockEntities.URBAN_CORE.get(), UrbanCoreRenderer::new);
        event.registerEntityRenderer(ModEntities.NORMAL_SOLDIER.get(),NormalSoldierRender::new);
    }
}
