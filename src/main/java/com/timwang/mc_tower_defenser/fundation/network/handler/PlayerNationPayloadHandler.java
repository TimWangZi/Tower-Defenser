package com.timwang.mc_tower_defenser.fundation.network.handler;

import com.timwang.mc_tower_defenser.fundation.network.ClientNationState;
import com.timwang.mc_tower_defenser.fundation.network.NationSyncService;
import com.timwang.mc_tower_defenser.fundation.network.payloads.RequestPlayerNationPayload;
import com.timwang.mc_tower_defenser.fundation.network.payloads.SyncPlayerNationPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 当前玩家国家信息查询的处理器。
 * 请求包由客户端发起，响应包只回给请求的那个玩家。
 */
public final class PlayerNationPayloadHandler {
    private PlayerNationPayloadHandler() {
    }

    /** 客户端收到同步包后，更新本地国家缓存。 */
    public static void clientHandler(final SyncPlayerNationPayload data, final IPayloadContext context) {
        context.enqueueWork(() -> ClientNationState.update(
                data.playerName(),
                data.nation().orElse(null)
        ));
    }

    /** 服务端按真实连接玩家查询所属国家，避免信任客户端上传的玩家名。 */
    public static void serverHandler(final RequestPlayerNationPayload data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null || context.player().getServer() == null) {
                return;
            }

            ServerPlayer player = (ServerPlayer) context.player();
            NationSyncService.syncPlayer(player);
        });
    }
}
