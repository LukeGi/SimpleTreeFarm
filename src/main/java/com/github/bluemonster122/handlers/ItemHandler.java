package com.github.bluemonster122.handlers;

import com.github.bluemonster122.item.block.TreeFarmItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;

import static com.github.bluemonster122.handlers.BlockHandler.*;

public class ItemHandler {

    public static final TreeFarmItem TREE_FARM_ITEM = new TreeFarmItem(TREE_FARM);

    public static void registerItemBlocks(RegistryEvent.Register<Item> evt){
        evt.getRegistry().registerAll(
                TREE_FARM_ITEM
        );
    }

    public static void registerItems(RegistryEvent.Register<Item> evt) {
//        evt.getRegistry().registerAll(
//
//        );
    }
}
