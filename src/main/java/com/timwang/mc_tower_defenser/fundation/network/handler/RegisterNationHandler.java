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
            GlobalNationManager nationManager = GlobalNationManager.get(player.getServer().overworld());
            String playerName = context.player().getGameProfile().getName();
            if (nationManager.hasNation(playerName)) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("你已经拥有国家，无法重复创建。"), true);
                NationSyncService.syncPlayer(player);
                return;
            }

            if (data.nation_name() == null || data.nation_name().isBlank()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("国家名称不能为空。"), true);
                return;
            }

            if (nationManager.getNationByName(data.nation_name()) != null) {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("国家名称已存在。"), true);
                NationSyncService.syncPlayer(player);
                return;
            }

            nationManager.createNation(playerName, data.nation_name());
            NationSyncService.syncPlayer(player);
        });
    }
}
