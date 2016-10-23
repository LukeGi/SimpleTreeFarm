package com.github.bluemonster122.gui;

import com.github.bluemonster122.container.ContainerTreeFarm;
import com.github.bluemonster122.lib.GuiBuilder;
import com.github.bluemonster122.lib.ModInfo;
import com.github.bluemonster122.tile.TreeFarmTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiTreeFarm extends GuiContainer {

    ResourceLocation TREE_FARM_GUI = new ResourceLocation(ModInfo.MOD_ID, "textures/gui/treefarm.png");
    EntityPlayer player;
    TreeFarmTile tile;

    public GuiTreeFarm(EntityPlayer player, TreeFarmTile treefarm) {
        super(new ContainerTreeFarm(player, treefarm));
        this.setGuiSize(176, 256);
        this.player = player;
        this.tile = treefarm;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TREE_FARM_GUI);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 176, 256);
        IEnergyStorage battery = tile.getCapability(CapabilityEnergy.ENERGY, EnumFacing.DOWN);
        int energyPercent = (int) ((float) battery.getEnergyStored() / (float) battery.getMaxEnergyStored() * 142);
        drawTexturedModalRect(guiLeft + 154, guiTop + 160 - energyPercent, 176, 160 - energyPercent, 17, energyPercent);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        if (isPointInRegion(154, 19, 16, 142, mouseX, mouseY)) {
            IEnergyStorage battery = tile.getCapability(CapabilityEnergy.ENERGY, EnumFacing.DOWN);
            List<String> list = new ArrayList<String>();
            list.add(battery.getEnergyStored() + " / " + battery.getMaxEnergyStored() + " " + "Forge Units");
            net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(list, mouseX - 64, mouseY-64, 153, 256, -1, mc.fontRendererObj);
            GlStateManager.disableLighting();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
    }
}
