package com.timwang.mc_tower_defenser.fundation.utils.ai.profession;

import com.timwang.mc_tower_defenser.fundation.entities.Mobs.CitizenEntity;
import com.timwang.mc_tower_defenser.fundation.utils.StateMachine;
import com.timwang.mc_tower_defenser.fundation.utils.ai.profession.task.HarvestTask;
import com.timwang.mc_tower_defenser.fundation.utils.ai.profession.task.TryAcquireFoodTask;
import com.timwang.mc_tower_defenser.fundation.utils.ai.profession.task.WorkBlockTaskContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * 农民职业状态机。
 * 默认流程为：采摘 -> 返回工作方块卸货/补给 -> 继续采摘。
 */
public class FarmerProfession extends ProfessionBase<CitizenEntity, FarmerProfession> {
    private final HarvestTask harvestTask;
    private final TryAcquireFoodTask returnToWorkBlockTask;

    public FarmerProfession(CitizenEntity parent, ServerLevel serverLevel) {
        super(parent, serverLevel);
        this.harvestTask = new HarvestTask();
        this.returnToWorkBlockTask = new TryAcquireFoodTask()
                .setDeliverItemsCallback(this::deliverItemsAtWorkBlock)
                .setRequestItemsCallback(this::requestItemsAtWorkBlock);
    }

    @Override
    public String getProfessionTypeId() {
        return CitizenProfessionTypes.FARMER;
    }

    @Override
    protected StateMachine<FarmerProfession> createStateMachine() {
        return new StateMachine<>(this.harvestTask)
                .addTransition(this.harvestTask, this.returnToWorkBlockTask, transition -> this.harvestTask.isFinished())
                .addTransition(this.returnToWorkBlockTask, this.harvestTask, transition -> this.returnToWorkBlockTask.isFinished());
    }

    protected HarvestTask getHarvestTask() {
        return this.harvestTask;
    }

    protected TryAcquireFoodTask getReturnToWorkBlockTask() {
        return this.returnToWorkBlockTask;
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
}
