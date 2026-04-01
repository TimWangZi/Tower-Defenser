package com.timwang.mc_tower_defenser.fundation.render.Mobs;

import com.timwang.mc_tower_defenser.fundation.entities.Mobs.CitizenEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 市民渲染器。
 * 当前先使用原版 Steve 贴图和基础人形模型，后续可替换为自定义资源。
 */
public class CitizenRender extends HumanoidMobRenderer<CitizenEntity, HumanoidModel<CitizenEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");

    public CitizenRender(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(CitizenEntity entity) {
        return TEXTURE;
    }
}
