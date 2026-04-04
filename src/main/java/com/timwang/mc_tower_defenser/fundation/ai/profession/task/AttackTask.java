package com.timwang.mc_tower_defenser.fundation.ai.profession.task;

import com.timwang.mc_tower_defenser.fundation.ai.profession.ProfessionBase;
import com.timwang.mc_tower_defenser.fundation.blockEntities.WorkBlock.WorkBlockEntities;
import com.timwang.mc_tower_defenser.fundation.entities.Mobs.CitizenEntity;
import com.timwang.mc_tower_defenser.fundation.system.GlobalNationManager;
import com.timwang.mc_tower_defenser.fundation.system.NationManager;
import com.timwang.mc_tower_defenser.fundation.utils.TaskType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * 追击任务。
 * 负责索敌、追踪，并在近战距离内使用主手物品攻击目标。
 */
public class AttackTask<P extends ProfessionBase<CitizenEntity, P>> extends Task<P> {
    private static final float DEFAULT_SEARCH_RANGE = 15.0f;
    private static final float TARGET_LOST_RANGE_MULTIPLIER = 1.75f;
    private static final double MIN_ATTACK_REACH = 2.0D;
    private static final int DEFAULT_ATTACK_INTERVAL_TICKS = 12;
    private static final int PATH_RECALCULATE_INTERVAL_TICKS = 10;
    private static final float RUN_SPEED = 2;

    @Nullable
    private LivingEntity target;
    @Nullable
    private BlockPos lastTargetPos;
    @Nullable
    private Path path;
    private int repathCooldown;
    private int attackCooldown;

    public AttackTask() {
        super("attack_task", TaskType.BLOCKING);
    }

    public boolean hasEnemies(P context) {
        return findNearestEnemy(context) != null;
    }

    @Override
    protected void onEnter(P context) {
        this.target = null;
        this.lastTargetPos = null;
        this.path = null;
        this.repathCooldown = 0;
        this.attackCooldown = 0;
    }

    @Override
    protected void onTick(P context) {
        CitizenEntity citizen = context.getParent();
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        // 当前目标失效时，重新按最近敌人原则选一个新目标。
        if (this.target == null || !isEnemy(context, this.target) || !this.target.isAlive()) {
            this.target = findNearestEnemy(context);
            this.lastTargetPos = null;
            this.path = null;
            this.repathCooldown = 0;
            this.attackCooldown = 0;
            if (this.target == null) {
                stopAndFinish(citizen);
                return;
            }
        }

        citizen.setTarget(this.target);
        citizen.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

        // 目标跑出追击容忍范围后，立即退出追击状态，交回状态机决定下一步。
        if (isTargetLost(citizen, this.target)) {
            stopAndFinish(citizen);
            return;
        }

        // 进入近战距离后，停止寻路并使用主手物品执行一次攻击结算。
        if (hasReachedAttackDistance(citizen, this.target)) {
            citizen.getNavigation().stop();
            this.path = null;
            tryAttackTarget(citizen, this.target);
            return;
        }

        if (!moveTowardTarget(citizen, this.target)) {
            stopAndFinish(citizen);
        }
    }

    @Override
    protected void onExit(P context) {
        clearNavigationState(context.getParent());
        super.onExit(context);
    }

    protected boolean isEnemy(P context, @Nullable LivingEntity entity) {
        if (entity == null || entity == context.getParent() || !entity.isAlive()) {
            return false;
        }

        if (entity instanceof Monster) {
            return true;
        }

        String ownNationName = resolveCitizenNationName(context.getServerLevel(), context.getParent());
        if (ownNationName.isBlank()) {
            return false;
        }

        if (entity instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) {
                return false;
            }

            NationManager playerNation = GlobalNationManager.get(context.getServerLevel())
                    .getNationByPlayer(player.getGameProfile().getName());
            String playerNationName = playerNation == null ? "" : playerNation.getNationName();
            return !ownNationName.equals(playerNationName);
        }

