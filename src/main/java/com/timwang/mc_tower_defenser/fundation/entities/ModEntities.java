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
/**
 * 实体注册入口。
 * 当前仅注册普通士兵实体，并在属性创建事件里补充基础属性表。
 */
@EventBusSubscriber(modid = MinecraftTowerDefenser.MODID)
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MinecraftTowerDefenser.MODID);
    // 普通士兵当前按怪物分类注册，后续如果要受国家系统控制，可以从这里继续扩展。
    public static final Supplier<EntityType<NormalSoldier>> NORMAL_SOLDIER = ENTITIES.register("normal_soldier", ()->EntityType.Builder.of(NormalSoldier::new,MobCategory.MONSTER).build("normal_soldier"));
    public static void register(IEventBus eventBus){ ENTITIES.register(eventBus); }

    /** 为自定义实体补上属性定义，否则实体生成时会缺少属性表。 */
    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(NORMAL_SOLDIER.get(), NormalSoldier.createAttributes().build());
    }

}
