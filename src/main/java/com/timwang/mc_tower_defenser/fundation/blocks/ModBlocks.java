package com.timwang.mc_tower_defenser.fundation.blocks;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.blocks.Core.UrbanCoreBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 集中注册本模组的方块。
 * 当前核心内容是 UrbanCore，据此展开国家与领地逻辑。
 */
public class ModBlocks {
    // 注册方块类
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MinecraftTowerDefenser.MODID);

    // UrbanCore 是国家系统里的核心据点方块，对应自定义方块实体和 Geckolib 渲染。
    public static final DeferredBlock<Block> URBAN_CORE = BLOCKS.register(
            "urban_core",
            () -> new UrbanCoreBlock(BlockBehaviour.Properties.of()
                    .destroyTime(2.0f)
                    .explosionResistance(10.0f)
                    .sound(SoundType.GRAVEL)
                    .mapColor(MapColor.STONE).noOcclusion()));

    /** 挂到模组事件总线，完成 DeferredRegister 的正式注册。 */
    public static void register(IEventBus eventBus){BLOCKS.register(eventBus);}
}
