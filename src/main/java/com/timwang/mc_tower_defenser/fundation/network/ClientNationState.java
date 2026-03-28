package com.timwang.mc_tower_defenser.fundation.network;

import com.timwang.mc_tower_defenser.fundation.network.payloads.RequestPlayerNationPayload;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * 客户端侧的国家信息缓存。
 * 这里不做持久化，只保存最近一次从服务端同步到的数据，方便 GUI 直接读取。
 */
public final class ClientNationState {
    private static boolean loaded;
    private static boolean hasNation;
    private static String playerName = "";
    private static String nationName = "";
    private static List<BlockPos> towerPositions = List.of();

    private ClientNationState() {
    }

    /** 用服务端同步结果覆盖本地缓存。 */
    public static void update(String playerName, boolean hasNation, String nationName, List<BlockPos> towerPositions) {
        ClientNationState.loaded = true;
        ClientNationState.hasNation = hasNation;
        ClientNationState.playerName = playerName == null ? "" : playerName;
        ClientNationState.nationName = hasNation && nationName != null ? nationName : "";
        ClientNationState.towerPositions = hasNation && towerPositions != null ? List.copyOf(towerPositions) : List.of();
    }

    /** 主动向服务端请求当前玩家所属国家的最新快照。 */
    public static void requestSync() {
        reset();
        PacketDistributor.sendToServer(new RequestPlayerNationPayload());
    }

    /** 清空缓存，通常在重新请求同步前调用。 */
    public static void reset() {
        loaded = false;
        hasNation = false;
        playerName = "";
        nationName = "";
        towerPositions = List.of();
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static boolean hasNation() {
        return loaded && hasNation;
    }

    public static String getPlayerName() {
        return playerName;
    }

    public static String getNationName() {
        return nationName;
    }

    /** 返回最近一次同步到的 UrbanCore 坐标列表。 */
    public static List<BlockPos> getTowerPositions() {
        return towerPositions;
    }
}
