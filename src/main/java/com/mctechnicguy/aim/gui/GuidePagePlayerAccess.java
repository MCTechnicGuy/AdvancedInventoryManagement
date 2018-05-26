package com.mctechnicguy.aim.gui;

import com.mctechnicguy.aim.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuidePagePlayerAccess implements ICustomManualEntry {

    @Override
    public boolean hasLeftSidePicture(int page) {
        return true;
    }

    @Override
    public void drawLeftSidePicture(int page, @Nonnull Minecraft mc, @Nonnull GuiAIMGuide gui, float zLevel) {
        GuiAIMGuide.drawScaledTexturedQuad(gui.BgStartX + 15, gui.BgStartY + (GuiAIMGuide.BGY / 2D) - 30, 0, 220, 512, 319, GuiAIMGuide.BGX / 2D - 30, 99 * ((GuiAIMGuide.BGX / 2D - 30) / 512), zLevel);
        GlStateManager.scale(0.75, 0.75, 0.75);
        mc.fontRenderer.drawSplitString(I18n.format("guide.picture.playeraccess"), (int)Math.round((gui.BgStartX + 15) * (1/0.75D)), (int)Math.round((gui.BgStartY + 100) * (1/0.75D)), (int)Math.round((GuiAIMGuide.BGX / 2 - 30) * (1/0.75D)), 4210752);
        GlStateManager.scale(1 / 0.75, 1 / 0.75, 1 / 0.75);
    }

    @Override
    public boolean showHeaderOnPage(int page) {
        return true;
    }

    @Nonnull
    @Override
    public String getManualName() {
        return "playeraccess";
    }

    @Override
    public int getPageCount() {
        return 1;
    }

    @Override
    public boolean doesProvideOwnContent() {
        return true;
    }

    @Nonnull
    @Override
    public Object[] getParams(int page) {
        return new Object[] {ClientProxy.KeyChangeAccess.getDisplayName()};
    }

    @Override
    public boolean needsSmallerFont() {
        return true;
    }

    @Override
    public boolean showCraftingRecipe(int page) {
        return false;
    }

    @Nullable
    @Override
    public ResourceLocation getRecipeForPage(int page) {
        return null;
    }
}
