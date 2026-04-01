package com.timwang.mc_tower_defenser.fundation.blocks.WorkBlock;

import com.timwang.mc_tower_defenser.fundation.blockEntities.WorkBlock.FarmerWorkBlockEntities;
import com.timwang.mc_tower_defenser.fundation.gui.Backend.FarmerWorkMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * 农民工作方块。
 * 负责创建对应的 FarmerWorkBlockEntities，并在放置时注册为工作节点。
 */
public class FarmerWorkBlock extends Block implements EntityBlock {
    public FarmerWorkBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new FarmerWorkBlockEntities(blockPos, blockState);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
        if (blockEntity instanceof FarmerWorkBlockEntities farmerWorkBlock) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, menuPlayer) -> new FarmerWorkMenu(
                            containerId,
                            playerInventory,
                            farmerWorkBlock,
                            ContainerLevelAccess.create(serverLevel, pos)
                    ),
                    Component.translatable("gui.minecraft_tower_defenser.farmer_work")
            ));
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
        if (blockEntity instanceof FarmerWorkBlockEntities farmerWorkBlock) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, menuPlayer) -> new FarmerWorkMenu(
                            containerId,
                            playerInventory,
                            farmerWorkBlock,
                            ContainerLevelAccess.create(serverLevel, pos)
                    ),
                    Component.translatable("gui.minecraft_tower_defenser.farmer_work")
            ));
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!(level instanceof ServerLevel serverLevel) || !(placer instanceof ServerPlayer player)) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FarmerWorkBlockEntities farmerWorkBlock) {
            farmerWorkBlock.registerWorkBlock(serverLevel, player, "farmer_work");
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (level instanceof ServerLevel serverLevel && blockEntity instanceof FarmerWorkBlockEntities farmerWorkBlock) {
                farmerWorkBlock.unregisterWorkBlock(serverLevel);
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }
}
