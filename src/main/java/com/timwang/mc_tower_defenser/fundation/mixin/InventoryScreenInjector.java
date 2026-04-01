package com.timwang.mc_tower_defenser.fundation.mixin;

import com.timwang.mc_tower_defenser.fundation.gui.Screen.CreateCountryScreen;
import com.timwang.mc_tower_defenser.fundation.network.ClientNationState;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 背包界面注入器。
 * 在原版背包里插入一个按钮，作为打开建国界面的入口。
 */
@Mixin(InventoryScreen.class)
public class InventoryScreenInjector extends EffectRenderingInventoryScreen<InventoryMenu> implements RecipeUpdateListener{
    @Unique
    private Button minecraftTowerDefenser$createCountryButton;

    public InventoryScreenInjector(InventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Shadow
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {

    }

    /** 打开建国界面，并把当前界面作为返回目标传进去。 */
    @Unique
    void minecraftTowerDefenser$onClickCreateCountryButton() {
        if (!ClientNationState.isLoaded()) {
            ClientNationState.requestSync();
            return;
        }

        if (ClientNationState.hasNation()) {
            return;
        }

        Minecraft.getInstance().setScreen(new CreateCountryScreen(Minecraft.getInstance().screen));
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    protected void injectInventoryScreen(CallbackInfo ci) {
        if (!ClientNationState.isLoaded()) {
            ClientNationState.requestSync();
        }

        InventoryScreen screen = (InventoryScreen)(Object) this;
        // 在原版背包初始化完成后追加按钮，避免与原版控件布局互相覆盖。
        this.minecraftTowerDefenser$createCountryButton = Button.builder(
                        Component.literal("Create"), // TODO：添加国际化文本组件
                        button -> {
                            minecraftTowerDefenser$onClickCreateCountryButton();
                        }
                )
                .pos(
                        screen.getGuiLeft() + 150,  // X 坐标（相对于背包左上角）
                        screen.getGuiTop() + 10      // Y 坐标
                )
                .size(60, 20) // 按钮宽、高
                .build();
        addRenderableWidget(this.minecraftTowerDefenser$createCountryButton);
        this.minecraftTowerDefenser$refreshCreateCountryButton();
    }

    @Inject(method = "containerTick()V", at = @At("TAIL"))
    private void minecraftTowerDefenser$updateCreateCountryButton(CallbackInfo ci) {
        this.minecraftTowerDefenser$refreshCreateCountryButton();
    }

    @Unique
    private void minecraftTowerDefenser$refreshCreateCountryButton() {
        if (this.minecraftTowerDefenser$createCountryButton == null) {
            return;
        }

        boolean visible = ClientNationState.isLoaded() && !ClientNationState.hasNation();
        this.minecraftTowerDefenser$createCountryButton.visible = visible;
        this.minecraftTowerDefenser$createCountryButton.active = visible;
    }


    @Shadow
    public void recipesUpdated() {

    }

    @Shadow
    public RecipeBookComponent getRecipeBookComponent() {
        return null;
    }
}
