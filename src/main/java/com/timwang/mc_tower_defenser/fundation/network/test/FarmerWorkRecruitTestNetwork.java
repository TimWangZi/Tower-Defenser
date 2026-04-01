package com.timwang.mc_tower_defenser.fundation.network.test;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * FarmerWork 临时测试网络注册入口。
 * 仅服务于测试招募界面，后续可整类删除。
 */
@SuppressWarnings("removal")
@EventBusSubscriber(modid = MinecraftTowerDefenser.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class FarmerWorkRecruitTestNetwork {
    private FarmerWorkRecruitTestNetwork() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("farmer_work_test");
        registrar.playToServer(
                FarmerWorkRecruitTestPayload.TYPE,
                FarmerWorkRecruitTestPayload.CODEC,
                FarmerWorkRecruitTestHandler::serverHandler
        );
    }
}
