package com.timwang.mc_tower_defenser.fundation.blockEntities.WorkBlock;

import com.timwang.mc_tower_defenser.fundation.ai.profession.ProfessionBase;
import com.timwang.mc_tower_defenser.fundation.ai.profession.SoldierProfession;
import com.timwang.mc_tower_defenser.fundation.blockEntities.ModBlockEntities;
import com.timwang.mc_tower_defenser.fundation.entities.Mobs.CitizenEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 士兵工作方块实体。
 * 士兵特化点只负责提供职业实例，公共招募流程由 WorkBlockEntities 处理。
 */
public class SoldierWorkBlockEntities extends WorkBlockEntities {
    public SoldierWorkBlockEntities(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.SOLDIER_WORK.get(), pos, blockState);
    }

    @Override
    protected ProfessionBase<? extends CitizenEntity, ?> createProfession(CitizenEntity citizen, ServerLevel level) {
        return new SoldierProfession(citizen, level);
    }
}
