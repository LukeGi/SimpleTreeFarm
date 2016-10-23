package com.github.bluemonster122.config;

import com.github.bluemonster122.lib.ModInfo;
import net.minecraftforge.common.config.Config;

@Config(modid = ModInfo.MOD_ID, name = ModInfo.MOD_NAME + " Config", type = Config.Type.INSTANCE)
public class Configs {
    @Config.RangeInt(min = 0)
    @Config.Comment(value = "The amount of energy used by the tree farm to break a block")
    public static int ENERGY_CONSUMPTION_PER_BLOCK_BREAK = 100;

    @Config.RangeInt(min = 0)
    @Config.Comment(value = "The amount of energy used by the tree farm to place a sapling")
    public static int ENERGY_CONSUMPTION_PER_BLOCK_PLACE = 20;
}
