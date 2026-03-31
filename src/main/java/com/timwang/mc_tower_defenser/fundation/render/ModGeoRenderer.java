package com.timwang.mc_tower_defenser.fundation.render;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.blockEntities.ModBlockEntities;
import com.timwang.mc_tower_defenser.fundation.render.Core.UrbanCoreRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 客户端渲染注册入口。
 * 统一把 Geckolib 的实体/方块实体渲染器挂到 NeoForge 渲染事件里。
 */
@EventBusSubscriber(modid = MinecraftTowerDefenser.MODID, value = Dist.CLIENT)
public class ModGeoRenderer {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        MinecraftTowerDefenser.LOGGER.info("Registering Geckolib renderers");
        event.registerBlockEntityRenderer(ModBlockEntities.URBAN_CORE.get(), UrbanCoreRenderer::new);
    }
}
