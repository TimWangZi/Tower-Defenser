package com.timwang.mc_tower_defenser.fundation.ai.profession.task;

import com.timwang.mc_tower_defenser.fundation.entities.Mobs.CitizenEntity;
import com.timwang.mc_tower_defenser.fundation.utils.TaskType;
import com.timwang.mc_tower_defenser.fundation.ai.profession.FarmerProfession;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * 逃跑任务。
 * 被攻击后朝攻击源反方向奔跑一段时间，然后回到工作方块流程。
 */
public class EscapeTask extends Task<FarmerProfession> {
    private static final int ESCAPE_DURATION_TICKS = 60;
    private static final int ESCAPE_HORIZONTAL_RANGE = 5;
    private static final int ESCAPE_VERTICAL_RANGE = 4;
    private static final double ESCAPE_SPEED_MULTIPLIER = 1.35D;

    private int remainingTicks;
    @Nullable
    private Vec3 escapeFromPos;
    @Nullable
    private BlockPos currentTarget;
    @Nullable
    private Path currentPath;

    public EscapeTask() {
        super("escape", TaskType.INTERRUPTIBLE);
    }

    @Override
    protected void onEnter(FarmerProfession context) {
        CitizenEntity citizen = context.getParent();
        this.remainingTicks = ESCAPE_DURATION_TICKS;
        this.currentTarget = null;
        this.currentPath = null;
        this.escapeFromPos = context.consumeEscapeSourcePos();
        if (this.escapeFromPos == null) {
            citizen.getNavigation().stop();
            finish();
        }
    }

    @Override
    protected void onTick(FarmerProfession context) {
        CitizenEntity citizen = context.getParent();

        Vec3 refreshedEscapeSourcePos = context.consumeEscapeSourcePos();
        if (refreshedEscapeSourcePos != null) {
            this.escapeFromPos = refreshedEscapeSourcePos;
            this.remainingTicks = ESCAPE_DURATION_TICKS;
            this.currentTarget = null;
            this.currentPath = null;
        }

        if (this.escapeFromPos == null) {
            citizen.getNavigation().stop();
            finish();
            return;
        }

        if (this.remainingTicks-- <= 0) {
            citizen.getNavigation().stop();
            finish();
            return;
        }

        if (this.currentTarget == null || citizen.getNavigation().isDone()) {
            if (!moveAwayFromThreat(citizen, this.escapeFromPos)) {
                citizen.getNavigation().stop();
                //finish();
            }
        }
    }

    @Override
    protected void onExit(FarmerProfession context) {
        this.remainingTicks = 0;
        this.escapeFromPos = null;
        this.currentTarget = null;
        this.currentPath = null;
        context.getParent().getNavigation().stop();
        super.onExit(context);
    }

    private boolean moveAwayFromThreat(CitizenEntity citizen, Vec3 threatPos) {
        Vec3 targetPos = LandRandomPos.getPosAway(citizen, ESCAPE_HORIZONTAL_RANGE, ESCAPE_VERTICAL_RANGE, threatPos);
        if (targetPos == null) {
            return false;
        }

        BlockPos targetBlockPos = BlockPos.containing(targetPos);
        Path path = citizen.getNavigation().createPath(targetBlockPos, 1);
        if (path == null || !path.canReach()) {
            return false;
        }

        this.currentTarget = targetBlockPos;
        this.currentPath = path;
        return citizen.getNavigation().moveTo(path, citizen.getWalkingSpeed() * ESCAPE_SPEED_MULTIPLIER);
    }
}
