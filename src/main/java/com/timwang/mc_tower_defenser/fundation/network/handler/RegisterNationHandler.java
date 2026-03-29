package com.timwang.mc_tower_defenser.fundation.network.handler;

import com.timwang.mc_tower_defenser.fundation.network.NationSyncService;
import com.timwang.mc_tower_defenser.fundation.network.payloads.RegisterNationPayloads;
import com.timwang.mc_tower_defenser.fundation.system.GlobalNationManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 建国请求处理器。
 * 当前只有服务端方向真正生效，客户端方向预留给以后做结果提示。
 */
public class RegisterNationHandler {
    public static void client_handler(final RegisterNationPayloads data, final IPayloadContext context) {

    }

    /** 服务端收到建国请求后，以真实玩家名创建国家并写入全局管理器。 */
    public static void server_handler(final RegisterNationPayloads data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null || context.player().getServer() == null) {
                return;
            }

            ServerPlayer player = (ServerPlayer) context.player();
            GlobalNationManager.get(player.getServer().overworld())
                    .createNation(context.player().getGameProfile().getName(), data.nation_name());
            NationSyncService.syncPlayer(player);
        });
    }
}
