package com.timwang.mc_tower_defenser.fundation.blockEntities.WorkBlock;

import com.timwang.mc_tower_defenser.fundation.blockEntities.ModBlockEntities;
import com.timwang.mc_tower_defenser.fundation.entities.ModEntities;
import com.timwang.mc_tower_defenser.fundation.entities.Mobs.CitizenEntity;
import com.timwang.mc_tower_defenser.fundation.utils.ai.profession.FarmerProfession;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * 农民工作方块实体。
 * 负责招募并初始化绑定到当前工作方块的农民市民。
 */
public class FarmerWorkBlockEntities extends WorkBlockEntities {
    public FarmerWorkBlockEntities(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FARMER_WORK.get(), pos, blockState);
    }

    /**
     * 预留招募条件判定。
     * 后续可在这里接入人口、资源、阵营科技或配额等限制。
     */
    protected boolean canRecruitFarmerCitizen(ServerLevel level) {
        return true;
    }

    @Nullable
    public CitizenEntity recruitFarmerCitizen(ServerLevel level) {
        if (level == null || getNationName().isBlank() || !canRecruitFarmerCitizen(level)) {
            return null;
        }

        CitizenEntity citizen = ModEntities.CITIZEN.get().create(level);
        if (citizen == null) {
            return null;
        }

        BlockPos spawnPos = getBlockPos().above();
        citizen.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        if (!level.noCollision(citizen)) {
            return null;
        }

        citizen.bindWorkBlock(getBlockPos());
        citizen.installProfession(new FarmerProfession(citizen, level));
        citizen.setPersistenceRequired();
        if (!level.addFreshEntity(citizen)) {
            return null;
        }

        return citizen;
    }
}
