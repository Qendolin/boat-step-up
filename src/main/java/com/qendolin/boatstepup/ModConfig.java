package com.qendolin.boatstepup;


import com.qendolin.boatstepup.config.Config;
import com.qendolin.boatstepup.config.GsonConfig;

@GsonConfig(Main.MODID)
public class ModConfig implements Config {
    public float groundStepHeight = 0.25f;
    public int waterStepHeight = 2;
}