        if (entity instanceof CitizenEntity otherCitizen) {
            String otherNationName = resolveCitizenNationName(context.getServerLevel(), otherCitizen);
            return !otherNationName.isBlank() && !ownNationName.equals(otherNationName);
        }

        return false;
    }

    @Nullable
    private LivingEntity findNearestEnemy(P context) {
        CitizenEntity citizen = context.getParent();
        double searchRange = resolveSearchRange(citizen);
        List<LivingEntity> livingEntities = context.getServerLevel().getEntitiesOfClass(
                LivingEntity.class,
                citizen.getBoundingBox().inflate(searchRange),
                entity -> isEnemy(context, entity)
        );

        if (livingEntities.isEmpty()) {
            return null;
        }

        return livingEntities.stream()
                .min(Comparator.comparingDouble(citizen::distanceToSqr))
                .orElse(null);
    }

    private boolean moveTowardTarget(CitizenEntity citizen, LivingEntity target) {
        BlockPos nextTargetPos = target.blockPosition().immutable();
        if (!nextTargetPos.equals(this.lastTargetPos)) {
            this.lastTargetPos = nextTargetPos;
            this.path = null;
            this.repathCooldown = 0;
        }

        if (this.repathCooldown > 0) {
            this.repathCooldown--;
        }

        // 目标移动后不必每 tick 重算路径，定期重算即可兼顾跟踪精度和开销。
        if (this.path == null || citizen.getNavigation().isDone() || this.repathCooldown <= 0) {
            this.path = citizen.getNavigation().createPath(this.lastTargetPos, 1);
            if (this.path == null || !this.path.canReach()) {
                return false;
            }
            this.repathCooldown = PATH_RECALCULATE_INTERVAL_TICKS;
        }

        return citizen.getNavigation().moveTo(this.path, citizen.getWalkingSpeed() * RUN_SPEED);
    }

    private boolean hasReachedAttackDistance(CitizenEntity citizen, LivingEntity target) {
        double reach = Math.max(MIN_ATTACK_REACH, citizen.getBbWidth() + target.getBbWidth() + 0.75D);
        return citizen.position().distanceToSqr(target.position()) <= reach * reach;
    }

    private void tryAttackTarget(CitizenEntity citizen, LivingEntity target) {
        if (this.attackCooldown > 0 || !target.isAlive()) {
            return;
        }

        citizen.swing(InteractionHand.MAIN_HAND);
        citizen.doHurtTarget(target);
        citizen.getRightHandItem().getItem().hurtEnemy(citizen.getRightHandItem().getItem().getDefaultInstance() ,target ,citizen);
        this.attackCooldown = DEFAULT_ATTACK_INTERVAL_TICKS;
    }

    private boolean isTargetLost(CitizenEntity citizen, LivingEntity target) {
        double searchRange = resolveSearchRange(citizen);
        double maxDistance = searchRange * TARGET_LOST_RANGE_MULTIPLIER;
        return citizen.position().distanceToSqr(target.position()) > maxDistance * maxDistance;
    }

    private double resolveSearchRange(CitizenEntity citizen) {
        double followRange = citizen.getAttributeValue(Attributes.FOLLOW_RANGE);
        return followRange > 0.0D ? followRange : DEFAULT_SEARCH_RANGE;
    }

    private String resolveCitizenNationName(ServerLevel level, CitizenEntity citizen) {
        BlockPos boundWorkBlockPos = citizen.getBoundWorkBlockPos();
        if (boundWorkBlockPos != null) {
            BlockEntity blockEntity = level.getBlockEntity(boundWorkBlockPos);
            if (blockEntity instanceof WorkBlockEntities workBlock) {
                return workBlock.getNationName();
            }
        }
        stopAndFinish(citizen);
        return "";
    }

    private void stopAndFinish(CitizenEntity citizen) {
        clearNavigationState(citizen);
        finish();
    }

    private void clearNavigationState(CitizenEntity citizen) {
        this.target = null;
        this.lastTargetPos = null;
        this.path = null;
        this.repathCooldown = 0;
        this.attackCooldown = 0;
        citizen.setTarget(null);
        citizen.getNavigation().stop();
    }
}
