package com.timwang.mc_tower_defenser.fundation.network.test;

import com.timwang.mc_tower_defenser.MinecraftTowerDefenser;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * FarmerWork 测试界面发给服务端的招募农民请求包。
 */
public record FarmerWorkRecruitTestPayload(BlockPos farmerWorkPos) implements CustomPacketPayload {
    public static final Type<FarmerWorkRecruitTestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MinecraftTowerDefenser.MODID, "farmer_work_recruit_test")
    );
    public static final StreamCodec<ByteBuf, FarmerWorkRecruitTestPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            FarmerWorkRecruitTestPayload::farmerWorkPos,
            FarmerWorkRecruitTestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
