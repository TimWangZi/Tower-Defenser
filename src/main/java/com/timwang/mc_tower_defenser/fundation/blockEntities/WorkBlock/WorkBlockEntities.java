package com.timwang.mc_tower_defenser.fundation.blockEntities.WorkBlock;

import com.timwang.mc_tower_defenser.fundation.ai.profession.ProfessionBase;
import com.timwang.mc_tower_defenser.fundation.entities.ModEntities;
import com.timwang.mc_tower_defenser.fundation.entities.Mobs.CitizenEntity;
import com.timwang.mc_tower_defenser.fundation.system.GlobalNationManager;
import com.timwang.mc_tower_defenser.fundation.system.NationManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/*
* 工作方块基类
* 包含关系管理、招募与库存流转
* 动画与渲染需要在其子类中实现
* */
public abstract class WorkBlockEntities extends BlockEntity {
    public static final int STORAGE_SIZE = 27;
    private static final String TYPE_TAG = "Type";
    private static final String NATION_TAG = "Nation";

    private String TYPE = "";
    private String NATION = "";
    private final SimpleContainer inventory = new SimpleContainer(STORAGE_SIZE);

    public WorkBlockEntities(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.inventory.addListener(container -> this.setChanged());
    }

    public boolean registerWorkBlock(ServerLevel level, ServerPlayer player, String work_block_type) {
        NationManager nation = GlobalNationManager.get(level).getNationByPlayer(player.getName().getString());
        if (nation != null && nation.isInTerritory(getBlockPos())) {
            nation.registerWorkNode(getBlockPos());
            TYPE = work_block_type;
            NATION = nation.getNationName();
            setChanged();
            return true;
        } else {
            return false;
        }
    }

    public boolean unregisterWorkBlock(ServerLevel level) {
        GlobalNationManager manager = GlobalNationManager.get(level);
        NationManager nation = this.NATION.isBlank() ? manager.getNationByTerritory(getBlockPos()) : manager.getNationByName(this.NATION);
        if (nation == null) {
            return false;
        }

        boolean changed = manager.unregisterWorkNode(nation, getBlockPos());
        if (changed) {
            setChanged();
        }
        return changed;
    }

    public String getTYPE() {
        return TYPE;
    }

    public String getNationName() {
        return NATION;
    }

    public SimpleContainer getInventory() {
        return inventory;
    }

    /**
     * 统一处理市民招募流程，子类只需要提供职业实例。
     */
    @Nullable
    public CitizenEntity recruitCitizen(ServerLevel level) {
        if (level == null || getNationName().isBlank() || !canRecruitCitizen(level)) {
            return null;
        }

        CitizenEntity citizen = ModEntities.CITIZEN.get().create(level);
        if (citizen == null) {
            return null;
        }

        BlockPos spawnPos = resolveCitizenSpawnPos(level);
        citizen.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        if (!level.noCollision(citizen)) {
            return null;
        }

        citizen.bindWorkBlock(getBlockPos());
        citizen.installProfession(createProfession(citizen, level));
        citizen.setPersistenceRequired();
        if (!level.addFreshEntity(citizen)) {
            return null;
        }

        return citizen;
    }

    /**
     * 子类可覆写招募限制，例如人口、资源或科技条件。
     */
    protected boolean canRecruitCitizen(ServerLevel level) {
        return true;
    }

    protected BlockPos resolveCitizenSpawnPos(ServerLevel level) {
        return getBlockPos().above();
    }

    protected abstract ProfessionBase<? extends CitizenEntity, ?> createProfession(CitizenEntity citizen, ServerLevel level);

    /**
     * 从工作方块中提取指定物品并放入目标容器。
     *
     * @return 未能满足的请求列表
     */
    public List<ItemStack> requestItems(Container targetInventory, List<ItemStack> requests) {
        return transferItems(this.inventory, targetInventory, requests);
    }

    /**
     * 向指向当前方块的上游工作方块请求物品，并把物品拉入当前工作方块容器。
     *
     * @return 仍未满足的请求列表
     */
    public List<ItemStack> requestItemsFromWorkBlocks(List<WorkBlockEntities> sourceWorkBlocks, List<ItemStack> requests) {
        List<ItemStack> remainingRequests = copyStacks(requests);
        if (remainingRequests.isEmpty() || sourceWorkBlocks == null || sourceWorkBlocks.isEmpty()) {
            return remainingRequests;
        }

        for (WorkBlockEntities sourceWorkBlock : sourceWorkBlocks) {
            if (sourceWorkBlock == null || sourceWorkBlock == this) {
                continue;
            }

            remainingRequests = transferItems(sourceWorkBlock.inventory, this.inventory, remainingRequests);
            if (remainingRequests.isEmpty()) {
                break;
            }
        }

        return List.copyOf(remainingRequests);
    }

