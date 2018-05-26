package com.mctechnicguy.aim.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class GuideTexturedButton extends GuiButton {

    private int u, v;

    public GuideTexturedButton(int id, int x, int y, int width, int height, int u, int v) {
        super(id, x, y, width, height, "");
        this.u = u;
        this.v = v;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        GuiAIMGuide.drawTexturedQuad(this.x, this.y, u, v, width, height, zLevel);
    }
}
