package com.timwang.mc_tower_defenser.fundation.ai.profession;

import com.timwang.mc_tower_defenser.fundation.ai.profession.task.AttackTask;
import com.timwang.mc_tower_defenser.fundation.ai.profession.task.WalkAroundTask;
import com.timwang.mc_tower_defenser.fundation.ai.profession.task.WorkBlockTask;
import com.timwang.mc_tower_defenser.fundation.ai.profession.task.WorkBlockTaskContext;
import com.timwang.mc_tower_defenser.fundation.entities.Mobs.CitizenEntity;
import com.timwang.mc_tower_defenser.fundation.utils.StateMachine;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.SimpleContainer;

import java.util.Comparator;
import java.util.List;

/**
 * 士兵职业骨架。
 * 当前只提供一个空闲占位状态，后续可在此基础上补战斗、巡逻或驻守状态机。
 */
public class SoldierProfession extends ProfessionBase<CitizenEntity, SoldierProfession> {
    private static final double TEST_WEAPON_PICKUP_RANGE = 1.5D;
    private static final float RETURN_TO_WORK_BLOCK_CHANCE = 0.005F;

    private final AttackTask<SoldierProfession> attackTask = new AttackTask<>();
    private final WorkBlockTask<SoldierProfession> returnToWorkBlockTask = new WorkBlockTask<SoldierProfession>("soldier_return_to_work_block")
            .setDeliverItemsCallback(this::deliverItemsAtWorkBlock);
    private final WalkAroundTask<SoldierProfession> walkAroundTask = new WalkAroundTask<>();

    public SoldierProfession(CitizenEntity parent, ServerLevel serverLevel) {
        super(parent, serverLevel);
    }
    @Override
    protected StateMachine<SoldierProfession> createStateMachine() {
        return new StateMachine<>(this.walkAroundTask)
                .addTask(this.attackTask)
                .addTask(this.returnToWorkBlockTask)
                // 平时巡逻；发现敌人后切入追踪状态。
                .addTransition(this.walkAroundTask, this.attackTask, transition -> this.attackTask.hasEnemies(this))
                // 仅在随机移动阶段按概率/背包满回工作方块卸货；战斗阶段不接这条边。
                .addTransition(this.walkAroundTask, this.returnToWorkBlockTask, transition -> shouldReturnToWorkBlock())
                .addTransition(this.returnToWorkBlockTask, this.walkAroundTask, transition -> this.returnToWorkBlockTask.isFinished())
                // 追踪任务自行在目标丢失或无法继续时 finish，再回到巡逻。
                .addTransition(this.attackTask, this.walkAroundTask, transition -> this.attackTask.isFinished());
    }
    @Override
    public String getProfessionTypeId() {
        return CitizenProfessionTypes.SOLDIER;
    }

    private boolean shouldReturnToWorkBlock() {
        CitizenEntity citizen = getParent();
        if (!hasBackpackItems(citizen)) {
            return false;
        }

        if (citizen.isBackpackFull()) {
            return true;
        }

        return citizen.getRandom().nextFloat() < RETURN_TO_WORK_BLOCK_CHANCE;
    }

    private boolean hasBackpackItems(CitizenEntity citizen) {
        SimpleContainer inventory = citizen.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (!inventory.getItem(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void deliverItemsAtWorkBlock(WorkBlockTaskContext<SoldierProfession> context) {
        List<ItemStack> backpackItems = context.getCitizenBackpackItemsSnapshot();
        if (backpackItems.isEmpty()) {
            return;
        }

        // 只投递背包内容；主手武器仍保留在士兵手上。
        context.depositItemsToCurrentWorkBlock(backpackItems);
    }

    @Override
    protected void afterStateMachineTick() {
        super.afterStateMachineTick();
        // [test-only] 临时测试逻辑：士兵会从脚边拾取掉落物；
        // 武器进主手，非武器且非工具的物品尝试塞进背包。
        tryEquipNearbyTestWeapon();
        tryStoreNearbyNonWeaponItem();
    }

    private void tryEquipNearbyTestWeapon() {
        CitizenEntity citizen = getParent();
        if (!citizen.getMainHandItem().isEmpty()) {
            return;
        }

        ItemEntity nearestWeapon = getServerLevel().getEntitiesOfClass(
                        ItemEntity.class,
                        citizen.getBoundingBox().inflate(TEST_WEAPON_PICKUP_RANGE),
                        this::isTestWeaponPickup
                ).stream()
                .min(Comparator.comparingDouble(citizen::distanceToSqr))
                .orElse(null);

        if (nearestWeapon == null) {
            return;
        }

        ItemStack pickedWeapon = takeSingleItem(nearestWeapon);
        if (pickedWeapon.isEmpty()) {
            return;
        }

        citizen.setRightHandItem(pickedWeapon);
    }

    private boolean isTestWeaponPickup(ItemEntity itemEntity) {
        if (itemEntity == null || !itemEntity.isAlive() || itemEntity.hasPickUpDelay()) {
            return false;
        }

        return isTestWeapon(itemEntity.getItem());
    }

    private void tryStoreNearbyNonWeaponItem() {
        CitizenEntity citizen = getParent();
        if (citizen.isBackpackFull()) {
            return;
        }

        ItemEntity nearestItem = getServerLevel().getEntitiesOfClass(
                        ItemEntity.class,
                        citizen.getBoundingBox().inflate(TEST_WEAPON_PICKUP_RANGE),
                        this::isTestBackpackPickup
                ).stream()
                .min(Comparator.comparingDouble(citizen::distanceToSqr))
                .orElse(null);

        if (nearestItem == null) {
            return;
        }

        storeItemEntityToBackpack(citizen, nearestItem);
    }

    private boolean isTestBackpackPickup(ItemEntity itemEntity) {
        if (itemEntity == null || !itemEntity.isAlive() || itemEntity.hasPickUpDelay()) {
            return false;
        }

        ItemStack stack = itemEntity.getItem();
        return !stack.isEmpty() && !isTestWeapon(stack);
    }

    private boolean isTestWeapon(ItemStack stack) {
        // [test-only] 临时把“剑类物品”和“带 TOOL 组件的物品”都视为可拾取武器；
        // 正式版本应改成 item tag 或独立装备系统，而不是继续用硬编码规则。
        return !stack.isEmpty() && (stack.getItem() instanceof SwordItem || stack.has(DataComponents.TOOL));
    }

    private ItemStack takeSingleItem(ItemEntity itemEntity) {
        ItemStack groundStack = itemEntity.getItem();
        if (groundStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack picked = groundStack.copyWithCount(1);
        ItemStack remaining = groundStack.copy();
        remaining.shrink(1);

        if (remaining.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(remaining);
        }

        return picked;
    }

    private void storeItemEntityToBackpack(CitizenEntity citizen, ItemEntity itemEntity) {
        ItemStack groundStack = itemEntity.getItem();
        if (groundStack.isEmpty()) {
            return;
        }

        // [test-only] 临时直接把整堆非武器掉落物塞进背包；放不下的部分继续留在地上。
        ItemStack leftover = citizen.addItemToBackpack(groundStack.copy());
        if (leftover.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(leftover);
        }
    }

}
