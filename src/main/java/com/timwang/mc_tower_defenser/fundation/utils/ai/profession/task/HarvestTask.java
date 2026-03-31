package com.timwang.mc_tower_defenser.fundation.utils.ai.profession.task;

import com.timwang.mc_tower_defenser.fundation.utils.Task;
import com.timwang.mc_tower_defenser.fundation.utils.TaskType;
import com.timwang.mc_tower_defenser.fundation.utils.ai.profession.FarmerProfession;
import net.minecraft.core.BlockPos;

/**
 * 收割任务骨架。
 * 当前不提供任何具体实现。
 */
public class HarvestTask extends Task<FarmerProfession> {
    private BlockPos harvest_target;
    private int half_block = 5;
    public HarvestTask() {
        super("harvest", TaskType.INTERRUPTIBLE);
    }

    @Override
    protected void onTick(FarmerProfession context) {
        if(!context.getServerLevel().isClientSide()) {
            if (harvest_target == null) {
                for(BlockPos pos : BlockPos.betweenClosed(context.getParent().blockPosition().offset(-half_block, -half_block, -half_block), context.getParent().blockPosition().offset(half_block, half_block, half_block))){

                }
            }
        }
    }
}
