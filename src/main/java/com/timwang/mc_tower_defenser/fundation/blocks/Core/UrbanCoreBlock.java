package com.timwang.mc_tower_defenser.fundation.blocks.Core;

import com.timwang.mc_tower_defenser.fundation.blockEntities.Core.UrbanCoreBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * UrbanCore 对应的方块类型。
 * 这个方块始终携带方块实体，渲染也走 Geckolib 的实体方块路径。
 */
public class UrbanCoreBlock extends Block implements EntityBlock {
    public UrbanCoreBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new UrbanCoreBlockEntities(blockPos, blockState);
    }

    /** 返回实体方块渲染，允许方块使用动态模型和动画。 */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}
