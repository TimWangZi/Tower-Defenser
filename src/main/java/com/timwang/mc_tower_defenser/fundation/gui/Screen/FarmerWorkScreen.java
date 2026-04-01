package com.timwang.mc_tower_defenser.fundation.gui.Screen;

import com.timwang.mc_tower_defenser.fundation.gui.Backend.FarmerWorkMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 农民工作方块正式界面。
 * 使用菜单同步库存，并提供服务端招募按钮。
 */
public class FarmerWorkScreen extends AbstractContainerScreen<FarmerWorkMenu> {
    private static final int SLOT_COLOR = 0xFF4A3D31;
    private static final int PANEL_COLOR = 0xF02A211B;
    private static final int BORDER_COLOR = 0xFF8F7657;
    private static final int TITLE_COLOR = 0xFFF5E9D4;

    public FarmerWorkScreen(FarmerWorkMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 190;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 94;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(
                Button.builder(Component.literal("招募农民"), button -> this.onRecruitClicked())
                        .pos(this.leftPos + 28, this.topPos + 76)
                        .size(120, 20)
                        .build()
        );
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;
        int right = left + this.imageWidth;
        int bottom = top + this.imageHeight;

        guiGraphics.fill(left, top, right, bottom, PANEL_COLOR);
        guiGraphics.fill(left, top, right, top + 1, BORDER_COLOR);
        guiGraphics.fill(left, bottom - 1, right, bottom, BORDER_COLOR);
        guiGraphics.fill(left, top, left + 1, bottom, BORDER_COLOR);
        guiGraphics.fill(right - 1, top, right, bottom, BORDER_COLOR);

        drawSlotGrid(guiGraphics, left, top, 8, 18, 9, 3);
        drawSlotGrid(guiGraphics, left, top, 8, 104, 9, 3);
        drawSlotGrid(guiGraphics, left, top, 8, 162, 9, 1);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, TITLE_COLOR, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, TITLE_COLOR, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void onRecruitClicked() {
        if (this.minecraft == null || this.minecraft.gameMode == null) {
            return;
        }

        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, FarmerWorkMenu.RECRUIT_BUTTON_ID);
    }

    private void drawSlotGrid(GuiGraphics guiGraphics, int left, int top, int startX, int startY, int columns, int rows) {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int x = left + startX + column * 18;
                int y = top + startY + row * 18;
                guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, BORDER_COLOR);
                guiGraphics.fill(x, y, x + 16, y + 16, SLOT_COLOR);
            }
        }
    }
}
