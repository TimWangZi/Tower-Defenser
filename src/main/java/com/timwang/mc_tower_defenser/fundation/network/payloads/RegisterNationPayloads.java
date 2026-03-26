package com.timwang.mc_tower_defenser.fundation.network.payloads;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

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
        return null;
    }
}
