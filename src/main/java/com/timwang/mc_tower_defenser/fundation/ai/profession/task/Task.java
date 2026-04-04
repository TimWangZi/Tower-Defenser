package com.timwang.mc_tower_defenser.fundation.ai.profession.task;

import com.timwang.mc_tower_defenser.fundation.utils.TaskType;

import java.util.Objects;

/**
 * 状态机里的基础状态对象。
 * 每个状态都是一个 Task，进入状态时会重置完成标记，并在每个 tick 执行自身逻辑。
 *
 * @param <C> 状态机运行时上下文
 */
public abstract class Task<C> {
    private final String name;
    private final TaskType type;
    private boolean finished;

    protected Task(TaskType type) {
        this(null, type);
    }

    protected Task(String name, TaskType type) {
        this.name = name == null || name.isBlank() ? getClass().getSimpleName() : name;
        this.type = Objects.requireNonNull(type, "type");
    }

    public final String getName() {
        return name;
    }

    public final TaskType getType() {
        return type;
    }

    public final boolean isFinished() {
        return finished;
    }

    public final void enter(C context) {
        this.finished = false;
        onEnter(context);
    }

    public final void tick(C context) {
        onTick(context);
    }

    public final void exit(C context) {
        onExit(context);
    }

    protected void onEnter(C context) {
    }

    protected abstract void onTick(C context);

    protected void onExit(C context) {
        this.finished = true;
    }

    protected final void finish() {
        this.finished = true;
    }

    @Override
    public String toString() {
        return name;
    }
}
