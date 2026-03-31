package com.timwang.mc_tower_defenser.fundation.entities;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 实体注册入口。
 * 当前不注册任何自定义实体，保留该入口供后续重新启用实体注册时使用。
 */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MinecraftTowerDefenser.MODID);

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
