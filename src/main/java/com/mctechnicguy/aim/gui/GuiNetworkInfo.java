package com.mctechnicguy.aim.gui;

import com.mctechnicguy.aim.ModInfo;
import com.mctechnicguy.aim.tileentity.TileEntityAIMCore;
import com.mctechnicguy.aim.tileentity.TileEntityNetworkElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GuiNetworkInfo extends GuiScreen {

	private static final ResourceLocation GuiTexture = new ResourceLocation(ModInfo.ID.toLowerCase(), "textures/gui/guinetworkinfo.png");

	private TileEntityAIMCore core;
	// Width of the gui-image
	private static final int BGX = 216;
	// Height of the gui-image
	private static final int BGY = 166;
	private int BgStartX;
	private int BgStartY;
	private int ScrollBarPos;

	private boolean ScrollBarActive;
	private int lastMouseY;

	@Nonnull
	private Map<String, Integer> devices = new HashMap<>();
	@Nonnull
	private Map<String, ItemStack> stackMap = new HashMap<>();

	public GuiNetworkInfo(TileEntityAIMCore entity) {
		super();
		this.core = entity;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		GL11.glColor4f(1F, 1F, 1F, 1F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTexture);
		drawTexturedModalRect(BgStartX, BgStartY, 0, 0, BGX, BGY);
		if (this.core.Power > 0) {
			int k = this.core.getPowerRemainingScaled(50);
			drawTexturedModalRect(BgStartX + 11, BgStartY + 71 - k, BGX, 50 - k, 16, k);
		}

		// Scrollbar
		drawTexturedModalRect(BgStartX + 194, BgStartY + 56 + ScrollBarPos, BGX, 50, 12, 15);

		this.fontRenderer.drawString(I18n.format("gui.AIMInfo.coreHeader"), BgStartX + 10, BgStartY + 7, 4210752);
		this.fontRenderer.drawString(I18n.format("gui.AIMInfo.networkHeader"), BgStartX + 115, BgStartY + 7, 4210752);

		GL11.glPushMatrix();
		GL11.glScalef(0.75F, 0.75F, 0.75F);
		double scale = 1/0.75F;

		// Core Information
		this.fontRenderer.drawString(I18n.format("gui.AIMInfo.Power"), (int) ((BgStartX + 31) * scale), (int) ((BgStartY + 22) * scale), 4210752);
		this.fontRenderer.drawString(core.Power + " RF", (int) ((BgStartX + 31) * scale), (int) ((BgStartY + 31) * scale), 4210752);

		this.fontRenderer.drawString(I18n.format("gui.AIMInfo.PowerDrain"), (int) ((BgStartX + 31) * scale), (int) ((BgStartY + 40) * scale),
				4210752);
		this.fontRenderer.drawString((core.isActive() ? core.getNetworkPowerDrain() : 0) + " RF/t", (int) ((BgStartX + 31) * scale),
				(int) ((BgStartY + 49) * scale), 4210752);

		this.fontRenderer.drawString(I18n.format("gui.AIMInfo.isActive"), (int) ((BgStartX + 31) * scale), (int) ((BgStartY + 58) * scale), 4210752);
		this.fontRenderer.drawString(I18n.format(core.isActive() ? "gui.AIMInfo.boolTrue" : "gui.AIMInfo.boolFalse"),
				(int) ((BgStartX + 31) * scale), (int) ((BgStartY + 67) * scale), core.isActive() ? 1238807 : 15208978);

		this.fontRenderer.drawString(I18n.format("gui.AIMInfo.connectedPlayer"), (int) ((BgStartX + 10) * scale), (int) ((BgStartY + 76) * scale),
				4210752);
		this.fontRenderer.drawString((core.playerConnectedName != null && !core.playerConnectedName.isEmpty() ? core.playerConnectedName : I18n.format("gui.AIMInfo.noPlayer")),
				(int) ((BgStartX + 13) * scale), (int) ((BgStartY + 85) * scale), 1234664);

		this.fontRenderer.drawString(I18n.format("gui.AIMInfo.isPlayerAccessible"), (int) ((BgStartX + 10) * scale), (int) ((BgStartY + 94) * scale),
				4210752);
		this.fontRenderer.drawString(I18n.format(core.playerAccessible ? "gui.AIMInfo.boolTrue" : "gui.AIMInfo.boolFalse"),
				(int) ((BgStartX + 13) * scale), (int) ((BgStartY + 103) * scale), core.playerAccessible ? 1238807 : 15208978);

		this.fontRenderer.drawString(I18n.format("gui.AIMInfo.isSecured"), (int) ((BgStartX + 10) * scale), (int) ((BgStartY + 112) * scale),
				4210752);
		this.fontRenderer.drawString(I18n.format(core.hasUpgrade(0) && core.playerConnectedName != null && !core.playerConnectedName.isEmpty() ? "gui.AIMInfo.boolTrue" : "gui.AIMInfo.boolFalse"),
				(int) ((BgStartX + 13) * scale), (int) ((BgStartY + 121) * scale),
				core.hasUpgrade(0) && core.playerConnectedName != null && !core.playerConnectedName.isEmpty() ? 1238807 : 15208978);

		this.fontRenderer.drawString(I18n.format("gui.AIMInfo.corePosition"), (int) ((BgStartX + 10) * scale), (int) ((BgStartY + 130) * scale),
				4210752);
		this.fontRenderer.drawString(core.getPos().getX() + " | " + core.getPos().getY() + " | " + core.getPos().getZ(), (int) ((BgStartX + 13) * scale),
				(int) ((BgStartY + 139) * scale), 4210752);

		// Network Information
		this.fontRenderer.drawString(I18n.format("gui.AIMInfo.elementsConnected"), (int) ((BgStartX + 113) * scale), (int) ((BgStartY + 22) * scale),
				4210752);
		this.fontRenderer.drawString(String.valueOf(core.numberCablesConnected() + core.numberDevicesConnected()) + " + 1 Core",
				(int) ((BgStartX + 115) * scale), (int) ((BgStartY + 31) * scale), 4210752);

		this.fontRenderer.drawString(I18n.format("gui.AIMInfo.elementList"), (int) ((BgStartX + 113) * scale), (int) ((BgStartY + 45) * scale),
				4210752);

		GL11.glPopMatrix();

		// Element List
        GL11.glPushMatrix();
        GL11.glScalef(0.45F, 0.45F, 0.45F);
        scale = 1 / 0.45F;
		if (!devices.isEmpty()) {
			int ScrollBarListPos = this.getListPosFromScrollbar();
			for (int i = 0; i < Math.min(devices.size(), 5); i++) {
				if (devices.size() > i) {
					this.drawItemStack(stackMap.get(devices.keySet().toArray()[ScrollBarListPos + i]), (int)((BgStartX + 170) * scale), (int)((BgStartY + 58 + 18*i) * scale));

					this.fontRenderer.drawString(I18n.format((String) devices.keySet().toArray()[ScrollBarListPos + i]),
							(int) ((BgStartX + 115) * scale), (int) ((BgStartY + 60 + 18*i) * scale), 16448250);
					this.fontRenderer.drawString(I18n.format("gui.AIMInfo.Amount") + devices.get(devices.keySet().toArray()[ScrollBarListPos + i]),
							(int) ((BgStartX + 115) * scale), (int) ((BgStartY + 67 + 18*i) * scale), 16448250);
				}
			}

		}
        GL11.glPopMatrix();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void drawItemStack(@Nonnull ItemStack stack, int x, int y) {
		if (stack.isEmpty()) return;
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glScalef(2F, 2F, 2F);
		itemRender.renderItemIntoGUI(stack, x/2, y/2);
		GL11.glScalef(0.5F, 0.5F, 0.5F);
	}

	private int getListPosFromScrollbar() {
		if (this.ScrollBarPos == 0 || devices.size() < 6)
			return 0;
		int i = Math.round(devices.size() * (75 / this.ScrollBarPos));
		return i < devices.size() - 4 ? i : devices.size() - 5;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		BgStartX = (this.width / 2) - (BGX / 2);
		BgStartY = (this.height / 2) - (BGY / 2);

		for (TileEntityNetworkElement te : core.registeredDevices) {
			String key = te.getUnlocalizedBlockName();
			if (!devices.containsKey(key)) {
				devices.put(key, 1);
				stackMap.put(key, te.getDisplayStack());
			} else
				devices.put(key, devices.get(key) + 1);

		}

	}

	protected void keyTyped(char c, int id) throws IOException {
		if (c == 1 || id == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
			this.mc.player.closeScreen();
		}
		super.keyTyped(c, id);
	}

	protected void mouseClickMove(int x, int y, int button, long t) {
		if (this.ScrollBarActive) {
			ScrollBarPos = y - BgStartY - 56 - lastMouseY;
			if (ScrollBarPos < 0)
				ScrollBarPos = 0;
			else if (ScrollBarPos > 75)
				ScrollBarPos = 75;
		}
	}

	protected void mouseReleased(int mouseX, int mouseY, int state)
    {
		if (state == 0 && this.ScrollBarActive) {
			this.ScrollBarActive = false;
		}
		super.mouseReleased(mouseX, mouseY, state);
	}

	protected void mouseClicked(int x, int y, int button) throws IOException {
		if (button == 0) {
			if (x >= BgStartX + 194 && x <= BgStartX + 194 + 12 && y >= BgStartY + 56 + ScrollBarPos && y <= BgStartY + 56 + ScrollBarPos + 16) {
				this.ScrollBarActive = true;
				this.lastMouseY = y - BgStartY - 56 - ScrollBarPos;
			}
		}
		super.mouseClicked(x, y, button);
	}

}
