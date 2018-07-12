package com.mctechnicguy.aim.gui;

import com.mctechnicguy.aim.ModInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiLoadingScreen extends GuiScreen {

    private static final ResourceLocation GuiTexture = new ResourceLocation(ModInfo.ID.toLowerCase(), "textures/gui/guiloading.png");

    // Width of the gui-image
    private static final int BGX = 400;
    // Height of the gui-image
    private static final int BGY = 240;
    private int BgStartX;
    private int BgStartY;

    private int tickCounter = 0;
    private double prevProgress = 0;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        GlStateManager.color(1F, 1F, 1F, 1F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiTexture);
        GuiUtils.drawScaledTexturedQuad(BgStartX, BgStartY, 0, 0,200, 120, 256, BGX, BGY , zLevel);
        double progress = tickCounter / 20D;
        double actualProgress = prevProgress + (progress - prevProgress) * partialTicks;
        prevProgress = progress;

        GuiUtils.drawScaledTexturedQuad(BgStartX + BGX / 2 - 50, BgStartY + BGY / 2 - 16, 0, 120,actualProgress * 50, 136, 256, actualProgress * 100, 32 , zLevel);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        tickCounter++;
        if (tickCounter >= 20) {
            tickCounter = 0;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        BgStartX = (this.width / 2) - (BGX / 2);
        BgStartY = (this.height / 2) - (BGY / 2);
    }

    protected void keyTyped(char c, int id) throws IOException {
        if (c == 1 || id == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            this.mc.player.closeScreen();
        }
        super.keyTyped(c, id);
    }


}
