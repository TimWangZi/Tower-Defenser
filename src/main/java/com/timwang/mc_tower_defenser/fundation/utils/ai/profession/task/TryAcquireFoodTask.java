package com.timwang.mc_tower_defenser.fundation.utils.ai.profession.task;

import com.timwang.mc_tower_defenser.fundation.utils.Task;
import com.timwang.mc_tower_defenser.fundation.utils.TaskType;
import com.timwang.mc_tower_defenser.fundation.utils.ai.profession.FarmerProfession;

/**
 * 尝试获取食物任务骨架。
 * 当前不提供任何具体实现。
 */
public class TryAcquireFoodTask extends Task<FarmerProfession> {
    public TryAcquireFoodTask() {
        super("try_acquire_food", TaskType.INTERRUPTIBLE);
    }

    @Override
    protected void onTick(FarmerProfession context) {
    }
}
