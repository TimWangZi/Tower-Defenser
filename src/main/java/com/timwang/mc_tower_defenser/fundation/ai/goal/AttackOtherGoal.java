package com.timwang.mc_tower_defenser.fundation.ai.goal;

import com.timwang.mc_tower_defenser.fundation.entities.Mobs.NormalSoldier;
import com.timwang.mc_tower_defenser.fundation.system.GlobalNationManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.EnumSet;

/**
 * [test-only] 用于验证“普通士兵会主动攻击非本阵营目标”的简单 Target Goal。
 * 仅覆盖玩家与普通士兵两类目标，方便后续整块删除替换。
 */
public class AttackOtherGoal extends Goal {
    private final NormalSoldier mob;

    @Nullable
    private LivingEntity pendingTarget;

    public AttackOtherGoal(NormalSoldier mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!(mob.level() instanceof ServerLevel) || !mob.hasNationBelongTo()) {
            return false;
        }

        LivingEntity currentTarget = mob.getTarget();
        if (isEnemy(currentTarget)) {
            return false;
        }

        double searchRange = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        if (searchRange <= 0.0) {
            searchRange = 16.0;
        }

        pendingTarget = mob.level().getEntitiesOfClass(
                        LivingEntity.class,
                        mob.getBoundingBox().inflate(searchRange),
                        this::isEnemy
                ).stream()
                .min(Comparator.comparingDouble(mob::distanceToSqr))
                .orElse(null);

        return pendingTarget != null;
    }

    @Override
    public boolean canContinueToUse() {
        return isEnemy(mob.getTarget());
    }

    @Override
    public void start() {
        if (pendingTarget != null) {
            mob.setTarget(pendingTarget);
            pendingTarget = null;
        }
    }

    @Override
    public void stop() {
        pendingTarget = null;
        if (!isEnemy(mob.getTarget())) {
            mob.setTarget(null);
        }
    }

    private boolean isEnemy(@Nullable LivingEntity target) {
        if (target == null || target == mob || !target.isAlive()) {
            return false;
        }

        String ownNation = mob.getNationBelongTo();
        if (ownNation.isBlank()) {
            return false;
        }

        if (target instanceof NormalSoldier otherSoldier) {
            return !ownNation.equals(otherSoldier.getNationBelongTo());
        }

        if (target instanceof Player player) {
            if (player.isCreative() || player.isSpectator() || !(mob.level() instanceof ServerLevel serverLevel)) {
                return false;
            }

            var nation = GlobalNationManager.get(serverLevel)
                    .getNationByPlayer(player.getGameProfile().getName());
            String playerNation = nation == null ? "" : nation.getNationName();
            return !ownNation.equals(playerNation);
        }

        return false;
    }
}
