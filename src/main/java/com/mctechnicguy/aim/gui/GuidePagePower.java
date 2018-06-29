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
public class GuidePagePower implements ICustomManualEntry {

    @Override
    public boolean hasLeftSidePicture(int page) {
        return true;
    }

    @Override
    public void drawLeftSidePicture(int page, @Nonnull Minecraft mc, @Nonnull GuiAIMGuide gui, float zLevel) {
        GuiUtils.drawScaledTexturedQuad(gui.BgStartX + 15, gui.BgStartY + GuiAIMGuide.BGY / 2 - 135 / 2, 325, 0, 512, 180, 512, GuiAIMGuide.BGX / 2 - 30, 180 * (((GuiAIMGuide.BGX / 2D) - 30) / 187), zLevel);
        GlStateManager.scale(0.75, 0.75, 0.75);
        mc.fontRenderer.drawSplitString(I18n.format("guide.picture.power"), (int)Math.round((gui.BgStartX + 15) * (1/0.75D)), (int)Math.round((gui.BgStartY + 160) * (1/0.75D)), (int)Math.round((GuiAIMGuide.BGX / 2 - 30) * (1/0.75D)), 4210752);
        GlStateManager.scale(1 / 0.75, 1 / 0.75, 1 / 0.75);
    }

    @Override
    public boolean showHeaderOnPage(int page) {
        return true;
    }

    @Nonnull
    @Override
    public String getManualName() {
        return "power";
    }

    @Override
    public int getPageCount() {
        return 1;
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

    @Nonnull
    @Override
    public Object[] getParams(int page) {
        return new Object[] {AdvancedInventoryManagement.BASE_POWER_USAGE, AdvancedInventoryManagement.POWER_PER_CABLE, AdvancedInventoryManagement.POWER_PER_MACHINE};
    }

}
