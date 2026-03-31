package com.timwang.mc_tower_defenser.fundation.utils;

/**
 * 传给状态边 lambda 的上下文对象。
 * lambda 可以从这里读取运行时上下文，以及当前 / 目标任务。
 *
 * @param context  状态机运行时上下文，例如 ProfessionBase 子类
 * @param currentTask 当前任务
 * @param nextTask 目标任务
 * @param <C>      状态机运行时上下文类型
 */
public record TransitionContext<C>(C context, Task<C> currentTask, Task<C> nextTask) {
}
