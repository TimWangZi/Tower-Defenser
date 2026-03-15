package com.timwang.mc_tower_defenser.fundation.blockEntities.Core;

import com.timwang.mc_tower_defenser.fundation.blockEntities.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class UrbanCore extends BlockEntity {
     public UrbanCore(BlockPos pos, BlockState state) {
         super(ModBlockEntities.URBAN_CORE.get(), pos, state);
     }
}
