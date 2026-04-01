package com.timwang.mc_tower_defenser.fundation.utils.ai.profession.task;

import com.timwang.mc_tower_defenser.fundation.entities.Mobs.CitizenEntity;
import com.timwang.mc_tower_defenser.fundation.utils.Task;
import com.timwang.mc_tower_defenser.fundation.utils.TaskType;
import com.timwang.mc_tower_defenser.fundation.utils.ai.profession.FarmerProfession;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * 收割任务骨架。
 * 当前不提供任何具体实现。
 */
public class HarvestTask extends Task<FarmerProfession> {
    private static final double MIN_HARVEST_REACH = 1.5D;

    private BlockPos harvest_target;
    private int half_block = 5;
    private Path path;

    public HarvestTask() {
        super("harvest", TaskType.INTERRUPTIBLE);
    }

    @Override
    protected void onTick(FarmerProfession context) {
        CitizenEntity citizen = context.getParent();
        ServerLevel level = context.getServerLevel();

        if (citizen.isBackpackFull()) {
            clearHarvestTarget(citizen);
            finish();
            return;
        }

        if (harvest_target != null && !isHarvestableCrop(level, harvest_target)) {
            clearHarvestTarget(citizen);
        }

        if (harvest_target == null) {
            if (!tryAcquireHarvestTarget(citizen, level)) {
                finish();
            }
            return;
        }

        if (hasReachedHarvestTarget(citizen, harvest_target)) {
            boolean shouldReturnToWorkBlock = harvestCrop(citizen, level, harvest_target);
            clearHarvestTarget(citizen);
            if (shouldReturnToWorkBlock) {
                finish();
            }
            return;
        }

        if (path == null || citizen.getNavigation().isDone()) {
            moveToHarvestTarget(citizen, harvest_target);
        }
    }

    @Override
    protected void onExit(FarmerProfession context) {
        clearHarvestTarget(context.getParent());
        super.onExit(context);
    }

    private boolean tryAcquireHarvestTarget(CitizenEntity citizen, ServerLevel level) {
        BlockPos center = citizen.blockPosition();
        BlockPos bestTarget = null;
        Path bestPath = null;
        double bestDistance = Double.MAX_VALUE;

        for (BlockPos candidate : BlockPos.betweenClosed(center.offset(-half_block, -half_block, -half_block), center.offset(half_block, half_block, half_block))) {
            BlockPos candidatePos = candidate.immutable();
            if (!isHarvestableCrop(level, candidatePos)) {
                continue;
            }

            double distance = candidatePos.distSqr(center);
            if (distance >= bestDistance) {
                continue;
            }

            Path candidatePath = citizen.getNavigation().createPath(candidatePos, 1);
            if (candidatePath == null || !candidatePath.canReach()) {
                continue;
            }

            bestTarget = candidatePos;
            bestPath = candidatePath;
            bestDistance = distance;
        }

        if (bestTarget == null || bestPath == null) {
            return false;
        }

        harvest_target = bestTarget;
        path = bestPath;
        if (!citizen.getNavigation().moveTo(bestPath, citizen.getWalkingSpeed())) {
            clearHarvestTarget(citizen);
            return false;
        }
        return true;
    }

    private void moveToHarvestTarget(CitizenEntity citizen, BlockPos target) {
        path = citizen.getNavigation().createPath(target, 1);
        if (path == null || !path.canReach()) {
            clearHarvestTarget(citizen);
            return;
        }

        if (!citizen.getNavigation().moveTo(path, citizen.getWalkingSpeed())) {
            clearHarvestTarget(citizen);
        }
    }

    private boolean hasReachedHarvestTarget(CitizenEntity citizen, BlockPos target) {
        double reach = Math.max(MIN_HARVEST_REACH, citizen.getBbWidth() + 1.0D);
        return citizen.position().distanceToSqr(Vec3.atCenterOf(target)) <= reach * reach;
    }

    private boolean isHarvestableCrop(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CropBlock cropBlock)) {
            return false;
        }

        return cropBlock.isMaxAge(state);
    }

    private boolean harvestCrop(CitizenEntity citizen, ServerLevel level, BlockPos target) {
        BlockState state = level.getBlockState(target);
        if (!(state.getBlock() instanceof CropBlock cropBlock) || !cropBlock.isMaxAge(state)) {
            return false;
        }

        ItemStack rightHand = citizen.getRightHandItem();
        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(target) : null;
        List<ItemStack> drops = new ArrayList<>(Block.getDrops(state, level, target, blockEntity, citizen, rightHand.copy()));
        consumeReplantCost(cropBlock, level, target, state, drops);

        citizen.swing(InteractionHand.MAIN_HAND);
        if (!level.setBlock(target, cropBlock.getStateForAge(0), Block.UPDATE_ALL)) {
            return false;
        }

        boolean backpackFullAfterHarvest = citizen.isBackpackFull();
        for (ItemStack drop : drops) {
            ItemStack leftover = citizen.addItemToBackpack(drop.copy());
            if (!leftover.isEmpty()) {
                citizen.spawnAtLocation(leftover);
                backpackFullAfterHarvest = true;
            }
        }

        return backpackFullAfterHarvest;
    }

    private void consumeReplantCost(CropBlock cropBlock, ServerLevel level, BlockPos target, BlockState state, List<ItemStack> drops) {
        ItemStack replantSeed = cropBlock.getCloneItemStack(level, target, state);
        if (replantSeed.isEmpty()) {
            return;
        }

        for (int i = 0; i < drops.size(); i++) {
            ItemStack drop = drops.get(i);
            if (!ItemStack.isSameItemSameComponents(drop, replantSeed)) {
                continue;
            }

            drop.shrink(1);
            if (drop.isEmpty()) {
                drops.remove(i);
            }
            return;
        }
    }

    private void clearHarvestTarget(CitizenEntity citizen) {
        harvest_target = null;
        path = null;
        citizen.getNavigation().stop();
    }
}
