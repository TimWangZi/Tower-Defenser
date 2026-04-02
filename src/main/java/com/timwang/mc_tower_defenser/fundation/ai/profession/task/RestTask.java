package com.timwang.mc_tower_defenser.fundation.ai.profession.task;

import com.timwang.mc_tower_defenser.fundation.utils.Task;
import com.timwang.mc_tower_defenser.fundation.utils.TaskType;
import com.timwang.mc_tower_defenser.fundation.ai.profession.FarmerProfession;

/**
 * 休息任务骨架。
 * 当前不提供任何具体实现。
 */
public class RestTask extends Task<FarmerProfession> {
    public RestTask() {
        super("rest", TaskType.INTERRUPTIBLE);
    }

    @Override
    protected void onTick(FarmerProfession context) {
    }
}
