package com.github.bluemonster122.tile;

import cofh.api.energy.IEnergyReceiver;
import com.github.bluemonster122.config.Configs;
import com.github.bluemonster122.farm.TreeSlot;
import com.github.bluemonster122.lib.Pair;
import com.google.common.collect.ImmutableSet;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

@Optional.Interface(modid = "tesla", iface = "net.darkhax.tesla.api.ITeslaConsumer")
public class TreeFarmTile extends TileEntity implements ITickable, IEnergyStorage, IEnergyReceiver, ITeslaConsumer {
    private Map<BlockPos, TreeSlot> saplings = new HashMap<>();
    private ItemStackHandler inventory = new ItemStackHandler(64);
    private static Set<BlockPos> farmed = ImmutableSet.of(new BlockPos(-3, 0, -3), new BlockPos(-2, 0, -3), new BlockPos(-1, 0, -3), new BlockPos(0, 0, -3), new BlockPos(1, 0, -3), new BlockPos(2, 0, -3), new BlockPos(3, 0, -3), new BlockPos(-3, 0, -2), new BlockPos(-2, 0, -2), new BlockPos(-1, 0, -2), new BlockPos(0, 0, -2), new BlockPos(1, 0, -2), new BlockPos(2, 0, -2), new BlockPos(3, 0, -2), new BlockPos(-3, 0, -1), new BlockPos(-2, 0, -1), new BlockPos(-1, 0, -1), new BlockPos(0, 0, -1), new BlockPos(1, 0, -1), new BlockPos(2, 0, -1), new BlockPos(3, 0, -1), new BlockPos(-3, 0, 0), new BlockPos(-2, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0), new BlockPos(2, 0, 0), new BlockPos(3, 0, 0), new BlockPos(-3, 0, 1), new BlockPos(-2, 0, 1), new BlockPos(-1, 0, 1), new BlockPos(0, 0, 1), new BlockPos(1, 0, 1), new BlockPos(2, 0, 1), new BlockPos(3, 0, 1), new BlockPos(-3, 0, 2), new BlockPos(-2, 0, 2), new BlockPos(-1, 0, 2), new BlockPos(0, 0, 2), new BlockPos(1, 0, 2), new BlockPos(2, 0, 2), new BlockPos(3, 0, 2), new BlockPos(-3, 0, 3), new BlockPos(-2, 0, 3), new BlockPos(-1, 0, 3), new BlockPos(0, 0, 3), new BlockPos(1, 0, 3), new BlockPos(2, 0, 3), new BlockPos(3, 0, 3));
    private int energy = 0;

    public TreeFarmTile() {
    }

    private boolean setupInternalFarm() {
        boolean flag = false;
        for (BlockPos saplingPos : farmed) {
            IBlockState block = getWorld().getBlockState(pos.add(saplingPos));
            if (saplings.get(pos.add(saplingPos)) == null && block.getBlock().equals(Blocks.SAPLING)) {
                BlockPlanks.EnumType type = BlockPlanks.EnumType.byMetadata(block.getBlock().getMetaFromState(block));
                boolean isMega = getWorld().rand.nextInt(10) == 2 && (type == BlockPlanks.EnumType.SPRUCE || type == BlockPlanks.EnumType.JUNGLE);
                saplings.put(pos.add(saplingPos), new TreeSlot(type, isMega, TreeSlot.getMinHeight(type, isMega), TreeSlot.getChanceHeight(type, isMega, getWorld().rand), TreeSlot.getAddChanceHeight(type, isMega, getWorld().rand)));
                block.getBlock().setTickRandomly(false);
                flag = true;
            }
        }
        return flag;
    }

