package com.github.bluemonster122.container;

import com.github.bluemonster122.tile.TreeFarmTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerTreeFarm extends Container {

    private EntityPlayer player;
    private TreeFarmTile tileEntity;

    public ContainerTreeFarm(EntityPlayer player, TreeFarmTile tileEntity) {
        this.player = player;
        this.tileEntity = tileEntity;

        IItemHandler inventory = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN);
        for (int j = 0; j < 8; j++) {
            for (int i = 0; i < 8; i++) {
                this.addSlotToContainer(new SlotItemHandler(inventory, j * 8 + i, i * 18 + 8, j * 18 + 18));
            }
        }

        int i;

        for (i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, 174 + i * 18));
            }
        }

        for (i = 0; i < 9; ++i) {
            this.addSlotToContainer(new Slot(player.inventory, i, 8 + i * 18, 232));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int fromSlot) {
        ItemStack previous = null;
        Slot slot = (Slot) this.inventorySlots.get(fromSlot);

        if (slot != null && slot.getHasStack()) {
            ItemStack current = slot.getStack();
            previous = current.copy();

            if (fromSlot < 9) {
                // From TE Inventory to Player Inventory
                if (!this.mergeItemStack(current, 64, 100, true))
                    return null;
            } else {
                // From Player Inventory to TE Inventory
                if (!this.mergeItemStack(current, 0, 63, false))
                    return null;
            }

            if (current.stackSize == 0)
                slot.putStack((ItemStack) null);
            else
                slot.onSlotChanged();

            if (current.stackSize == previous.stackSize)
                return null;
            slot.onPickupFromSlot(playerIn, current);
        }
        return previous;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.getPosition().distanceSq(tileEntity.getPos()) < 49;
    }


}