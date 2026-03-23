package com.timwang.mc_tower_defenser.fundation.gui.Backend;

import com.timwang.mc_tower_defenser.fundation.gui.ModGuiMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.units.qual.C;

public class CreateCountryMenu extends AbstractContainerMenu {
    // 客户端构造函数
    public CreateCountryMenu(int syncId, Inventory playerInventory, FriendlyByteBuf packetByteBuf){
        this(syncId, playerInventory);
    }

    // 服务端构造函数
    public CreateCountryMenu(int syncId, Inventory playerInventory/*在此处放入参数*/){
        super(ModGuiMenu.CREATE_COUNTRY_MENU.get(), syncId);
    }
    // 没有物品，所以无需快速移动
    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    // 始终保持创建菜单的打开
    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
