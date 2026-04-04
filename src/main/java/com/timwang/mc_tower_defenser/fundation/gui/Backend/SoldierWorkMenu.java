package com.timwang.mc_tower_defenser.fundation.gui.Backend;

import com.timwang.mc_tower_defenser.fundation.blockEntities.WorkBlock.SoldierWorkBlockEntities;
import com.timwang.mc_tower_defenser.fundation.blockEntities.WorkBlock.WorkBlockEntities;
import com.timwang.mc_tower_defenser.fundation.blocks.ModBlocks;
import com.timwang.mc_tower_defenser.fundation.gui.ModGuiMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/**
 * 士兵工作方块菜单。
 * 负责同步 27 格库存，并通过菜单按钮触发服务端招募逻辑。
 */
public class SoldierWorkMenu extends AbstractContainerMenu {
    public static final int RECRUIT_BUTTON_ID = 0;

    private static final int WORK_SLOT_COLUMNS = 9;
    private static final int WORK_SLOT_ROWS = 3;
    private static final int WORK_SLOT_COUNT = WorkBlockEntities.STORAGE_SIZE;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = 27;
    private static final int PLAYER_HOTBAR_SLOT_COUNT = 9;
    private static final int VANILLA_SLOT_COUNT = PLAYER_INVENTORY_SLOT_COUNT + PLAYER_HOTBAR_SLOT_COUNT;
    private static final int TOTAL_SLOT_COUNT = WORK_SLOT_COUNT + VANILLA_SLOT_COUNT;
    private static final int WORK_SLOT_X = 8;
    private static final int WORK_SLOT_Y = 18;
    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 104;
    private static final int HOTBAR_Y = 162;
    private static final int EMERALD_COST = 8;

    private final Container container;
    @Nullable
    private final SoldierWorkBlockEntities soldierWorkBlock;
    private final ContainerLevelAccess access;

    public SoldierWorkMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(WORK_SLOT_COUNT), null, ContainerLevelAccess.NULL);
    }

    public SoldierWorkMenu(int containerId, Inventory playerInventory, SoldierWorkBlockEntities soldierWorkBlock, ContainerLevelAccess access) {
        this(containerId, playerInventory, soldierWorkBlock.getInventory(), soldierWorkBlock, access);
    }

    private SoldierWorkMenu(
            int containerId,
            Inventory playerInventory,
            Container container,
            @Nullable SoldierWorkBlockEntities soldierWorkBlock,
            ContainerLevelAccess access
    ) {
        super(ModGuiMenu.SOLDIER_WORK_MENU.get(), containerId);
        checkContainerSize(container, WORK_SLOT_COUNT);
        this.container = container;
        this.soldierWorkBlock = soldierWorkBlock;
        this.access = access;
        this.container.startOpen(playerInventory.player);

        addWorkSlots();
        addPlayerInventorySlots(playerInventory);
        addPlayerHotbarSlots(playerInventory);
    }

    private void addWorkSlots() {
        for (int row = 0; row < WORK_SLOT_ROWS; row++) {
            for (int column = 0; column < WORK_SLOT_COLUMNS; column++) {
                int slotIndex = column + row * WORK_SLOT_COLUMNS;
                addSlot(new Slot(this.container, slotIndex, WORK_SLOT_X + column * 18, WORK_SLOT_Y + row * 18));
            }
        }
    }

    private void addPlayerInventorySlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int slotIndex = column + row * 9 + 9;
                addSlot(new Slot(playerInventory, slotIndex, PLAYER_INV_X + column * 18, PLAYER_INV_Y + row * 18));
            }
        }
    }

    private void addPlayerHotbarSlots(Inventory playerInventory) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, PLAYER_INV_X + column * 18, HOTBAR_Y));
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != RECRUIT_BUTTON_ID || this.soldierWorkBlock == null || !(player.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        int emeraldCount = 0;
        for (int slot = 0; slot < WORK_SLOT_COUNT; slot++) {
            ItemStack stack = this.container.getItem(slot);
            if (stack.is(Items.EMERALD)) {
                emeraldCount += stack.getCount();
            }
        }
        if (emeraldCount < EMERALD_COST) {
            player.displayClientMessage(Component.literal("招募失败:没有足够绿宝石"), true);
            return false;
        }

        int remaining = EMERALD_COST;
        for (int slot = 0; slot < WORK_SLOT_COUNT && remaining > 0; slot++) {
            ItemStack stack = this.container.getItem(slot);
            if (stack.is(Items.EMERALD)) {
                int consumeCount = Math.min(stack.getCount(), remaining);
                this.container.removeItem(slot, consumeCount);
                remaining -= consumeCount;
            }
        }

        /*if (remaining > 0) {
            player.displayClientMessage(Component.literal("招募失败:扣除绿宝石失败"), true);
            return false;
        }*/
        this.container.setChanged();
        this.broadcastChanges();

        if ((this.soldierWorkBlock.recruitCitizen(serverLevel) == null)) {
            player.displayClientMessage(Component.literal("招募失败:不满足生成条件"), true);
            return false;
        }
        player.displayClientMessage(Component.literal("已招募士兵"), true);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int quickMovedSlotIndex) {
        if (quickMovedSlotIndex < 0 || quickMovedSlotIndex >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot quickMovedSlot = this.slots.get(quickMovedSlotIndex);
        if (quickMovedSlot == null || !quickMovedSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack rawStack = quickMovedSlot.getItem();
        ItemStack copiedStack = rawStack.copy();

        if (quickMovedSlotIndex < WORK_SLOT_COUNT) {
            if (!moveItemStackTo(rawStack, WORK_SLOT_COUNT, TOTAL_SLOT_COUNT, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(rawStack, 0, WORK_SLOT_COUNT, false)) {
            int playerInventoryEnd = WORK_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
            if (quickMovedSlotIndex < playerInventoryEnd) {
                if (!moveItemStackTo(rawStack, playerInventoryEnd, TOTAL_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(rawStack, WORK_SLOT_COUNT, playerInventoryEnd, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (rawStack.isEmpty()) {
            quickMovedSlot.set(ItemStack.EMPTY);
        } else {
            quickMovedSlot.setChanged();
        }

        if (rawStack.getCount() == copiedStack.getCount()) {
            return ItemStack.EMPTY;
        }

        quickMovedSlot.onTake(player, rawStack);
        return copiedStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.SOLDIER_WORK.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}
