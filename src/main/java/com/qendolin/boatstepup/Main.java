package com.qendolin.boatstepup;

import com.qendolin.boatstepup.config.ConfigLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main implements ModInitializer, ClientModInitializer {
    public static final String MODID = "boat_step_up";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final ModConfig CONFIG = ConfigLoader.createOrLoad(ModConfig.class);
    public static final ModConfig RUNTIME_CONFIG = new ModConfig().copyFrom(CONFIG);
    public static final Identifier CONFIG_PACKET_ID = new Identifier(MODID, "config");

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            // I have no idea
            ServerPlayNetworking.send(handler.player, CONFIG_PACKET_ID, RUNTIME_CONFIG.writePacket());
        });
        LOGGER.info("Local config: {}", CONFIG);
        LOGGER.info("Runtime config: {}", RUNTIME_CONFIG);
    }

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(CONFIG_PACKET_ID, (client, handler, buf, responseSender) -> {
            LOGGER.info("Received server config");
            if(client.isInSingleplayer()) {
                RUNTIME_CONFIG.serverEnabled = true;
                LOGGER.info("Discarding config as we are in singleplayer");
                LOGGER.info("Runtime config: {}", RUNTIME_CONFIG);
                return;
            }
            try {
                RUNTIME_CONFIG.readPacket(buf);
                RUNTIME_CONFIG.serverEnabled = true;
            } catch (Exception e) {
                LOGGER.error("Could not deserialize config: ", e);
            }
            LOGGER.info("Runtime config: {}", RUNTIME_CONFIG);
        });

        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            // I hope this works always
            RUNTIME_CONFIG.serverEnabled = false;
            RUNTIME_CONFIG.copyFrom(CONFIG);
        });
    }
}
