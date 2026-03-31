package com.timwang.mc_tower_defenser.fundation.utils.ai.profession.task;

import com.timwang.mc_tower_defenser.fundation.utils.Task;
import com.timwang.mc_tower_defenser.fundation.utils.TaskType;
import com.timwang.mc_tower_defenser.fundation.utils.ai.profession.FarmerProfession;

/**
 * 逃跑任务骨架。
 * 当前不提供任何具体实现。
 */
public class EscapeTask extends Task<FarmerProfession> {
    public EscapeTask() {
        super("escape", TaskType.INTERRUPTIBLE);
    }

    @Override
    protected void onTick(FarmerProfession context) {
    }
}
