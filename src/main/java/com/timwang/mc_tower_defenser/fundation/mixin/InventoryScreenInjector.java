package com.timwang.mc_tower_defenser.fundation.mixin;

import com.timwang.mc_tower_defenser.fundation.gui.Screen.CreateCountryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class InventoryScreenInjector extends EffectRenderingInventoryScreen<InventoryMenu> implements RecipeUpdateListener{
    public InventoryScreenInjector(InventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Shadow
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {

    }

    void onClickCreateCountryButton() {
        Minecraft.getInstance().setScreen(new CreateCountryScreen(Minecraft.getInstance().screen));
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    protected void injectInventoryScreen(CallbackInfo ci) {
        InventoryScreen screen = (InventoryScreen)(Object) this;
        Button customButton = Button.builder(
                        Component.literal("Create"), // TODO：添加国际化文本组件
                        button -> {
                            onClickCreateCountryButton();
                        }
                )
                .pos(
                        screen.getGuiLeft() + 150,  // X 坐标（相对于背包左上角）
                        screen.getGuiTop() + 10      // Y 坐标
                )
                .size(60, 20) // 按钮宽、高
                .build();
        addRenderableWidget(customButton);
    }


    @Shadow
    public void recipesUpdated() {

    }

    @Shadow
    public RecipeBookComponent getRecipeBookComponent() {
        return null;
    }
}
