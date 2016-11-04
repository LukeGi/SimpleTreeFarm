package bluemonster122.simpletreefarm.item.block;

import bluemonster122.simpletreefarm.lib.ModInfo;
import bluemonster122.simpletreefarm.lib.Names;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TreeFarmItem extends ItemBlock {
    public TreeFarmItem(Block block) {
        super(block);
        setRegistryName(ModInfo.MOD_ID, Names.Blocks.TREE_FARM);
        setUnlocalizedName(getRegistryName().getResourcePath());
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        if (world.isAirBlock(pos.up())) {
            return super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
        } else {
            return false;
        }
    }
}
