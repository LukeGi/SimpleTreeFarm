package bluemonster122.simpletreefarm.tile;

import cofh.api.energy.IEnergyReceiver;
import bluemonster122.simpletreefarm.config.Configs;
import bluemonster122.simpletreefarm.farm.TreeSlot;
import com.google.common.collect.ImmutableSet;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemBlock;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.*;

@Optional.Interface(modid = "tesla", iface = "net.darkhax.tesla.api.ITeslaConsumer")
public class TreeFarmTile extends TileEntity implements ITickable, IEnergyStorage, IEnergyReceiver, ITeslaConsumer {
    private Map<BlockPos, TreeSlot> saplings = new HashMap<>();
    private ItemStackHandler inventory = new ItemStackHandler(72);
    private static Set<BlockPos> farmed = ImmutableSet.of(new BlockPos(-3, 0, -3), new BlockPos(-2, 0, -3), new BlockPos(-1, 0, -3), new BlockPos(0, 0, -3), new BlockPos(1, 0, -3), new BlockPos(2, 0, -3), new BlockPos(3, 0, -3), new BlockPos(-3, 0, -2), new BlockPos(-2, 0, -2), new BlockPos(-1, 0, -2), new BlockPos(0, 0, -2), new BlockPos(1, 0, -2), new BlockPos(2, 0, -2), new BlockPos(3, 0, -2), new BlockPos(-3, 0, -1), new BlockPos(-2, 0, -1), new BlockPos(-1, 0, -1), new BlockPos(0, 0, -1), new BlockPos(1, 0, -1), new BlockPos(2, 0, -1), new BlockPos(3, 0, -1), new BlockPos(-3, 0, 0), new BlockPos(-2, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0), new BlockPos(2, 0, 0), new BlockPos(3, 0, 0), new BlockPos(-3, 0, 1), new BlockPos(-2, 0, 1), new BlockPos(-1, 0, 1), new BlockPos(0, 0, 1), new BlockPos(1, 0, 1), new BlockPos(2, 0, 1), new BlockPos(3, 0, 1), new BlockPos(-3, 0, 2), new BlockPos(-2, 0, 2), new BlockPos(-1, 0, 2), new BlockPos(0, 0, 2), new BlockPos(1, 0, 2), new BlockPos(2, 0, 2), new BlockPos(3, 0, 2), new BlockPos(-3, 0, 3), new BlockPos(-2, 0, 3), new BlockPos(-1, 0, 3), new BlockPos(0, 0, 3), new BlockPos(1, 0, 3), new BlockPos(2, 0, 3), new BlockPos(3, 0, 3));
    public static Set<Block> ALLOWED_FARMING_BLOCKS = ImmutableSet.of(Blocks.DIRT, Blocks.FARMLAND, Blocks.GRASS, Blocks.GRASS_PATH);
    private int energy = 0;

    public TreeFarmTile() {
    }

    private boolean setupInternalFarm() {
        boolean flag = false;
        for (BlockPos saplingPos : farmed) {
            IBlockState block = getWorld().getBlockState(pos.add(saplingPos));
            if (saplings.get(pos.add(saplingPos)) == null && block.getBlock().equals(Blocks.SAPLING)) {
                BlockPlanks.EnumType type = block.getValue(BlockSapling.TYPE);
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
                for (int i = 1; i < height; i++) {
                    canHarvest &= getWorld().isAirBlock(current.up(i));
                }
                canHarvest &= energy >= (drops.size() * Configs.ENERGY_CONSUMPTION_PER_BLOCK_BREAK);
                System.out.println((drops.size() * Configs.ENERGY_CONSUMPTION_PER_BLOCK_BREAK));
                for (int i = 0; i < inventory.getSlots() && canHarvest; i++) {
                    ItemStack stackInSlot = inventory.getStackInSlot(i);
                    if (stackInSlot != null) {
                        trialInv.insertItem(i, stackInSlot.copy(), false);
                    }
                }
                for (int i = 0; i < drops.size() && canHarvest; i++) {
                    ItemStack drop = drops.get(i).copy();
                    canHarvest = ItemHandlerHelper.insertItem(trialInv, drop, false) == null;
                }
                if (canHarvest) {
                    farm.remove();
                    drops.forEach(stack -> {
                        ItemHandlerHelper.insertItem(inventory, stack.copy(), false);
                        energy -= Configs.ENERGY_CONSUMPTION_PER_BLOCK_BREAK;
                    });
                    getWorld().setBlockToAir(current);
                }
            }
        }
    }

    private void findGrowers() {
        Iterator<BlockPos> farm = saplings.keySet().iterator();
        while (farm.hasNext()) {
            BlockPos pos = farm.next();
            IBlockState state = worldObj.getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof BlockSapling) {
                if (!state.getValue(BlockSapling.TYPE).equals(saplings.get(pos).getTYPE())) {
                    farm.remove();
                } else if (state.getValue(BlockSapling.STAGE) != 0) {
                    saplings.get(pos).setHarvest(true);
                }
            } else if (worldObj.isAirBlock(pos)) {
                farm.remove();
            }
        }
    }

    @Override
    public void update() {
        findGrowers();
        getWorld().notifyBlockUpdate(pos, getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
        if (getWorld().getTotalWorldTime() % 60 != 0) return;
        plantSaplings();
        setupInternalFarm();
        if (!saplings.isEmpty())
            tickFarms();
    }

    private void plantSaplings() {
        if (!getWorld().isRemote && energy >= Configs.ENERGY_CONSUMPTION_PER_BLOCK_PLACE && inventory != null) {
            for (BlockPos p : farmed) {
                BlockPos pos = this.pos.add(p);
                if (worldObj.isAirBlock(pos) && isValidBlock(pos.down())) {
                    for (int i = 0; i < inventory.getSlots(); i++) {
                        ItemStack stack = inventory.getStackInSlot(i);
                        if (stack != null && stack.getItem() instanceof ItemBlock) {
                            Block block = ((ItemBlock) stack.getItem()).block;
                            if (block == Blocks.SAPLING) {
                                worldObj.setBlockState(pos, block.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.byMetadata(stack.getItemDamage())), 3);
                                inventory.extractItem(i, 1, false);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isValidBlock(BlockPos down) {
        return ALLOWED_FARMING_BLOCKS.contains(worldObj.getBlockState(down).getBlock());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);

        return new SPacketUpdateTileEntity(getPos(), 0, nbt);

    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    @Nonnull
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound nbt = super.getUpdateTag();
        writeToNBT(nbt);
        return nbt;
    }

    @Override
    @Nonnull
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
        //TODO: remove this
        ItemStackHandler temp = new ItemStackHandler(72);
        for (int i = 0; i < inventory.getSlots(); i++) {
            temp.setStackInSlot(i, inventory.getStackInSlot(i));
        }
        inventory = temp;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nonnull EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
    }

    @Override
    @Nonnull
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nonnull EnumFacing facing) {
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
