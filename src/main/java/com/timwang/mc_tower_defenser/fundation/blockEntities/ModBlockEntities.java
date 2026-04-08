package com.timwang.mc_tower_defenser.fundation.blockEntities;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.blocks.ModBlocks;
import com.timwang.mc_tower_defenser.fundation.blockEntities.Core.UrbanCoreBlockEntities;
import com.timwang.mc_tower_defenser.fundation.blockEntities.WorkBlock.FarmerWorkBlockEntities;
import com.timwang.mc_tower_defenser.fundation.blockEntities.WorkBlock.SoldierWorkBlockEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * 集中注册方块实体类型。
 * 这里把方块逻辑对象和具体方块实例做绑定。
 */
public class ModBlockEntities {
    // 新建一个方块实体注册器
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITYIES = DeferredRegister.create(
            Registries.BLOCK_ENTITY_TYPE , MinecraftTowerDefenser.MODID);

    // 以下是方块实体注册
    public static final Supplier<BlockEntityType<UrbanCoreBlockEntities>> URBAN_CORE = BLOCK_ENTITYIES.register(
            "urban_core",() -> BlockEntityType.Builder.of(UrbanCoreBlockEntities::new, ModBlocks.URBAN_CORE.get()).build(null));
    public static final Supplier<BlockEntityType<FarmerWorkBlockEntities>> FARMER_WORK = BLOCK_ENTITYIES.register(
            "farmer_work", () -> BlockEntityType.Builder.of(FarmerWorkBlockEntities::new, ModBlocks.FARMER_WORK.get()).build(null));
    public static final Supplier<BlockEntityType<SoldierWorkBlockEntities>> SOLDIER_WORK = BLOCK_ENTITYIES.register(
            "soldier_work", () -> BlockEntityType.Builder.of(SoldierWorkBlockEntities::new, ModBlocks.SOLDIER_WORK.get()).build(null));

    /** 挂到模组事件总线，完成方块实体类型注册。 */
    public static void register(IEventBus eventBus){ BLOCK_ENTITYIES.register(eventBus); }

}
