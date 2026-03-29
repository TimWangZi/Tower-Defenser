package com.timwang.mc_tower_defenser.fundation.network;

import com.timwang.mc_tower_defenser.fundation.network.payloads.SyncPlayerNationPayload;
import com.timwang.mc_tower_defenser.fundation.system.GlobalNationManager;
import com.timwang.mc_tower_defenser.fundation.system.NationManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;
import java.util.Set;

/**
 * 服务端国家同步工具。
 * 负责把玩家所属国家快照推送给指定玩家或某个国家的全部在线成员。
 */
public final class NationSyncService {
    private NationSyncService() {
    }

    /** 按玩家当前真实归属发送一次国家快照。 */
    public static void syncPlayer(ServerPlayer player) {
        if (player == null || player.getServer() == null) {
            return;
        }

        String playerName = player.getGameProfile().getName();
        NationManager nation = GlobalNationManager.get(player.getServer().overworld())
                .getNationByPlayer(playerName);

        PacketDistributor.sendToPlayer(player, createPayload(playerName, nation));
    }

    /** 把指定国家的最新快照同步给其所有在线成员。 */
    public static void syncNationMembers(MinecraftServer server, NationManager nation) {
        if (server == null || nation == null) {
            return;
        }

        Set<String> memberNames = Set.copyOf(nation.getMemberNames());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (memberNames.contains(player.getGameProfile().getName())) {
                syncPlayer(player);
            }
        }
    }

    /** 按国家名查找在线成员并同步。 */
    public static void syncNationMembers(MinecraftServer server, String nationName) {
        if (server == null || nationName == null || nationName.isBlank()) {
            return;
        }

        GlobalNationManager manager = GlobalNationManager.get(server.overworld());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            NationManager playerNation = manager.getNationByPlayer(player.getGameProfile().getName());
            if (playerNation != null && nationName.equals(playerNation.getNationName())) {
                syncPlayer(player);
            }
        }
    }

    private static SyncPlayerNationPayload createPayload(String playerName, NationManager nation) {
        NationManager snapshot = nation == null ? null : nation.copy();
        return new SyncPlayerNationPayload(playerName, Optional.ofNullable(snapshot));
    }
}
