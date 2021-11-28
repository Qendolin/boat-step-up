package com.qendolin.boatstepup;

import com.qendolin.boatstepup.config.ConfigLoader;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main implements ModInitializer {
    public static final String MODID = "boat_step_up";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static final ModConfig CONFIG = ConfigLoader.createOrLoad(ModConfig.class);

    @Override
    public void onInitialize() {

    }
}
