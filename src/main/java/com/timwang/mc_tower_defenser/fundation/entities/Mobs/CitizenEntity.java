package com.timwang.mc_tower_defenser.fundation.entities.Mobs;

import com.timwang.mc_tower_defenser.fundation.utils.ai.goal.CitizenGoal;
import com.timwang.mc_tower_defenser.fundation.utils.ai.profession.ProfessionBase;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Objects;

public class CitizenEntity extends PathfinderMob implements InventoryCarrier {
    private static final int BACKPACK_SIZE = 27;
    private static final String WORK_BLOCK_POS_TAG = "WorkBlockPos";

    protected float walking_speed = 1f;
    private final SimpleContainer inventory = new SimpleContainer(BACKPACK_SIZE);
    @Nullable
    private ProfessionBase<? extends CitizenEntity, ?> profession;
    @Nullable
    private BlockPos boundWorkBlockPos;

    protected CitizenEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new CitizenGoal(this));
    }

    public void installProfession(ProfessionBase<? extends CitizenEntity, ?> profession) {
        ProfessionBase<? extends CitizenEntity, ?> nextProfession = Objects.requireNonNull(profession, "profession");
        if (nextProfession.getParent() != this) {
            throw new IllegalArgumentException("Profession must be created for this citizen instance");
        }
        this.profession = nextProfession;
    }

    public void clearProfession() {
        this.profession = null;
    }

    @Nullable
    public ProfessionBase<? extends CitizenEntity, ?> getProfession() {
        return this.profession;
    }

    public boolean hasProfession() {
        return this.profession != null;
    }

    @Override
    public SimpleContainer getInventory() {
        return inventory;
    }

    public ItemStack getRightHandItem() {
        return getMainHandItem();
    }

    public void setRightHandItem(ItemStack stack) {
        setItemInHand(InteractionHand.MAIN_HAND, stack);
    }

    public ItemStack getLeftHandItem() {
        return getOffhandItem();
    }

    public void setLeftHandItem(ItemStack stack) {
        setItemInHand(InteractionHand.OFF_HAND, stack);
    }

    public ItemStack addItemToBackpack(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return inventory.addItem(stack);
    }

    public boolean isBackpackFull() {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                return false;
            }
            if (stack.getCount() < Math.min(inventory.getMaxStackSize(stack), stack.getMaxStackSize())) {
                return false;
            }
        }
        return true;
    }

    public boolean hasNationBelongTo() {
        return true;
    }

    public void bindWorkBlock(BlockPos pos) {
        this.boundWorkBlockPos = pos == null ? null : pos.immutable();
    }

    public void clearBoundWorkBlock() {
        this.boundWorkBlockPos = null;
    }

    @Nullable
    public BlockPos getBoundWorkBlockPos() {
        return this.boundWorkBlockPos == null ? null : this.boundWorkBlockPos.immutable();
    }

    public boolean hasBoundWorkBlock() {
        return this.boundWorkBlockPos != null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        writeInventoryToTag(tag, this.registryAccess());
        if (this.boundWorkBlockPos != null) {
            tag.putLong(WORK_BLOCK_POS_TAG, this.boundWorkBlockPos.asLong());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        readInventoryFromTag(tag, this.registryAccess());
        this.boundWorkBlockPos = tag.contains(WORK_BLOCK_POS_TAG) ? BlockPos.of(tag.getLong(WORK_BLOCK_POS_TAG)) : null;
    }

    public float getWalkingSpeed(){return walking_speed;}
    public void setWalkingSpeed(float speed){walking_speed = speed;}
}
