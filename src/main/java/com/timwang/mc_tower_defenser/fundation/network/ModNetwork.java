package com.timwang.mc_tower_defenser.fundation.network;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.network.handler.PlayerNationPayloadHandler;
import com.timwang.mc_tower_defenser.fundation.network.handler.RegisterNationHandler;
import com.timwang.mc_tower_defenser.fundation.network.payloads.RequestPlayerNationPayload;
import com.timwang.mc_tower_defenser.fundation.network.payloads.RegisterNationPayloads;
import com.timwang.mc_tower_defenser.fundation.network.payloads.SyncPlayerNationPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 自定义网络包注册入口。
 * 当前负责国家创建请求，以及客户端查询当前玩家国家信息这两条链路。
 */
public class ModNetwork {
    /** 在模组事件总线触发时注册全部 payload 与处理器。 */
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("b0.2");
        registrar.playBidirectional(RegisterNationPayloads.TYPE ,
                RegisterNationPayloads.CODEC, new DirectionalPayloadHandler<>(
                        RegisterNationHandler::client_handler,
                        RegisterNationHandler::server_handler
                ));
        registrar.playToServer(RequestPlayerNationPayload.TYPE,
                RequestPlayerNationPayload.CODEC,
                PlayerNationPayloadHandler::serverHandler);
        registrar.playToClient(SyncPlayerNationPayload.TYPE,
                SyncPlayerNationPayload.CODEC,
                PlayerNationPayloadHandler::clientHandler);
    }
}
