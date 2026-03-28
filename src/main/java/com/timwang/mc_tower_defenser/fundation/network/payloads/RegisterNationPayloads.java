package com.timwang.mc_tower_defenser.fundation.network.payloads;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端发给服务端的建国请求包。
 * 目前携带玩家名和国家名，但服务端实际只信任国家名，玩家身份以连接上下文为准。
 */
public record RegisterNationPayloads(String player_name, String nation_name) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RegisterNationPayloads> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MinecraftTowerDefenser.MODID, "register_nation_payloads"));
    public static final StreamCodec<ByteBuf, RegisterNationPayloads> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            RegisterNationPayloads::player_name,
            ByteBufCodecs.STRING_UTF8,
            RegisterNationPayloads::nation_name,
            RegisterNationPayloads::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
