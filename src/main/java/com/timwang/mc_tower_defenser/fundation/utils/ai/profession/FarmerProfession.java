package com.timwang.mc_tower_defenser.fundation.utils.ai.profession;

import com.timwang.mc_tower_defenser.fundation.entities.Mobs.CitizenEntity;
import net.minecraft.server.level.ServerLevel;

/**
 * 农民职业骨架。
 * 当前只提供类型入口，不提供任何状态机实现。
 */
public abstract class FarmerProfession extends ProfessionBase<CitizenEntity, FarmerProfession> {
    protected FarmerProfession(CitizenEntity parent, ServerLevel serverLevel) {
        super(parent, serverLevel);
    }
}
