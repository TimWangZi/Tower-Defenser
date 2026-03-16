package com.timwang.mc_tower_defenser.fundation.utils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 通用状态机基类：提供状态切换、进入/退出钩子与事件处理。
 * @param <S> 状态类型（推荐使用 enum）
 * @param <E> 事件类型
 */
public class StateMachine<S, E> {
    public interface TransitionGuard<S, E> {
        boolean canTransition(S from, S to, E event);
    }

    private final Map<S, Consumer<E>> onEnter = new ConcurrentHashMap<>();
    private final Map<S, Consumer<E>> onExit = new ConcurrentHashMap<>();
    private final Map<S, Consumer<E>> handlers = new ConcurrentHashMap<>();
    private final TransitionGuard<S, E> guard;

    private volatile S current;

    public StateMachine(S initial, TransitionGuard<S, E> guard) {
        this.current = Objects.requireNonNull(initial);
        this.guard = guard;
    }

    public S getState() {
        return current;
    }

    /** 注册状态进入时的回调。 */
    public StateMachine<S, E> onEnter(S state, Consumer<E> action) {
        onEnter.put(state, action);
        return this;
    }

    /** 注册状态退出时的回调。 */
    public StateMachine<S, E> onExit(S state, Consumer<E> action) {
        onExit.put(state, action);
        return this;
    }

    /** 注册指定状态下的事件处理逻辑。 */
    public StateMachine<S, E> onHandle(S state, Consumer<E> action) {
        handlers.put(state, action);
        return this;
    }

    /** 处理事件；可在回调中调用 transitionTo 进行状态切换。 */
    public synchronized void handle(E event) {
        Consumer<E> handler = handlers.get(current);
        if (handler != null) {
            handler.accept(event);
        }
    }

    /** 显式请求切换状态。返回 true 表示切换成功或已在目标状态。 */
    public synchronized boolean transitionTo(S next, E cause) {
        S from = current;
        if (Objects.equals(from, next)) {
            return true;
        }
        if (guard != null && !guard.canTransition(from, next, cause)) {
            return false;
        }
        Consumer<E> exit = onExit.get(from);
        if (exit != null) {
            exit.accept(cause);
        }

        current = next;

        Consumer<E> enter = onEnter.get(next);
        if (enter != null) {
            enter.accept(cause);
        }
        return true;
    }
}

