package com.mctechnicguy.aim.gui;

import com.mctechnicguy.aim.ModInfo;
import com.mctechnicguy.aim.container.ContainerAIMCore;
import com.mctechnicguy.aim.tileentity.TileEntityAIMCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GuiAIMCore extends GuiContainer {

	private static final ResourceLocation GuiTexture = new ResourceLocation(ModInfo.ID.toLowerCase(), "textures/gui/guicore.png");

	public TileEntityAIMCore core;

	private boolean SearchSuccess;
	private int TickCounter = 0;

	public GuiAIMCore(@Nonnull InventoryPlayer inventoryPlayer, @Nonnull TileEntityAIMCore entity) {
		super(new ContainerAIMCore(inventoryPlayer, entity));

		this.core = entity;
		this.xSize = 176;
		this.ySize = 189;

		SearchSuccess = core.searchForDevicesInNetwork();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX,mouseY);
	}

	public void drawGuiContainerForegroundLayer(int par1, int par2) {
		
		if (TickCounter >= 1000) {
			SearchSuccess = core.searchForDevicesInNetwork();
			TickCounter = 0;
		} else
			TickCounter++;
		
		
		String problemMessage = "gui.AIMCore.Problems.none";
		if (core.playerConnectedName == null || core.playerConnectedName.isEmpty()) problemMessage = "gui.AIMCore.Problems.noPlayer";
		else if (!SearchSuccess) problemMessage = "gui.AIMCore.Problems.wrongSetup";
		else if (core.Power <= core.getNetworkPowerDrain()) problemMessage = "gui.AIMCore.Problems.noPower";

		String name = core.hasCustomName() ? core.getName() : I18n.format(core.getName());
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2 + 5, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 115, this.ySize - 105, 4210752);
		this.fontRenderer.drawString(I18n.format("gui.AIMCore.Upgrades"), 120, 25, 4210752);

		GL11.glScaled(0.8, 0.8, 0.8);

		this.fontRenderer.drawString(I18n.format("gui.AIMInfo.isActive"), 39, 29, 4210752);
		this.fontRenderer.drawString(I18n.format(core.isActive() ? "gui.AIMInfo.boolTrue" : "gui.AIMInfo.boolFalse"), 45, 38,
				core.isActive() ? 1238807 : 15208978);

		this.fontRenderer.drawString(I18n.format("gui.AIMInfo.PowerDrain"), 39, 49, 4210752);
		this.fontRenderer.drawString((core.isActive() ? core.getNetworkPowerDrain() : 0) + " RF/t", 45, 58, 4210752);

		this.fontRenderer.drawString(I18n.format("gui.AIMInfo.connectedPlayer"), 39, 69, 4210752);
		this.fontRenderer.drawString(
				(core.playerConnectedName != null && !core.playerConnectedName.equals("") ? core.playerConnectedName : I18n.format("gui.AIMInfo.noPlayer")),
				45, 78, 1234664);

		this.fontRenderer.drawString(I18n.format("gui.AIMCore.Problems"), 39, 89, 4210752);
		this.fontRenderer.drawString((I18n.format(problemMessage)), 45, 98, problemMessage.equals("gui.AIMCore.Problems.none") ? 1238807 : 15208978);

		GL11.glScaled(1.25, 1.25, 1.25);
		
		if (par1 >= guiLeft + 8 && par1 <= guiLeft + 24 && par2 <= guiTop + 71 && par2 >= guiTop + 21) {
			List list = new ArrayList<String>();
			list.add(core.Power + "/" + core.MaxPower() + " RF");
			this.drawHoveringText(list, par1 - guiLeft, par2 - guiTop, this.fontRenderer);
		}
		
		
		if (!problemMessage.equals("gui.AIMCore.Problems.none") && par1 >= guiLeft + 31 && par1 <= guiLeft + 101 && par2 <= guiTop + 91 && par2 >= guiTop + 75) {
			List list = new ArrayList<String>();
			list.add(I18n.format(problemMessage + ".desc"));
			if (problemMessage.equals("gui.AIMCore.Problems.wrongSetup") || problemMessage.equals("gui.AIMCore.Problems.noPower")) list.add(I18n.format(problemMessage + ".desc.2"));
			if (problemMessage.equals("gui.AIMCore.Problems.wrongSetup")) list.add(I18n.format(problemMessage + ".desc.3"));
			this.drawHoveringText(list, par1 - guiLeft, par2 - guiTop, this.fontRenderer);
		}
		
		
	}

	public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GL11.glColor4f(1F, 1F, 1F, 1F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTexture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		if (this.core.Power > 0) {
			int k = this.core.getPowerRemainingScaled(50);
			drawTexturedModalRect(guiLeft + 8, guiTop + 71 - k, 176, 50 - k, 16, k);
		}
	}
}
