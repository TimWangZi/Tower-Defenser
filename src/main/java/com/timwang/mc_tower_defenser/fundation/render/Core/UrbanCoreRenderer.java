package com.timwang.mc_tower_defenser.fundation.render.Core;

import com.timwang.mc_tower_defenser.fundation.blockEntities.Core.UrbanCoreBlockEntities;
import com.timwang.mc_tower_defenser.fundation.model.Core.UrbanCoreModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * UrbanCore 的 Geckolib 渲染器。
 * 这里只负责把方块实体和对应模型连接起来。
 */
public class UrbanCoreRenderer extends GeoBlockRenderer<UrbanCoreBlockEntities> {
    public UrbanCoreRenderer(BlockEntityRendererProvider.Context context) {
        super(new UrbanCoreModel());
    }
}
