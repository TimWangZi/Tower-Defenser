package com.timwang.mc_tower_defenser.fundation.gui;

import com.timwang.mc_tower_defenser.fundation.gui.Screen.FarmerWorkScreen;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * GUI 层注册入口。
 * 统一把菜单类型和客户端屏幕绑定起来。
 */
public final class ModGuiScreen {
    private ModGuiScreen() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModGuiMenu.FARMER_WORK_MENU.get(), FarmerWorkScreen::new);
    }
}
