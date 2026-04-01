package com.timwang.mc_tower_defenser.fundation.items;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.blocks.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 模组物品注册入口。
 * 目前还没有独立物品，后续如果 UrbanCore 需要单独的物品配置，可以在这里补充。
 */
public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MinecraftTowerDefenser.MODID);
    public static final Supplier<BlockItem> URBANCORE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("urban_core", ModBlocks.URBAN_CORE);
    public static final Supplier<BlockItem> FARMER_WORK_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("farmer_work", ModBlocks.FARMER_WORK);


    /** 挂到模组事件总线，完成物品注册。 */
    public static void register(IEventBus eventBus){ ITEMS.register(eventBus); }
}
