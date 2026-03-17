package com.timwang.mc_tower_defenser.fundation.blocks;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.blocks.Core.UrbanCore;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModBlocks {
    // 注册方块类
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MinecraftTowerDefenser.MODID);

    public static final DeferredBlock<Block> URBAN_CORE = BLOCKS.register(
            "urban_core",
            () -> new UrbanCore(BlockBehaviour.Properties.of()
                    .destroyTime(2.0f)
                    .explosionResistance(10.0f)
                    .sound(SoundType.GRAVEL)
                    .mapColor(MapColor.STONE).noOcclusion()));

    public static void register(IEventBus eventBus){BLOCKS.register(eventBus);}
}
