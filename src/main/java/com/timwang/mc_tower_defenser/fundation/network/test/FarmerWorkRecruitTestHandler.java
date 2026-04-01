package com.timwang.mc_tower_defenser.fundation.network.test;

import com.timwang.mc_tower_defenser.fundation.blockEntities.WorkBlock.FarmerWorkBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * FarmerWork 测试招募请求处理器。
 */
public final class FarmerWorkRecruitTestHandler {
    private FarmerWorkRecruitTestHandler() {
    }

    public static void serverHandler(FarmerWorkRecruitTestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            BlockPos targetPos = payload.farmerWorkPos();
            if (!player.level().isLoaded(targetPos) || !player.blockPosition().closerThan(targetPos, 8.0D)) {
                return;
            }

            BlockEntity blockEntity = player.level().getBlockEntity(targetPos);
            if (!(blockEntity instanceof FarmerWorkBlockEntities farmerWorkBlock)) {
                return;
            }

            if (farmerWorkBlock.recruitFarmerCitizen(player.serverLevel()) == null) {
                player.displayClientMessage(Component.literal("[test] 招募失败"), true);
                return;
            }

            player.displayClientMessage(Component.literal("[test] 已招募农民"), true);
        });
    }
}
