package com.timwang.mc_tower_defenser.fundation.gui;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.gui.Backend.CreateCountryMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModGuiMenu {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MinecraftTowerDefenser.MODID);
    public static final Supplier<MenuType<CreateCountryMenu>> CREATE_COUNTRY_MENU = MENU_TYPES.register(
            "create_country_menu",
            () -> IMenuTypeExtension.create(CreateCountryMenu::new)
    );
}
