package com.timwang.mc_tower_defenser.fundation.network;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.network.handler.RegisterNationHandler;
import com.timwang.mc_tower_defenser.fundation.network.payloads.RegisterNationPayloads;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = MinecraftTowerDefenser.MODID)
public class ModNetwork {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("b0.1");
        registrar.playBidirectional(RegisterNationPayloads.TYPE ,
                RegisterNationPayloads.CODEC, new DirectionalPayloadHandler<>(
                        RegisterNationHandler::client_handler,
                        RegisterNationHandler::server_handler
                ));
    }
}
