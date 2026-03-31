package com.timwang.mc_tower_defenser.fundation.utils.ai.goal;

import com.timwang.mc_tower_defenser.fundation.entities.Mobs.CitizenEntity;
import com.timwang.mc_tower_defenser.fundation.utils.ai.profession.ProfessionBase;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

public class CitizenGoal extends Goal {
    CitizenEntity owner;
    ProfessionBase<? extends CitizenEntity,?> profession_base;
    public void CitizenGoal(CitizenEntity owner, ProfessionBase<? extends CitizenEntity,?> profession_base) {
        this.owner = owner;
        this.profession_base = profession_base;
    }
    @Override
    public boolean canUse() {
        if (!(owner.level() instanceof ServerLevel) || !owner.hasNationBelongTo()) {
            return false;
        }else {
            return true;
        }
    }
    @Override
    public void tick() {
        profession_base.tick();
    }
}
