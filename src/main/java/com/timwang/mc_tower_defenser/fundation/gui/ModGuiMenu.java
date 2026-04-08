package com.timwang.mc_tower_defenser.fundation.gui;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.gui.Backend.CreateCountryMenu;
import com.timwang.mc_tower_defenser.fundation.gui.Backend.FarmerWorkMenu;
import com.timwang.mc_tower_defenser.fundation.gui.Backend.SoldierWorkMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 容器菜单类型注册入口。
 * 当前只保留建国菜单的注册点，方便以后切回真正的服务端容器同步方案。
 */
public class ModGuiMenu {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MinecraftTowerDefenser.MODID);
    public static final Supplier<MenuType<CreateCountryMenu>> CREATE_COUNTRY_MENU = MENU_TYPES.register(
            "create_country_menu",
            () -> IMenuTypeExtension.create(CreateCountryMenu::new)
    );
    public static final Supplier<MenuType<FarmerWorkMenu>> FARMER_WORK_MENU = MENU_TYPES.register(
            "farmer_work_menu",
            () -> IMenuTypeExtension.create((containerId, playerInventory, ignored) -> new FarmerWorkMenu(containerId, playerInventory))
    );
    public static final Supplier<MenuType<SoldierWorkMenu>> SOLDIER_WORK_MENU = MENU_TYPES.register(
            "soldier_work_menu",
            () -> IMenuTypeExtension.create((containerId, playerInventory, ignored) -> new SoldierWorkMenu(containerId, playerInventory))
    );

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
