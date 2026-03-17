package com.timwang.mc_tower_defenser.fundation.render.Core;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.blockEntities.Core.UrbanCoreBlockEntities;
import com.timwang.mc_tower_defenser.fundation.blockEntities.ModBlockEntities;
import com.timwang.mc_tower_defenser.fundation.model.Core.UrbanCoreModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

//@EventBusSubscriber(modid = MinecraftTowerDefenser.MODID, value = Dist.CLIENT)
public class UrbanCoreRenderer extends GeoBlockRenderer<UrbanCoreBlockEntities> {
    public UrbanCoreRenderer(BlockEntityRendererProvider.Context context) {
        super(new UrbanCoreModel());
    }


    /*@SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        System.out.println("[UrbanCoreRenderer] registering block entity renderer");
        event.registerBlockEntityRenderer(ModBlockEntities.URBAN_CORE.get(), UrbanCoreRenderer::new);
    }*/
}
