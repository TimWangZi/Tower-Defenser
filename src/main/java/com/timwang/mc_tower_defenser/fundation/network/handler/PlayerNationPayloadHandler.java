package com.timwang.mc_tower_defenser.fundation.network.handler;

import com.timwang.mc_tower_defenser.fundation.network.ClientNationState;
import com.timwang.mc_tower_defenser.fundation.network.payloads.RequestPlayerNationPayload;
import com.timwang.mc_tower_defenser.fundation.network.payloads.SyncPlayerNationPayload;
import com.timwang.mc_tower_defenser.fundation.system.GlobalNationManager;
import com.timwang.mc_tower_defenser.fundation.system.NationManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

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
                data.found(),
                data.nationName(),
                data.towerPositions()
        ));
    }

    /** 服务端按真实连接玩家查询所属国家，避免信任客户端上传的玩家名。 */
    public static void serverHandler(final RequestPlayerNationPayload data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null || context.player().getServer() == null) {
                return;
            }

            ServerPlayer player = (ServerPlayer) context.player();
            String playerName = player.getGameProfile().getName();
            NationManager nation = GlobalNationManager.get(player.getServer().overworld())
                    .getNationByPlayer(playerName);
            boolean found = nation != null;
            String nationName = found ? nation.getNationName() : "";
            List<net.minecraft.core.BlockPos> towerPositions = found ? List.copyOf(nation.getTowerPositions()) : List.of();

            PacketDistributor.sendToPlayer(player, new SyncPlayerNationPayload(playerName, found, nationName, towerPositions));
        });
    }
}
