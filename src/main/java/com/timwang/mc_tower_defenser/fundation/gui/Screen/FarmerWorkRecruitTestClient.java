package com.timwang.mc_tower_defenser.fundation.gui.Screen;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * FarmerWork 测试屏幕客户端打开器。
 * 单独拆出，方便在 common 侧只保留一行调用。
 */
@OnlyIn(Dist.CLIENT)
public final class FarmerWorkRecruitTestClient {
    private FarmerWorkRecruitTestClient() {
    }

    public static void open(BlockPos farmerWorkPos) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new FarmerWorkRecruitTestScreen(farmerWorkPos));
    }
}
