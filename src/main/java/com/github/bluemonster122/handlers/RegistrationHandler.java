package com.github.bluemonster122.handlers;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class RegistrationHandler {
    @SubscribeEvent
    public static void addBlocks(RegistryEvent.Register<Block> evt) {
        BlockHandler.registerBlocks(evt);
    }

    @SubscribeEvent
    public static void addItems(RegistryEvent.Register<Item> evt) {
        ItemHandler.registerItemBlocks(evt);
        ItemHandler.registerItems(evt);
    }

}
