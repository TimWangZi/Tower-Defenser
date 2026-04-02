package com.timwang.mc_tower_defenser.fundation.ai.profession.task;

import com.timwang.mc_tower_defenser.fundation.blockEntities.WorkBlock.WorkBlockEntities;
import com.timwang.mc_tower_defenser.fundation.entities.Mobs.CitizenEntity;
import com.timwang.mc_tower_defenser.fundation.ai.profession.ProfessionBase;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 工作方块任务回调上下文。
 * 回调可基于当前职业、市民、当前工作方块以及所有上游工作方块决定如何搬运物品。
 */
public record WorkBlockTaskContext<P extends ProfessionBase<CitizenEntity, P>>(
        P profession,
        ServerLevel serverLevel,
        CitizenEntity citizen,
        WorkBlockEntities currentWorkBlock,
        List<WorkBlockEntities> sourceWorkBlocks
) {
    public WorkBlockTaskContext {
        Objects.requireNonNull(profession, "profession");
        Objects.requireNonNull(serverLevel, "serverLevel");
        Objects.requireNonNull(citizen, "citizen");
        Objects.requireNonNull(currentWorkBlock, "currentWorkBlock");
        sourceWorkBlocks = sourceWorkBlocks == null ? List.of() : List.copyOf(sourceWorkBlocks);
    }

    public List<ItemStack> depositItemsToCurrentWorkBlock(List<ItemStack> deliveries) {
        return this.currentWorkBlock.depositItems(this.citizen.getInventory(), deliveries);
    }

    public List<ItemStack> requestItemsIntoCurrentWorkBlock(List<ItemStack> requests) {
        return this.currentWorkBlock.requestItemsFromWorkBlocks(this.sourceWorkBlocks, requests);
    }

    public List<ItemStack> moveItemsFromCurrentWorkBlockToCitizen(List<ItemStack> requests) {
        return this.currentWorkBlock.requestItems(this.citizen.getInventory(), requests);
    }

    public List<ItemStack> getCitizenBackpackItemsSnapshot() {
        SimpleContainer inventory = this.citizen.getInventory();
        List<ItemStack> snapshot = new ArrayList<>();

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            snapshot.add(stack.copy());
        }

        return List.copyOf(snapshot);
    }
}
