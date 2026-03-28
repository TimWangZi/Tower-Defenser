package com.timwang.mc_tower_defenser.fundation.network.payloads;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务端回给客户端的国家信息快照。
 * 当 found 为 false 时，nationName 与 towerPositions 都表示“没有可用国家数据”。
 */
public record SyncPlayerNationPayload(String playerName, boolean found, String nationName, List<BlockPos> towerPositions) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncPlayerNationPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MinecraftTowerDefenser.MODID, "sync_player_nation"));
    public static final StreamCodec<ByteBuf, SyncPlayerNationPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SyncPlayerNationPayload::playerName,
            ByteBufCodecs.BOOL,
            SyncPlayerNationPayload::found,
            ByteBufCodecs.STRING_UTF8,
            SyncPlayerNationPayload::nationName,
            ByteBufCodecs.collection(ArrayList::new, BlockPos.STREAM_CODEC),
            SyncPlayerNationPayload::towerPositions,
            SyncPlayerNationPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
