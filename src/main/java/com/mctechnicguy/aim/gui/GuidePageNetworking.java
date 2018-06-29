package com.mctechnicguy.aim.gui;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class GuidePageNetworking implements ICustomManualEntry {

    @Override
    public boolean hasLeftSidePicture(int page) {
        return true;
    }

    @Override
    public void drawLeftSidePicture(int page, @Nonnull Minecraft mc, @Nonnull GuiAIMGuide gui, float zLevel) {
        mc.getTextureManager().bindTexture(GuiAIMGuide.LogoTexture);
        GuiUtils.drawScaledTexturedQuad(gui.BgStartX + 15, gui.BgStartY + (GuiAIMGuide.BGY / 2D) - 50, 0, 512 + 225, 512, 1024, 512, GuiAIMGuide.BGX / 2D - 30, 289 * ((GuiAIMGuide.BGX / 2D - 30) / 512D), zLevel);
        GlStateManager.scale(0.75, 0.75, 0.75);
        mc.fontRenderer.drawSplitString(I18n.format("guide.picture.networking"), (int)Math.round((gui.BgStartX + 15) * (1/0.75D)), (int)Math.round((gui.BgStartY + 125) * (1/0.75D)), (int)Math.round((GuiAIMGuide.BGX / 2 - 30) * (1/0.75D)), 4210752);
        GlStateManager.scale(1 / 0.75, 1 / 0.75, 1 / 0.75);
    }

    @Override
    public boolean showHeaderOnPage(int page) {
        return true;
    }

    @Nonnull
    @Override
    public String getManualName() {
        return "networking";
    }

    @Override
    public int getPageCount() {
        return 1;
    }

    @Nonnull
    @Override
    public Object[] getParams(int page) {
        return new Object[] {AdvancedInventoryManagement.MAX_CABLE_LENGHT};
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
