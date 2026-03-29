package com.timwang.mc_tower_defenser.fundation.blocks.Core;

import com.timwang.mc_tower_defenser.fundation.blockEntities.Core.UrbanCoreBlockEntities;
import com.timwang.mc_tower_defenser.fundation.system.GlobalNationManager;
import com.timwang.mc_tower_defenser.fundation.system.NationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
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

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!(level instanceof ServerLevel serverLevel) || !(placer instanceof ServerPlayer player)) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof UrbanCoreBlockEntities urbanCore)) {
            return;
        }

        String playerName = player.getGameProfile().getName();
        NationManager nation = GlobalNationManager.get(serverLevel).getNationByPlayer(playerName);
        urbanCore.bindToNation(playerName, nation == null ? "" : nation.getNationName());
        urbanCore.registerBoundNation(serverLevel);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (level instanceof ServerLevel serverLevel && blockEntity instanceof UrbanCoreBlockEntities urbanCore) {
                urbanCore.unregisterBoundNation(serverLevel);
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }
}
