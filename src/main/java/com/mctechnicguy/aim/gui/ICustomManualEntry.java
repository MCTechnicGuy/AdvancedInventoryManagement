package com.mctechnicguy.aim.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public interface ICustomManualEntry extends IManualEntry {

    boolean showCraftingRecipe(int page);
    boolean hasLeftSidePicture(int page);
    void drawLeftSidePicture(int page, Minecraft mc, GuiAIMGuide gui, float zLevel);
    @Nullable
    ResourceLocation getRecipeForPage(int Page);
    boolean showHeaderOnPage(int page);
}
