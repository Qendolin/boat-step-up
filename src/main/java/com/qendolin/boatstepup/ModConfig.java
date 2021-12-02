package com.qendolin.boatstepup;


import com.qendolin.boatstepup.config.Config;
import com.qendolin.boatstepup.config.GsonConfig;

@GsonConfig(value = Main.MODID, version = 2)
public class ModConfig implements Config {
    public float groundStepHeight = 0.25f;
    public float waterStepHeight = 3;
}
