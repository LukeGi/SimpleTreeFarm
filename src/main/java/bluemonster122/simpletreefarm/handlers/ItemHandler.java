package bluemonster122.simpletreefarm.handlers;

import bluemonster122.simpletreefarm.item.block.TreeFarmItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;

import static bluemonster122.simpletreefarm.handlers.BlockHandler.*;

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
