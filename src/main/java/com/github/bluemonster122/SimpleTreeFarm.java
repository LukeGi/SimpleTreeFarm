package com.github.bluemonster122;

import com.github.bluemonster122.config.Configs;
import com.github.bluemonster122.gui.GuiHandler;
import com.github.bluemonster122.handlers.BlockHandler;
import com.github.bluemonster122.handlers.ItemHandler;
import com.github.bluemonster122.lib.ModInfo;
import com.github.bluemonster122.lib.Names;
import com.github.bluemonster122.tile.TreeFarmTile;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import scala.collection.parallel.ParIterableLike;

@Mod(modid = ModInfo.MOD_ID, name = ModInfo.MOD_NAME, version = ModInfo.VERSION, dependencies = ModInfo.DEPENDENCIES)
public class SimpleTreeFarm {
    public static Configs config;

    @Instance(ModInfo.MOD_ID)
    public static SimpleTreeFarm INSTANCE;

    public static CreativeTabs theTab = new CreativeTabs(ModInfo.MOD_ID) {
        @Override
        public Item getTabIconItem() {
            return Item.getItemFromBlock(BlockHandler.TREE_FARM);
        }
    };

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        MinecraftForge.TERRAIN_GEN_BUS.register(SimpleTreeFarm.TreeHandler.class);
        config = Configs.initialize(evt.getSuggestedConfigurationFile());
        GameRegistry.registerTileEntity(TreeFarmTile.class, ModInfo.MOD_ID + ":" + Names.Blocks.TREE_FARM);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        NetworkRegistry.INSTANCE.registerGuiHandler(SimpleTreeFarm.INSTANCE, new GuiHandler());
        GameRegistry.addShapedRecipe(new ItemStack(BlockHandler.TREE_FARM, 1), "SAS", "IOI", "SAS", 'S', new ItemStack(Blocks.SAPLING, 1, OreDictionary.WILDCARD_VALUE), 'A', new ItemStack(Items.IRON_AXE, 1), 'I', new ItemStack(Blocks.IRON_BLOCK, 1), 'O', new ItemStack(Blocks.OBSIDIAN, 1));
    }

    @Mod.EventBusSubscriber(Side.CLIENT)
    public static class ClientRegistry {
        @SubscribeEvent
        public static void registerModels(ModelRegistryEvent evt) {
            ModelLoader.setCustomModelResourceLocation(ItemHandler.TREE_FARM_ITEM, 0, new ModelResourceLocation(ItemHandler.TREE_FARM_ITEM.getRegistryName(), "inventory"));
        }
    }

    public static class TreeHandler {
        @SubscribeEvent
        public static void stopGrowth(SaplingGrowTreeEvent evt) {
            for (int i = -3; i <= 3 && !evt.getResult().equals(Event.Result.DENY); i++) {
                for (int j = -3; j <= 3; j++) {
                    if (evt.getWorld().getTileEntity(evt.getPos().add(i, 0, j)) instanceof TreeFarmTile) {
                        evt.setResult(Event.Result.DENY);
                        break;
                    }
                }
            }
        }
    }
}
