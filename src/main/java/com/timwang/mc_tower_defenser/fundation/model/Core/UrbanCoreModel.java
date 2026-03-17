package com.timwang.mc_tower_defenser.fundation.model.Core;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.blockEntities.Core.UrbanCoreBlockEntities;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class UrbanCoreModel extends GeoModel<UrbanCoreBlockEntities> {
     private final ResourceLocation model = ResourceLocation.fromNamespaceAndPath(MinecraftTowerDefenser.MODID, "geo/urban_core.geo.json");
     private final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MinecraftTowerDefenser.MODID, "textures/urban_core.png");
     private final ResourceLocation animation = ResourceLocation.fromNamespaceAndPath(MinecraftTowerDefenser.MODID, "animations/urban_core.animation.json");
     @Override
     public ResourceLocation getModelResource(UrbanCoreBlockEntities animatable) {
          return this.model;
     }

     @Override
     public ResourceLocation getTextureResource(UrbanCoreBlockEntities animatable) {
          return this.texture;
     }

     @Override
     public ResourceLocation getAnimationResource(UrbanCoreBlockEntities animatable) {
          return this.animation;
     }
}
