package com.mctechnicguy.aim.gui;

import com.mctechnicguy.aim.ModInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GuiAdvancedNetworkInfo extends GuiScreen {

    private static final ResourceLocation GuiTexture = new ResourceLocation(ModInfo.ID.toLowerCase(), "textures/gui/guiadvancednetworkinfo.png");

    // Width of the gui-image
    private static final int BGX = 400;
    // Height of the gui-image
    private static final int BGY = 240;

    private static final int MARGIN = 22;
    private static final int BOTTOM_MARGIN = 62;
    private int BgStartX;
    private int BgStartY;

    private WorldNetworkList networkListSelector;

    private GuiButton buttonSelect;

    private NBTTagList NBTCoreList;
    private boolean playerAccessible;

    public GuiAdvancedNetworkInfo(NBTTagList cores, boolean playerAccessible) {
        this.NBTCoreList = cores;
        this.playerAccessible = playerAccessible;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        mc.getTextureManager().bindTexture(GuiTexture);
        GuiUtils.drawScaledTexturedQuad(BgStartX, BgStartY, 0, 0,200, 120, 256, BGX, BGY , zLevel);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        this.networkListSelector.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRenderer, I18n.format("gui.advancedinfo.list.header"), this.width / 2, BgStartY + 8, 16777215);
        GuiUtils.drawScaledMultilineString(fontRenderer, I18n.format(playerAccessible ? "message.playerAccessibility.true" : "message.playerAccessibility.false"), BgStartX + MARGIN + 5, BgStartY + BGY - BOTTOM_MARGIN + 22, BGX - 3* MARGIN - 105, 28, playerAccessible ? 0x55FF55 : 0xFF5555);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.networkListSelector.handleMouseInput();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        super.onGuiClosed();
    }

    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            WorldCoreEntry selectedEntry = this.networkListSelector.getSelected() < 0 ? null : (WorldCoreEntry)this.networkListSelector.getListEntry(this.networkListSelector.getSelected());
            if (selectedEntry != null) {
                this.showDetailPage(selectedEntry);
            }
        }
    }

    void showDetailPage(WorldCoreEntry toShow) {
        Minecraft.getMinecraft().player.sendChatMessage("Details shown for core " + toShow.toString());
    }

    @Override
    public void initGui() {
        BgStartX = (this.width / 2) - (BGX / 2);
        BgStartY = (this.height / 2) - (BGY / 2);
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        this.networkListSelector = new WorldNetworkList(this, this.mc, BGX - 2 * MARGIN, BGY - BOTTOM_MARGIN - MARGIN, BgStartY + MARGIN, BgStartY + BGY - BOTTOM_MARGIN, 20);
        this.networkListSelector.setSlotXBoundsFromLeft(BgStartX + MARGIN);
        this.networkListSelector.headerPadding = 0;
        this.networkListSelector.updateCoreList(NBTCoreList);

        buttonSelect = new GuiButton(0, BgStartX + BGX - 100, BgStartY + BGY - BOTTOM_MARGIN + 20, 70, 20, I18n.format("gui.advancedinfo.list.select"));
        buttonSelect.enabled = false;
        this.buttonList.add(buttonSelect);
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        int i = this.networkListSelector.getSelected();
        if (keyCode == 200) {
            if (i > 0) {
                this.networkListSelector.setSelectedSlotIndex(i - 1);
                this.networkListSelector.scrollBy(-this.networkListSelector.getSlotHeight());
            }
        } else if (keyCode == 208) {
            if (i < this.networkListSelector.getSize() - 1) {
                this.networkListSelector.setSelectedSlotIndex(i + 1);
                this.networkListSelector.scrollBy(this.networkListSelector.getSlotHeight());
            }
        } else if (keyCode == 28 || keyCode == 156) {
            this.actionPerformed(buttonSelect);
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    void selectEntry(int slotID) {
        this.buttonSelect.enabled = slotID >= 0 && slotID < this.networkListSelector.getSize();
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.networkListSelector.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.networkListSelector.mouseReleased(mouseX, mouseY, state);
    }

}
