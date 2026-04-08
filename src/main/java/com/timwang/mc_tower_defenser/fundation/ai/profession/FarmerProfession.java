package com.timwang.mc_tower_defenser.fundation.ai.profession;

import com.timwang.mc_tower_defenser.fundation.entities.Mobs.CitizenEntity;
import com.timwang.mc_tower_defenser.fundation.utils.StateMachine;
import com.timwang.mc_tower_defenser.fundation.ai.profession.task.EscapeTask;
import com.timwang.mc_tower_defenser.fundation.ai.profession.task.HarvestTask;
import com.timwang.mc_tower_defenser.fundation.ai.profession.task.WalkAroundTask;
import com.timwang.mc_tower_defenser.fundation.ai.profession.task.WorkBlockTask;
import com.timwang.mc_tower_defenser.fundation.ai.profession.task.WorkBlockTaskContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 农民职业状态机。
 * 默认流程为：采摘 -> 返回工作方块卸货/补给 -> 随机闲逛或继续采摘。
 */
public class FarmerProfession extends ProfessionBase<CitizenEntity, FarmerProfession> {
    private static final float WALK_AROUND_CHANCE = 0.35F;

    private final HarvestTask harvestTask;
    private final WorkBlockTask<FarmerProfession> returnToWorkBlockTask;
    private final WalkAroundTask<FarmerProfession> walkAroundTask;
    private final EscapeTask escapeTask;
    private int lastConsumedHurtByMobTimestamp = -1;

    public FarmerProfession(CitizenEntity parent, ServerLevel serverLevel) {
        super(parent, serverLevel);
        this.harvestTask = new HarvestTask();
        this.returnToWorkBlockTask = new WorkBlockTask<FarmerProfession>("try_acquire_food")
                .setDeliverItemsCallback(this::deliverItemsAtWorkBlock)
                .setRequestItemsCallback(this::requestItemsAtWorkBlock);
        this.walkAroundTask = new WalkAroundTask<>();
        this.escapeTask = new EscapeTask();
    }

    @Override
    public String getProfessionTypeId() {
        return CitizenProfessionTypes.FARMER;
    }

    @Override
    protected StateMachine<FarmerProfession> createStateMachine() {
        return new StateMachine<>(this.harvestTask)
                .addTask(this.returnToWorkBlockTask)
                .addTask(this.walkAroundTask)
                .addTask(this.escapeTask)
                .addTransition(this.harvestTask, this.escapeTask, transition -> shouldEscape())
                .addTransition(this.harvestTask, this.returnToWorkBlockTask, transition -> this.harvestTask.isFinished())
                .addTransition(this.returnToWorkBlockTask, this.escapeTask, transition -> shouldEscape())
                .addTransition(this.returnToWorkBlockTask, this.walkAroundTask, transition -> this.returnToWorkBlockTask.isFinished() && shouldWalkAround())
                .addTransition(this.returnToWorkBlockTask, this.harvestTask, transition -> this.returnToWorkBlockTask.isFinished())
                .addTransition(this.walkAroundTask, this.escapeTask, transition -> shouldEscape())
                .addTransition(this.walkAroundTask, this.returnToWorkBlockTask, transition -> this.walkAroundTask.isFinished())
                .addTransition(this.escapeTask, this.returnToWorkBlockTask, transition -> this.escapeTask.isFinished());
    }

    protected HarvestTask getHarvestTask() {
        return this.harvestTask;
    }

    protected WorkBlockTask<FarmerProfession> getReturnToWorkBlockTask() {
        return this.returnToWorkBlockTask;
    }

    protected WalkAroundTask<FarmerProfession> getWalkAroundTask() {
        return this.walkAroundTask;
    }

    protected EscapeTask getEscapeTask() {
        return this.escapeTask;
    }

    protected boolean shouldWalkAround() {
        return this.getParent().getRandom().nextFloat() < WALK_AROUND_CHANCE;
    }

    /**
     * 返回工作方块时默认先卸下背包物资。
     * 如需保留某些物品不投递，可覆写此方法。
     */
    protected List<ItemStack> createReturnDeliveryPlan(WorkBlockTaskContext<FarmerProfession> context) {
        return context.getCitizenBackpackItemsSnapshot();
    }

    /**
     * 返回工作方块时默认补给内容。
     * 可在子类中修改为更合适的工具或食物。
     */
    protected List<ItemStack> createDesiredReturnSupplies(WorkBlockTaskContext<FarmerProfession> context) {
        return List.of(
                new ItemStack(Items.WOODEN_HOE, 1),
                new ItemStack(Items.BREAD, 2)
        );
    }

    protected void deliverItemsAtWorkBlock(WorkBlockTaskContext<FarmerProfession> context) {
        context.depositItemsToCurrentWorkBlock(createReturnDeliveryPlan(context));
    }

    protected void requestItemsAtWorkBlock(WorkBlockTaskContext<FarmerProfession> context) {
        List<ItemStack> requestPlan = createMissingSupplyRequests(context, createDesiredReturnSupplies(context));
        if (requestPlan.isEmpty()) {
            return;
        }

        context.requestItemsIntoCurrentWorkBlock(requestPlan);
        context.moveItemsFromCurrentWorkBlockToCitizen(requestPlan);
    }

    protected List<ItemStack> createMissingSupplyRequests(WorkBlockTaskContext<FarmerProfession> context, List<ItemStack> desiredSupplies) {
        if (desiredSupplies == null || desiredSupplies.isEmpty()) {
            return List.of();
        }

        List<ItemStack> requests = new ArrayList<>();
        for (ItemStack desired : desiredSupplies) {
            if (desired == null || desired.isEmpty() || desired.getCount() <= 0) {
                continue;
            }

            int carriedCount = countCarriedItems(context.getCitizenBackpackItemsSnapshot(), context.citizen(), desired);
            int missingCount = desired.getCount() - carriedCount;
            if (missingCount > 0) {
                requests.add(desired.copyWithCount(missingCount));
            }
        }

        return List.copyOf(requests);
    }

    private int countCarriedItems(List<ItemStack> backpackItems, CitizenEntity citizen, ItemStack template) {
        int count = 0;

        for (ItemStack stack : backpackItems) {
            if (ItemStack.isSameItemSameComponents(stack, template)) {
                count += stack.getCount();
            }
        }

        ItemStack mainHand = citizen.getMainHandItem();
        if (ItemStack.isSameItemSameComponents(mainHand, template)) {
            count += mainHand.getCount();
        }

        ItemStack offHand = citizen.getOffhandItem();
        if (ItemStack.isSameItemSameComponents(offHand, template)) {
            count += offHand.getCount();
        }

        return count;
    }

    protected boolean shouldEscape() {
        return resolvePendingEscapeSourcePos() != null;
    }

    @Nullable
    public Vec3 consumeEscapeSourcePos() {
        LivingEntity attacker = getParent().getLastHurtByMob();
        if (attacker == null) {
            return null;
        }

        int hurtTimestamp = getParent().getLastHurtByMobTimestamp();
        if (hurtTimestamp <= this.lastConsumedHurtByMobTimestamp) {
            return null;
        }

        this.lastConsumedHurtByMobTimestamp = hurtTimestamp;
        return attacker.position();
    }

    @Nullable
    private Vec3 resolvePendingEscapeSourcePos() {
        LivingEntity attacker = getParent().getLastHurtByMob();
        if (attacker == null) {
            return null;
        }

        int hurtTimestamp = getParent().getLastHurtByMobTimestamp();
        if (hurtTimestamp <= this.lastConsumedHurtByMobTimestamp) {
            return null;
        }

        return attacker.position();
    }
}
