package com.timwang.mc_tower_defenser.fundation.network.payloads;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import com.timwang.mc_tower_defenser.fundation.system.NationManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * 服务端回给客户端的国家信息快照。
 * 当 nation 为空时，表示该玩家当前没有可用国家数据。
 */
public record SyncPlayerNationPayload(String playerName, Optional<NationManager> nation) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncPlayerNationPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MinecraftTowerDefenser.MODID, "sync_player_nation"));
    public static final StreamCodec<ByteBuf, SyncPlayerNationPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SyncPlayerNationPayload::playerName,
            NationManager.OPTIONAL_STREAM_CODEC,
            SyncPlayerNationPayload::nation,
            SyncPlayerNationPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
