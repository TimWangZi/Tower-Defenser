package com.timwang.mc_tower_defenser.fundation.network.handler;

import com.timwang.mc_tower_defenser.fundation.network.payloads.RegisterNationPayloads;
import com.timwang.mc_tower_defenser.fundation.system.GlobalNationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.logging.Level;

public class RegisterNationHandler {
    public static void client_handler(final RegisterNationPayloads data, final IPayloadContext context) {

    }
    public static void server_handler(final RegisterNationPayloads data, final IPayloadContext context) {
        GlobalNationManager.get(MinecraftServer.)
    }
}
