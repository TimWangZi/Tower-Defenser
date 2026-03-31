package com.timwang.mc_tower_defenser.fundation.entities.Mobs;

import com.timwang.mc_tower_defenser.fundation.utils.ai.profession.ProfessionBase;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public class CitizenEntity extends PathfinderMob {
    protected ProfessionBase<CitizenEntity,?> profession;
    protected CitizenEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        // 在此处写入职业初始化代码
    }
    public boolean hasNationBelongTo() {
        return true;
    }
}
