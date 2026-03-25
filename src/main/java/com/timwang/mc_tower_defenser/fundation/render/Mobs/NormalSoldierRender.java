package com.timwang.mc_tower_defenser.fundation.render.Mobs;

import com.timwang.mc_tower_defenser.fundation.entities.Mobs.NormalSoldier;
import com.timwang.mc_tower_defenser.fundation.model.Mob.NormalSoldierModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NormalSoldierRender extends GeoEntityRenderer<NormalSoldier> {
    public NormalSoldierRender(EntityRendererProvider.Context context) {
        super(context, new NormalSoldierModel());
    }
}
