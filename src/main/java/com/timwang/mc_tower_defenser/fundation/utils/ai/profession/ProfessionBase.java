package com.timwang.mc_tower_defenser.fundation.utils.ai.profession;

import com.timwang.mc_tower_defenser.fundation.utils.StateMachine;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;

import java.util.Objects;

/**
 * 职业系统基类。
 * 职业在每个服务端 tick 中驱动自身状态机，让 Task 负责行为，Condition 负责状态边判断。
 *
 * @param <T> 该职业依附的实体类型
 * @param <P> 具体职业自身类型
 */
public abstract class ProfessionBase<T extends PathfinderMob, P extends ProfessionBase<T, P>> {
    private ServerLevel serverLevel;
    private final T parent;
    private StateMachine<P> stateMachine;

    protected ProfessionBase(T parent, ServerLevel serverLevel) {
        this.parent = Objects.requireNonNull(parent, "parent");
        this.serverLevel = serverLevel;
    }

    protected abstract StateMachine<P> createStateMachine();

    /**
     * 此tick非彼tick,此处是Goal tick
     * 这里只在服务端执行，客户端不维护职业状态。
     */
    public final void tick() {
        if (!(parent.level() instanceof ServerLevel level)) {
            return;
        }

        this.serverLevel = level;
        getStateMachine().tick(self());
        afterStateMachineTick();
    }

    protected void afterStateMachineTick() {
    }

    public final ServerLevel getServerLevel() {
        return Objects.requireNonNull(serverLevel, "Profession tick must run before accessing serverLevel");
    }

    public final T getParent() {
        return parent;
    }

    public final StateMachine<P> getStateMachine() {
        if (stateMachine == null) {
            stateMachine = Objects.requireNonNull(createStateMachine(), "createStateMachine()");
        }
        return stateMachine;
    }

    @SuppressWarnings("unchecked")
    protected final P self() {
        return (P) this;
    }
}
