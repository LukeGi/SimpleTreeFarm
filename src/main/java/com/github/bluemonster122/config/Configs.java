package com.github.bluemonster122.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class Configs {
    private static Configs instance = null;
    public static String CATEGORY_TWEAKS = "FARM_TWEAKS";

    public static int farmEnergyUseagePerPlant;
    public static int farmEnergyUseagePerBreak;

    public static Configuration config;

    private Configs(File configFile) {
        config = new Configuration(configFile);
        config.load();
        Configs.configs();
        config.save();
    }

    public static Configs initialize(File configFile) {
        if (instance == null)
            instance = new Configs(configFile);
        else
            throw new IllegalStateException("Cannot initialize QuantumStorage Config twice");
        return instance;
    }

    public static Configs instance() {
        if (instance == null) {
            throw new IllegalStateException("Instance of QuantumStorage Config requested before initialization");
        }
        return instance;
    }

    public static void configs() {
        farmEnergyUseagePerPlant = config.get(CATEGORY_TWEAKS, "Energy usage on planting of sapling",
                50, "set to change the amount of energy drained from the farm when it places a sapling").getInt();
        farmEnergyUseagePerBreak = config.get(CATEGORY_TWEAKS, "Energy usage on chopping of block",
                200, "set to change the amount of energy drained from the farm when it breaks a /virtual/ block").getInt();
        if (config.hasChanged())
            config.save();
    }
}
