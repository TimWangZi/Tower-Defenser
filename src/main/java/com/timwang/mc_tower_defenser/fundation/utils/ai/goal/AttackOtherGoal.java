package com.timwang.mc_tower_defenser.fundation.utils.ai.goal;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.function.Predicate;

/**
 * 预留的 AI Goal 占位类。
 * 目前还没有接入任何具体判定，后续可以在这里实现阵营间攻击逻辑。
 */
public class AttackOtherGoal extends Goal {
    public AttackOtherGoal() {

    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

}
