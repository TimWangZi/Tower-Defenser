package com.timwang.mc_tower_defenser.fundation.gui.Screen;

import com.timwang.mc_tower_defenser.fundation.network.test.FarmerWorkRecruitTestPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * FarmerWork 方块的临时测试招募界面。
 * 只提供一个“招募农民”按钮，方便后续整组移除。
 */
@OnlyIn(Dist.CLIENT)
public class FarmerWorkRecruitTestScreen extends Screen {
    private static final int PANEL_WIDTH = 180;
    private static final int PANEL_HEIGHT = 90;

    private final BlockPos farmerWorkPos;
    private int leftPos;
    private int topPos;

    public FarmerWorkRecruitTestScreen(BlockPos farmerWorkPos) {
        super(Component.literal("Farmer Work Test"));
        this.farmerWorkPos = farmerWorkPos.immutable();
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - PANEL_WIDTH) / 2;
        this.topPos = (this.height - PANEL_HEIGHT) / 2;
        this.clearWidgets();

        this.addRenderableWidget(
                Button.builder(Component.literal("招募农民"), button -> this.onRecruitClicked())
                        .pos(this.leftPos + 30, this.topPos + 40)
                        .size(120, 20)
                        .build()
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + PANEL_WIDTH, this.topPos + PANEL_HEIGHT, 0xDD202020);
        guiGraphics.drawCenteredString(this.font, "Farmer Work Test", this.leftPos + PANEL_WIDTH / 2, this.topPos + 16, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void onRecruitClicked() {
        PacketDistributor.sendToServer(new FarmerWorkRecruitTestPayload(this.farmerWorkPos));
        this.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
