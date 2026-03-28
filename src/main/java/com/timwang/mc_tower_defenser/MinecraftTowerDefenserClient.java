package com.timwang.mc_tower_defenser;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * 客户端入口。
 * 这里只放客户端专属的初始化，例如配置界面入口和客户端日志。
 */
@Mod(value = MinecraftTowerDefenser.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MinecraftTowerDefenser.MODID, value = Dist.CLIENT)
public class MinecraftTowerDefenserClient {
    /** 注册模组配置界面，让 Mod 列表页可以直接打开配置屏幕。 */
    public MinecraftTowerDefenserClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    /** 客户端初始化阶段，目前主要用于简单日志确认。 */
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        MinecraftTowerDefenser.LOGGER.info("HELLO FROM CLIENT SETUP");
        MinecraftTowerDefenser.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
