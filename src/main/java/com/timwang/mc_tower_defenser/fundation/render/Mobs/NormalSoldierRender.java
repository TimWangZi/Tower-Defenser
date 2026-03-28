package com.timwang.mc_tower_defenser.fundation.render.Mobs;

import com.timwang.mc_tower_defenser.fundation.entities.Mobs.NormalSoldier;
import com.timwang.mc_tower_defenser.fundation.model.Mob.NormalSoldierModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 普通士兵实体渲染器。
 * 通过 Geckolib 模型驱动实体的几何体和贴图。
 */
public class NormalSoldierRender extends GeoEntityRenderer<NormalSoldier> {
    public NormalSoldierRender(EntityRendererProvider.Context context) {
        super(context, new NormalSoldierModel());
    }
}
