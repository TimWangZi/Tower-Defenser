package com.timwang.mc_tower_defenser.fundation.network.payloads;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端请求“当前连接玩家国家信息”的空 payload。
 * 不需要字段，发送这个包本身就代表发起一次查询。
 */
public record RequestPlayerNationPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RequestPlayerNationPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MinecraftTowerDefenser.MODID, "request_player_nation"));
    public static final StreamCodec<ByteBuf, RequestPlayerNationPayload> CODEC =
            StreamCodec.unit(new RequestPlayerNationPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
