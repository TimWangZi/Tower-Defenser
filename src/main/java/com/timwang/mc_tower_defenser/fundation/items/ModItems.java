package com.timwang.mc_tower_defenser.fundation.items;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MinecraftTowerDefenser.MODID);

    public static void register(IEventBus eventBus){ ITEMS.register(eventBus); }
}
