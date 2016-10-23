package com.github.bluemonster122.handlers;

import com.github.bluemonster122.block.TreeFarm;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;

public class BlockHandler {
    public static final TreeFarm TREE_FARM = new TreeFarm();

    public static void registerBlocks(RegistryEvent.Register<Block> evt) {
        evt.getRegistry().registerAll(
                TREE_FARM
        );
    }
}