    private void tickFarms() {
        Iterator<BlockPos> farm = saplings.keySet().iterator();
        while (farm.hasNext()) {
            BlockPos current = farm.next();
            TreeSlot sapling = saplings.get(current);
            if (sapling.shouldHarvest()) {
                List<ItemStack> drops = sapling.getTreeGrown(getWorld().rand);
                boolean canHarvest = true;
                IItemHandler trialInv = new ItemStackHandler(inventory.getSlots());
                int height = sapling.isMega() ? 15 : 7;
                for (int i = 0; i < height; i++) {
                    canHarvest &= getWorld().isAirBlock(current.up(i));
                }
                canHarvest &= energy > (drops.size() * Configs.ENERGY_CONSUMPTION_PER_BLOCK_BREAK);
                for (int i = 0; i < inventory.getSlots() && canHarvest; i++) {
                    ItemStack stackInSlot = inventory.getStackInSlot(i);
                    if (stackInSlot != null) {
                        trialInv.insertItem(i, stackInSlot.copy(), false);
                    }
                }
                for (int i = 0; i < drops.size() && canHarvest; i++) {
                    ItemStack drop = drops.get(i);
                    canHarvest &= ItemHandlerHelper.insertItem(trialInv, drop, false) == null;
                }
                if (canHarvest) {
                    farm.remove();
                    drops.forEach(stack -> {
                        ItemHandlerHelper.insertItem(inventory, stack, false);
                        energy -= Configs.ENERGY_CONSUMPTION_PER_BLOCK_BREAK;
                    });
                    getWorld().setBlockToAir(current);
                }
            }
        }
    }

