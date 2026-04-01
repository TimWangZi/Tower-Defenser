package com.timwang.mc_tower_defenser.fundation.utils.ai.goal;

import com.timwang.mc_tower_defenser.fundation.entities.Mobs.CitizenEntity;
import com.timwang.mc_tower_defenser.fundation.utils.ai.profession.ProfessionBase;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;

public class CitizenGoal extends Goal {
    private final CitizenEntity owner;

    public CitizenGoal(CitizenEntity owner) {
        this.owner = owner;
    }

    @Override
    public boolean canUse() {
        return getProfession() != null && owner.level() instanceof ServerLevel && owner.hasNationBelongTo();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        ProfessionBase<? extends CitizenEntity, ?> profession = getProfession();
        if (profession != null) {
            profession.tick();
        }
    }

    private ProfessionBase<? extends CitizenEntity, ?> getProfession() {
        return owner.getProfession();
    }
}
