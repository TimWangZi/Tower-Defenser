package com.timwang.mc_tower_defenser.fundation.ai.profession.task;

import com.timwang.mc_tower_defenser.fundation.blockEntities.WorkBlock.WorkBlockEntities;
import com.timwang.mc_tower_defenser.fundation.entities.Mobs.CitizenEntity;
import com.timwang.mc_tower_defenser.fundation.system.GlobalNationManager;
import com.timwang.mc_tower_defenser.fundation.system.NationManager;
import com.timwang.mc_tower_defenser.fundation.system.work.Edge;
import com.timwang.mc_tower_defenser.fundation.utils.TaskType;
import com.timwang.mc_tower_defenser.fundation.ai.profession.ProfessionBase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 通用工作方块交互任务。
 * 市民会前往自己绑定的工作方块，并在到达后调用投递/请求回调。
 */
public class WorkBlockTask<P extends ProfessionBase<CitizenEntity, P>> extends Task<P> {
    private static final double MIN_INTERACTION_REACH = 2.0D;

    private Consumer<WorkBlockTaskContext<P>> requestItemsCallback = context -> {
    };
    private Consumer<WorkBlockTaskContext<P>> deliverItemsCallback = context -> {
    };

    private BlockPos targetWorkBlockPos;
    private Path path;

    public WorkBlockTask(String name) {
        super(name, TaskType.INTERRUPTIBLE);
    }

    public WorkBlockTask<P> setRequestItemsCallback(Consumer<WorkBlockTaskContext<P>> callback) {
        this.requestItemsCallback = callback == null ? context -> {
        } : callback;
        return this;
    }

    public WorkBlockTask<P> setDeliverItemsCallback(Consumer<WorkBlockTaskContext<P>> callback) {
        this.deliverItemsCallback = callback == null ? context -> {
        } : callback;
        return this;
    }

    @Override
    protected void onEnter(P context) {
        this.targetWorkBlockPos = null;
        this.path = null;
    }

    @Override
    protected void onTick(P context) {
        CitizenEntity citizen = context.getParent();
        BlockPos boundWorkBlockPos = citizen.getBoundWorkBlockPos();
        if (boundWorkBlockPos == null) {
            stopAndFinish(citizen);
            return;
        }

        BlockPos nextTarget = boundWorkBlockPos.immutable();
        if (!nextTarget.equals(this.targetWorkBlockPos)) {
            this.targetWorkBlockPos = nextTarget;
            this.path = null;
        }

        WorkBlockEntities workBlock = resolveWorkBlock(context, nextTarget);
        if (workBlock == null) {
            citizen.clearBoundWorkBlock();
            stopAndFinish(citizen);
            return;
        }

        if (!hasReachedWorkBlock(citizen, nextTarget)) {
            moveToWorkBlock(citizen, nextTarget);
            return;
        }

        citizen.getNavigation().stop();
        WorkBlockTaskContext<P> taskContext = buildTaskContext(context, workBlock);
        this.deliverItemsCallback.accept(taskContext);
        this.requestItemsCallback.accept(taskContext);
        finish();
    }

    @Override
    protected void onExit(P context) {
        clearNavigationState(context.getParent());
        super.onExit(context);
    }

    private WorkBlockEntities resolveWorkBlock(P context, BlockPos pos) {
        BlockEntity blockEntity = context.getServerLevel().getBlockEntity(pos);
        return blockEntity instanceof WorkBlockEntities workBlock ? workBlock : null;
    }

    private WorkBlockTaskContext<P> buildTaskContext(P context, WorkBlockEntities currentWorkBlock) {
        ServerLevel level = context.getServerLevel();
        return new WorkBlockTaskContext<>(
                context,
                level,
                context.getParent(),
                currentWorkBlock,
                resolveSourceWorkBlocks(level, currentWorkBlock)
        );
    }

    private List<WorkBlockEntities> resolveSourceWorkBlocks(ServerLevel level, WorkBlockEntities currentWorkBlock) {
        NationManager nation = resolveWorkBlockNation(level, currentWorkBlock);
        if (nation == null) {
            return List.of();
        }

        List<Edge> incomingEdges = nation.getWorkGraphManager().getIncomingEdges(currentWorkBlock.getBlockPos());
        if (incomingEdges.isEmpty()) {
            return List.of();
        }

        List<WorkBlockEntities> sourceWorkBlocks = new ArrayList<>();
        for (Edge edge : incomingEdges) {
            BlockEntity blockEntity = level.getBlockEntity(edge.getFrom());
            if (!(blockEntity instanceof WorkBlockEntities sourceWorkBlock)) {
                continue;
            }
            if (sourceWorkBlock == currentWorkBlock) {
                continue;
            }
            sourceWorkBlocks.add(sourceWorkBlock);
        }

        return List.copyOf(sourceWorkBlocks);
    }

    private NationManager resolveWorkBlockNation(ServerLevel level, WorkBlockEntities currentWorkBlock) {
        GlobalNationManager manager = GlobalNationManager.get(level);
        String nationName = currentWorkBlock.getNationName();
        if (nationName != null && !nationName.isBlank()) {
            NationManager nation = manager.getNationByName(nationName);
            if (nation != null) {
                return nation;
            }
        }

        return manager.getNationByTerritory(currentWorkBlock.getBlockPos());
    }

    private void moveToWorkBlock(CitizenEntity citizen, BlockPos target) {
        if (this.path == null || citizen.getNavigation().isDone()) {
            this.path = citizen.getNavigation().createPath(target, 1);
            if (this.path == null || !this.path.canReach()) {
                stopAndFinish(citizen);
                return;
            }
        }

        if (!citizen.getNavigation().moveTo(this.path, citizen.getWalkingSpeed())) {
            stopAndFinish(citizen);
        }
    }

    private boolean hasReachedWorkBlock(CitizenEntity citizen, BlockPos target) {
        double reach = Math.max(MIN_INTERACTION_REACH, citizen.getBbWidth() + 1.5D);
        return citizen.position().distanceToSqr(Vec3.atCenterOf(target)) <= reach * reach;
    }

    private void stopAndFinish(CitizenEntity citizen) {
        clearNavigationState(citizen);
        finish();
    }

    private void clearNavigationState(CitizenEntity citizen) {
        this.path = null;
        this.targetWorkBlockPos = null;
        citizen.getNavigation().stop();
    }
}