    private void findGrowers() {
        Iterator<BlockPos> farm = saplings.keySet().iterator();
        List<Pair<BlockPos, TreeSlot>> cache = new ArrayList<>();
        while (farm.hasNext()) {
            BlockPos pos = farm.next();
            IBlockState state = worldObj.getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof BlockSapling) {
                if (state.getValue(BlockSapling.TYPE).equals(saplings.get(pos).getTYPE())) {
                    farm.remove();
                    continue;
                }
                if (state.getValue(BlockSapling.STAGE) != 0) {
                    IBlockState newState = block.getDefaultState();
                    newState = newState.withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.byMetadata(state.getBlock().getMetaFromState(state))).withProperty(BlockSapling.STAGE, 0);
                    getWorld().notifyBlockUpdate(pos, state, newState, 3);
                    TreeSlot sl = saplings.get(pos);
                    farm.remove();
                    sl.setHarvest(true);
                    cache.add(new Pair<>(pos, sl));
                }
            } else if (worldObj.isAirBlock(pos)) {
                farm.remove();
            }
        }
        cache.forEach(pair -> saplings.put(pair.getKey(), pair.getValue()));
    }

    @Override
    public void update() {
        findGrowers();
        if (!getWorld().isAirBlock(pos.up())) getWorld().destroyBlock(pos.up(), true);
        getWorld().notifyBlockUpdate(pos, getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
        if (getWorld().getTotalWorldTime() % 60 != 0) return;
        plantSaplings();
        setupInternalFarm();
        if (!saplings.isEmpty())
            tickFarms();
    }

    private void plantSaplings() {
        if (!getWorld().isRemote && energy >= Configs.ENERGY_CONSUMPTION_PER_BLOCK_PLACE) {
            for (int i = 0; i < inventory.getSlots() && energy >= Configs.ENERGY_CONSUMPTION_PER_BLOCK_PLACE; i++) {
                ItemStack element = inventory.getStackInSlot(i);
                if (element == null) continue;
                ItemStack elementCopy = ItemStack.copyItemStack(element);
                elementCopy.setItemDamage(OreDictionary.WILDCARD_VALUE);
                elementCopy.stackSize = 1;
                if (elementCopy.isItemEqual(new ItemStack(Blocks.SAPLING, 1, OreDictionary.WILDCARD_VALUE))) {
                    BlockSapling sapling = (BlockSapling) Block.getBlockFromItem(element.getItem());
                    for (BlockPos saplingPos : farmed) {
                        if (energy < Configs.ENERGY_CONSUMPTION_PER_BLOCK_PLACE) break;
                        if (worldObj.isAirBlock(pos.add(saplingPos)) && isValidSoil(worldObj.getBlockState(pos.add(saplingPos).down())) && energy >= 50) {
                            IBlockState theState = sapling.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.byMetadata(element.getItemDamage()));
                            worldObj.setBlockState(pos.add(saplingPos), theState, 3);
                            energy -= Configs.ENERGY_CONSUMPTION_PER_BLOCK_PLACE;
                            inventory.extractItem(i, 1, false);
                            if (inventory.getStackInSlot(i) == null || inventory.getStackInSlot(i).stackSize <= 0) {
                                inventory.setStackInSlot(i, null);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isValidSoil(IBlockState blockState) {
        return blockState.getBlock().equals(Blocks.GRASS) || blockState.getBlock().equals(Blocks.DIRT);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);

        return new SPacketUpdateTileEntity(getPos(), 0, nbt);

    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound nbt = super.getUpdateTag();
        writeToNBT(nbt);
        return nbt;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("energy", energy);
        compound.setTag("inventory", inventory.serializeNBT());
        compound.setInteger("saplingsize", saplings.keySet().size());
        int id = 0;
        for (BlockPos pos : saplings.keySet()) {
            TreeSlot slot = saplings.get(pos);
            compound.setLong("sapPos" + id, pos.toLong());
            compound.setInteger("sapType" + id, slot.getType());
            compound.setBoolean("sapGrow" + id++, slot.shouldHarvest());
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        Random random = new Random();
        energy = compound.getInteger("energy");
        if (compound.hasKey("inventory")) {
            inventory.deserializeNBT((NBTTagCompound) compound.getTag("inventory"));
        }
        int saplingsize = compound.getInteger("saplingsize");
        for (int i = 0; i < saplingsize; i++) {
            BlockPos pos = BlockPos.fromLong(compound.getLong("sapPos" + i));
            BlockPlanks.EnumType type = BlockPlanks.EnumType.byMetadata(compound.getInteger("sapType" + i));
            boolean isMega = random.nextInt(10) == 2 && (type == BlockPlanks.EnumType.SPRUCE || type == BlockPlanks.EnumType.JUNGLE);
            TreeSlot slot = new TreeSlot(type, isMega, TreeSlot.getMinHeight(type, isMega), TreeSlot.getChanceHeight(type, isMega, random), TreeSlot.getAddChanceHeight(type, isMega, random));
            slot.setHarvest(compound.getBoolean("sapGrow" + i));
            saplings.put(pos, slot);
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory) : capability == CapabilityEnergy.ENERGY ? CapabilityEnergy.ENERGY.cast(this) : super.getCapability(capability, facing);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive())
            return 0;

        int energyReceived = Math.min(1000000 - energy, maxReceive);
        if (!simulate)
            energy += energyReceived;
        getWorld().notifyBlockUpdate(pos, getWorld().getBlockState(pos), getWorld().getBlockState(pos), 3);
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return 1000000;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return canReceive();
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        return getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        return getMaxEnergyStored();
    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        return receiveEnergy(maxReceive, simulate);
    }

    @Override
    @Optional.Method(modid = "tesla")
    public long givePower(long power, boolean simulated) {
        return receiveEnergy(Math.toIntExact(power), simulated);
    }

    public void dropInventory() {
        if (worldObj.isRemote) {
            return;
        }
        for (int i = 0; i < inventory.getSlots(); ++i) {
            ItemStack itemstack = inventory.getStackInSlot(i);
            if (itemstack != null) {
                InventoryHelper.spawnItemStack(worldObj, pos.getX(), pos.getY(), pos.getZ(), itemstack);
            }
        }
    }

    public void breakSaplings() {
        if (worldObj.isRemote) {
            return;
        }
        for (BlockPos blockPos : saplings.keySet()) {
            worldObj.destroyBlock(blockPos, true);
        }
    }
}
