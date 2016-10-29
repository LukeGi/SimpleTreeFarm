package com.github.bluemonster122.block;

import com.github.bluemonster122.SimpleTreeFarm;
import com.github.bluemonster122.lib.ModInfo;
import com.github.bluemonster122.lib.Names;
import com.github.bluemonster122.tile.TreeFarmTile;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TreeFarm extends Block implements ITileEntityProvider {

    public TreeFarm() {
        super(Material.IRON);
        setRegistryName(ModInfo.MOD_ID, Names.Blocks.TREE_FARM);
        setUnlocalizedName(getRegistryName().getResourcePath());
        setCreativeTab(SimpleTreeFarm.theTab);
        setHarvestLevel("pickaxe", 3);
        setHardness(7);
        setResistance(500);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TreeFarmTile) {
            TreeFarmTile farmTile = (TreeFarmTile) tileEntity;
            farmTile.dropInventory();
            farmTile.breakSaplings();
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        playerIn.openGui(SimpleTreeFarm.INSTANCE, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Nonnull
    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TreeFarmTile();
    }
}
