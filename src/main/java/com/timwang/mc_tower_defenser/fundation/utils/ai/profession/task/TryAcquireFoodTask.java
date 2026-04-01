package com.timwang.mc_tower_defenser.fundation.utils.ai.profession.task;

import com.timwang.mc_tower_defenser.fundation.utils.ai.profession.FarmerProfession;

import java.util.function.Consumer;

/**
 * 兼容原有命名的工作方块交互任务。
 * 具体请求/投递内容通过 WorkBlockTask 提供的配置函数决定。
 */
public class TryAcquireFoodTask extends WorkBlockTask<FarmerProfession> {
    public TryAcquireFoodTask() {
        super("try_acquire_food");
    }

    @Override
    public TryAcquireFoodTask setRequestItemsCallback(Consumer<WorkBlockTaskContext<FarmerProfession>> callback) {
        super.setRequestItemsCallback(callback);
        return this;
    }

    @Override
    public TryAcquireFoodTask setDeliverItemsCallback(Consumer<WorkBlockTaskContext<FarmerProfession>> callback) {
        super.setDeliverItemsCallback(callback);
        return this;
    }
}
