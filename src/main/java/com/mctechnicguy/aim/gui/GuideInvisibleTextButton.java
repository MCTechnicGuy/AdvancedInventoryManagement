package com.mctechnicguy.aim.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nonnull;

public class GuideInvisibleTextButton extends GuiButton {

    public GuideInvisibleTextButton(int id, int x, int y, int width, int height, @Nonnull String text) {
        super(id, x, y, width, height, text);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        int j = 4210752;
        if (mouseX > this.x && mouseX < this.x + width && mouseY > y && mouseY < y + height) j = 15257873;
        GlStateManager.scale(0.5, 0.5, 0.5);
        mc.fontRenderer.drawString(this.displayString, (this.x + this.width / 2) * 2, (this.y + (this.height - 8) / 2) * 2, j);
        GlStateManager.scale(2, 2, 2);
    }

    public void drawButtonWithCustomText(@Nonnull Minecraft mc, int mouseX, int mouseY, @Nonnull String text) {
        int j = 4210752;
        if (mouseX > this.x && mouseX < this.x + width && mouseY > y && mouseY < y + height) j = 15257873;
        GlStateManager.scale(0.5, 0.5, 0.5);
        mc.fontRenderer.drawString(text, (this.x + this.width / 2) * 2, (this.y + (this.height - 8) / 2) * 2, j);
        GlStateManager.scale(2, 2, 2);
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {

    }
}
