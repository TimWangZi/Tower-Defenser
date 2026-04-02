package com.timwang.mc_tower_defenser.fundation.ai.profession.task;

import com.timwang.mc_tower_defenser.fundation.ai.profession.FarmerProfession;
import com.timwang.mc_tower_defenser.fundation.entities.Mobs.CitizenEntity;
import com.timwang.mc_tower_defenser.fundation.utils.Task;
import com.timwang.mc_tower_defenser.fundation.utils.TaskType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 闲逛任务。
 * 市民会围绕当前工作方块附近随机走动一段时间，结束后回到返回工作方块流程。
 */
public class WalkAroundTask extends Task<FarmerProfession> {
    private static final int MIN_WALK_DURATION_TICKS = 80;
    private static final int MAX_EXTRA_WALK_DURATION_TICKS = 80;
    private static final int WALK_HORIZONTAL_RANGE = 6;
    private static final int WALK_VERTICAL_RANGE = 2;
    private static final int MAX_TARGET_SEARCH_ATTEMPTS = 12;
    private static final double MIN_TARGET_DISTANCE_SQR = 2.25D;
    private static final double MIN_TARGET_REACH = 1.25D;

    private int remainingTicks;
    @Nullable
    private BlockPos currentTarget;

    public WalkAroundTask() {
        super("walk_around", TaskType.INTERRUPTIBLE);
    }

    @Override
    protected void onEnter(FarmerProfession context) {
        CitizenEntity citizen = context.getParent();
        this.remainingTicks = MIN_WALK_DURATION_TICKS + citizen.getRandom().nextInt(MAX_EXTRA_WALK_DURATION_TICKS + 1);
        this.currentTarget = null;
    }

    @Override
    protected void onTick(FarmerProfession context) {
        CitizenEntity citizen = context.getParent();

        if (this.remainingTicks-- <= 0) {
            stopAndFinish(citizen);
            return;
        }

        if (this.currentTarget != null && hasReachedTarget(citizen, this.currentTarget)) {
            this.currentTarget = null;
            citizen.getNavigation().stop();
        }

        if (this.currentTarget == null || citizen.getNavigation().isDone()) {
            if (!moveToNextWalkTarget(citizen)) {
                stopAndFinish(citizen);
            }
        }
    }

    @Override
    protected void onExit(FarmerProfession context) {
        clearNavigationState(context.getParent());
        super.onExit(context);
    }

    private boolean moveToNextWalkTarget(CitizenEntity citizen) {
        BlockPos anchor = citizen.getBoundWorkBlockPos();
        if (anchor == null) {
            anchor = citizen.blockPosition();
        }

        RandomSource random = citizen.getRandom();
        for (int attempt = 0; attempt < MAX_TARGET_SEARCH_ATTEMPTS; attempt++) {
            int offsetX = random.nextInt(WALK_HORIZONTAL_RANGE * 2 + 1) - WALK_HORIZONTAL_RANGE;
            int offsetY = random.nextInt(WALK_VERTICAL_RANGE * 2 + 1) - WALK_VERTICAL_RANGE;
            int offsetZ = random.nextInt(WALK_HORIZONTAL_RANGE * 2 + 1) - WALK_HORIZONTAL_RANGE;

            if (offsetX == 0 && offsetY == 0 && offsetZ == 0) {
                continue;
            }

            BlockPos candidate = anchor.offset(offsetX, offsetY, offsetZ).immutable();
            if (candidate.distToCenterSqr(citizen.position()) < MIN_TARGET_DISTANCE_SQR) {
                continue;
            }

            Path path = citizen.getNavigation().createPath(candidate, 1);
            if (path == null || !path.canReach()) {
                continue;
            }

            this.currentTarget = candidate;
            if (citizen.getNavigation().moveTo(path, citizen.getWalkingSpeed())) {
                return true;
            }
        }

        return false;
    }

    private boolean hasReachedTarget(CitizenEntity citizen, BlockPos target) {
        double reach = Math.max(MIN_TARGET_REACH, citizen.getBbWidth() + 0.75D);
        return citizen.position().distanceToSqr(Vec3.atCenterOf(target)) <= reach * reach;
    }

    private void stopAndFinish(CitizenEntity citizen) {
        clearNavigationState(citizen);
        finish();
    }

    private void clearNavigationState(CitizenEntity citizen) {
        this.currentTarget = null;
        citizen.getNavigation().stop();
    }
}
