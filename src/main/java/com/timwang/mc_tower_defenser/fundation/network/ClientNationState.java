package com.timwang.mc_tower_defenser.fundation.network;

import com.timwang.mc_tower_defenser.fundation.network.payloads.RequestPlayerNationPayload;
import com.timwang.mc_tower_defenser.fundation.system.NationManager;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * 客户端侧的国家信息缓存。
 * 这里不做持久化，只保存最近一次从服务端同步到的数据，方便 GUI 直接读取。
 */
public final class ClientNationState {
    private static boolean loaded;
    private static String playerName = "";
    private static NationManager nation;

    private ClientNationState() {
    }

    /** 用服务端同步结果覆盖本地缓存。 */
    public static void update(String playerName, NationManager nation) {
        ClientNationState.loaded = true;
        ClientNationState.playerName = playerName == null ? "" : playerName;
        ClientNationState.nation = nation == null ? null : nation.copy();
    }

    /** 主动向服务端请求当前玩家所属国家的最新快照。 */
    public static void requestSync() {
        reset();
        PacketDistributor.sendToServer(new RequestPlayerNationPayload());
    }

    /**
     * 预留给未来的“客户端提交国家修改”入口。
     * 当前版本仍以服务端权威写入为准，因此这里暂不发送任何数据包。
     */
    public static void requestNationChange(NationManager updatedNation) {
        // Reserved for future client -> server mutation packets.
    }

    /** 清空缓存，通常在重新请求同步前调用。 */
    public static void reset() {
        loaded = false;
        playerName = "";
        nation = null;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static boolean hasNation() {
        return loaded && nation != null;
    }

    public static String getPlayerName() {
        return playerName;
    }

    /** 返回最近一次同步到的国家快照副本。 */
    public static NationManager getNation() {
        return nation == null ? null : nation.copy();
    }

    public static String getNationName() {
        return nation == null ? "" : nation.getNationName();
    }

    /** 返回最近一次同步到的 UrbanCore 坐标列表。 */
    public static List<BlockPos> getTowerPositions() {
        return nation == null ? List.of() : nation.getTowerPositions();
    }
}
