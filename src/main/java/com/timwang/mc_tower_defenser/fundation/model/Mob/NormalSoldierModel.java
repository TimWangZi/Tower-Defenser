package com.timwang.mc_tower_defenser.fundation.model.Mob;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.entities.Mobs.NormalSoldier;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 普通士兵的 Geckolib 模型定义。
 * 当前动画资源仍是占位路径，后续接入动画时可以直接在这里替换。
 */
public class NormalSoldierModel extends GeoModel<NormalSoldier> {
    private final ResourceLocation model = ResourceLocation.fromNamespaceAndPath(MinecraftTowerDefenser.MODID, "geo/test_soldier.geo.json");
    private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MinecraftTowerDefenser.MODID, "textures/urban_core.png");
    private final ResourceLocation animation = ResourceLocation.fromNamespaceAndPath(MinecraftTowerDefenser.MODID, "animations/null");

    @Override
    public ResourceLocation getModelResource(NormalSoldier animatable) {
        return this.model;
    }

    @Override
    public ResourceLocation getTextureResource(NormalSoldier animatable) {
        return this.texture;
    }

    @Override
    public ResourceLocation getAnimationResource(NormalSoldier animatable) {
        return this.animation;
    }
}
