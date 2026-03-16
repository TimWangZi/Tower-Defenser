package com.timwang.mc_tower_defenser.fundation.model.Core;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.blockEntities.Core.UrbanCoreBlockEntities;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class UrbanCoreModel extends GeoModel<UrbanCoreBlockEntities> {
     @Override
     public ResourceLocation getModelResource(UrbanCoreBlockEntities animatable) {
          return ResourceLocation.fromNamespaceAndPath(MinecraftTowerDefenser.MODID, "geo/urban_core.geo.json");
     }

     @Override
     public ResourceLocation getTextureResource(UrbanCoreBlockEntities animatable) {
          return ResourceLocation.fromNamespaceAndPath(MinecraftTowerDefenser.MODID, "textures/block/urban_core.png");
     }

     @Override
     public ResourceLocation getAnimationResource(UrbanCoreBlockEntities animatable) {
          return ResourceLocation.fromNamespaceAndPath(MinecraftTowerDefenser.MODID, "animations/urban_core.animation.json");
     }
}
