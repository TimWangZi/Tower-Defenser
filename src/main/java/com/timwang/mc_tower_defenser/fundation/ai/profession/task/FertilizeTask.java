package com.timwang.mc_tower_defenser.fundation.ai.profession.task;

import com.timwang.mc_tower_defenser.fundation.utils.Task;
import com.timwang.mc_tower_defenser.fundation.utils.TaskType;
import com.timwang.mc_tower_defenser.fundation.ai.profession.FarmerProfession;

/**
 * 施肥任务骨架。
 * 当前不提供任何具体实现。
 */
public class FertilizeTask extends Task<FarmerProfession> {
    public FertilizeTask() {
        super("fertilize", TaskType.INTERRUPTIBLE);
    }

    @Override
    protected void onTick(FarmerProfession context) {
    }
}
