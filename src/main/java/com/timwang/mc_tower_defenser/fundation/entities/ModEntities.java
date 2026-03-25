package com.timwang.mc_tower_defenser.fundation.entities;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.entities.Mobs.NormalSoldier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.checkerframework.checker.units.qual.N;

import javax.swing.plaf.PanelUI;
import java.util.function.Supplier;
@EventBusSubscriber(modid = MinecraftTowerDefenser.MODID)
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MinecraftTowerDefenser.MODID);
    public static final Supplier<EntityType<NormalSoldier>> NORMAL_SOLDIER = ENTITIES.register("normal_soldier", ()->EntityType.Builder.of(NormalSoldier::new,MobCategory.MONSTER).build("normal_soldier"));
    public static void register(IEventBus eventBus){ ENTITIES.register(eventBus); }
    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(NORMAL_SOLDIER.get(), NormalSoldier.createAttributes().build());
    }

}
