package com.timwang.mc_tower_defenser.fundation.utils;

/**
 * 任务类型决定任务在完成前是否允许被切走。
 */
public enum TaskType {
    /** 可以在尚未完成时被更高优先级或满足条件的边打断。 */
    INTERRUPTIBLE(true),
    /** 必须执行完成后才允许发生状态切换。 */
    BLOCKING(false);

    private final boolean canTransitionBeforeFinished;

    TaskType(boolean canTransitionBeforeFinished) {
        this.canTransitionBeforeFinished = canTransitionBeforeFinished;
    }

    public boolean canTransitionBeforeFinished() {
        return canTransitionBeforeFinished;
    }
}
