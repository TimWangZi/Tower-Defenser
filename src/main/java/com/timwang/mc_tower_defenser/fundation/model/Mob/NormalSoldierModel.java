package com.timwang.mc_tower_defenser.fundation.model.Mob;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.blockEntities.Core.UrbanCoreBlockEntities;
import com.timwang.mc_tower_defenser.fundation.entities.Mobs.NormalSoldier;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NormalSoldierModel extends GeoModel<NormalSoldier> {
    private final ResourceLocation model = ResourceLocation.fromNamespaceAndPath(MinecraftTowerDefenser.MODID, "geo/test_soldier.geo.json");
    private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MinecraftTowerDefenser.MODID, "test_text.png");
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