    /**
     * 从来源容器中取出指定物品并投递到工作方块内。
     *
     * @return 未能成功投递的物品列表
     */
    public List<ItemStack> depositItems(Container sourceInventory, List<ItemStack> deliveries) {
        return transferItems(sourceInventory, this.inventory, deliveries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString(TYPE_TAG, this.TYPE);
        tag.putString(NATION_TAG, this.NATION);
        ContainerHelper.saveAllItems(tag, this.inventory.getItems(), registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.TYPE = tag.getString(TYPE_TAG);
        this.NATION = tag.getString(NATION_TAG);
        this.inventory.clearContent();
        ContainerHelper.loadAllItems(tag, this.inventory.getItems(), registries);
    }

    private static List<ItemStack> transferItems(Container source, Container target, List<ItemStack> plan) {
        if (source == null || target == null || plan == null || plan.isEmpty()) {
            return List.of();
        }

        List<ItemStack> leftovers = new ArrayList<>();
        for (ItemStack request : plan) {
            if (request == null || request.isEmpty() || request.getCount() <= 0) {
                continue;
            }

            ItemStack extracted = removeMatchingItems(source, request, request.getCount());
            if (extracted.isEmpty()) {
                leftovers.add(request.copy());
                continue;
            }

            ItemStack remaining = addItem(target, extracted);
            if (!remaining.isEmpty()) {
                addItem(source, remaining.copy());
            }

            int unmetCount = request.getCount() - extracted.getCount() + remaining.getCount();
            if (unmetCount > 0) {
                ItemStack unmet = request.copyWithCount(unmetCount);
                leftovers.add(unmet);
            }
        }

        source.setChanged();
        target.setChanged();
        return List.copyOf(leftovers);
    }

    private static ItemStack removeMatchingItems(Container container, ItemStack template, int amount) {
        if (container == null || template.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack extracted = template.copyWithCount(0);
        int remaining = amount;

        for (int slot = 0; slot < container.getContainerSize() && remaining > 0; slot++) {
            ItemStack current = container.getItem(slot);
            if (current.isEmpty() || !ItemStack.isSameItemSameComponents(current, template)) {
                continue;
            }

            int moveCount = Math.min(current.getCount(), remaining);
            ItemStack removed = container.removeItem(slot, moveCount);
            if (removed.isEmpty()) {
                continue;
            }

            extracted.grow(removed.getCount());
            remaining -= removed.getCount();
        }

        return extracted.isEmpty() ? ItemStack.EMPTY : extracted;
    }

    private static ItemStack addItem(Container container, ItemStack stack) {
        if (container == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack remaining = stack.copy();

        for (int slot = 0; slot < container.getContainerSize() && !remaining.isEmpty(); slot++) {
            ItemStack current = container.getItem(slot);
            if (current.isEmpty() || !ItemStack.isSameItemSameComponents(current, remaining) || !container.canPlaceItem(slot, remaining)) {
                continue;
            }

            int maxCount = Math.min(container.getMaxStackSize(current), current.getMaxStackSize());
            int moveCount = Math.min(remaining.getCount(), maxCount - current.getCount());
            if (moveCount <= 0) {
                continue;
            }

            current.grow(moveCount);
            remaining.shrink(moveCount);
            container.setChanged();
        }

        for (int slot = 0; slot < container.getContainerSize() && !remaining.isEmpty(); slot++) {
            ItemStack current = container.getItem(slot);
            if (!current.isEmpty() || !container.canPlaceItem(slot, remaining)) {
                continue;
            }

            int moveCount = Math.min(remaining.getCount(), container.getMaxStackSize(remaining));
            ItemStack inserted = remaining.copyWithCount(moveCount);
            container.setItem(slot, inserted);
            remaining.shrink(moveCount);
        }

        return remaining.isEmpty() ? ItemStack.EMPTY : remaining;
    }

    private static List<ItemStack> copyStacks(List<ItemStack> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        return items.stream()
                .filter(stack -> stack != null && !stack.isEmpty() && stack.getCount() > 0)
                .map(ItemStack::copy)
                .toList();
    }
}
