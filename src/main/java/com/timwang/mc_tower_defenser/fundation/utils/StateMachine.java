package com.timwang.mc_tower_defenser.fundation.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 基于 Task 的状态机。
 * 每条边都必须显式注册一个 lambda 条件；在同一状态下，按注册顺序检查边，第一条满足条件的边会生效。
 *
 * @param <C> 状态机运行时上下文
 */
public class StateMachine<C> {
    private final Map<Task<C>, List<Transition<C>>> transitions = new LinkedHashMap<>();

    private Task<C> currentTask;
    private boolean started;

    public StateMachine(Task<C> initialTask) {
        this.currentTask = Objects.requireNonNull(initialTask, "initialTask");
        addTask(initialTask);
    }

    public Task<C> getCurrentTask() {
        return currentTask;
    }

    public StateMachine<C> addTask(Task<C> task) {
        transitions.computeIfAbsent(Objects.requireNonNull(task, "task"), key -> new ArrayList<>());
        return this;
    }

    /**
     * 注册一条状态边。
     * from -> to 是否生效，完全由该边对应的 condition 决定。
     */
    public StateMachine<C> addTransition(Task<C> from, Task<C> to, Predicate<TransitionContext<C>> condition) {
        Objects.requireNonNull(condition, "condition");
        addTask(from);
        addTask(to);
        transitions.get(from).add(new Transition<>(from, to, condition));
        return this;
    }

    /**
     * 手动请求切换到指定任务。
     * 只有当前状态存在一条通向 nextTask 的边且其条件满足时，切换才会成功。
     */
    public boolean transitionTo(Task<C> nextTask, C context) {
        Objects.requireNonNull(nextTask, "nextTask");
        Objects.requireNonNull(context, "context");
        ensureStarted(context);

        for (Transition<C> transition : getOutgoingTransitions(currentTask)) {
            if (Objects.equals(transition.to, nextTask) && transition.isTriggered(context, currentTask)) {
                performTransition(transition.to, context);
                return true;
            }
        }
        return false;
    }

    /**
     * 更新当前任务，并在 tick 结束后检查是否有可用边触发转移。
     *
     * @return 本次 tick 是否发生了状态切换
     */
    public boolean tick(C context) {
        Objects.requireNonNull(context, "context");
        ensureStarted(context);

        currentTask.tick(context);

        for (Transition<C> transition : getOutgoingTransitions(currentTask)) {
            if (transition.isTriggered(context, currentTask)) {
                performTransition(transition.to, context);
                return true;
            }
        }
        return false;
    }

    private void ensureStarted(C context) {
        if (!started) {
            currentTask.enter(context);
            started = true;
        }
    }

    private List<Transition<C>> getOutgoingTransitions(Task<C> task) {
        List<Transition<C>> outgoing = transitions.get(task);
        return outgoing == null ? Collections.emptyList() : outgoing;
    }

    private void performTransition(Task<C> nextTask, C context) {
        currentTask.exit(context);
        currentTask = nextTask;
        currentTask.enter(context);
    }

    private static final class Transition<C> {
        private final Task<C> from;
        private final Task<C> to;
        private final Predicate<TransitionContext<C>> condition;

        private Transition(Task<C> from, Task<C> to, Predicate<TransitionContext<C>> condition) {
            this.from = from;
            this.to = to;
            this.condition = condition;
        }

        private boolean isTriggered(C context, Task<C> currentTask) {
            if (!Objects.equals(from, currentTask)) {
                return false;
            }
            if (!currentTask.isFinished() && !currentTask.getType().canTransitionBeforeFinished()) {
                return false;
            }
            return condition.test(new TransitionContext<>(context, currentTask, to));
        }
    }
}
