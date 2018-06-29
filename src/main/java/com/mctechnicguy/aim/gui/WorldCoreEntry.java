package com.mctechnicguy.aim.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class WorldCoreEntry implements GuiListExtended.IGuiListEntry {
    private BlockPos pos;
    private String dimName;
    private int dimID;
    private long lastClickTime;
    private WorldNetworkList parentList;

    WorldCoreEntry(BlockPos pos, String dimName, int dimID, WorldNetworkList parentList) {
        this.pos = pos;
        this.dimName = dimName;
        this.dimID = dimID;
        this.parentList = parentList;
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getDimID() {
        return dimID;
    }

    public String getDimName() {
        return dimName;
    }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
        if (y < this.parentList.top + 2 || y > this.parentList.bottom - this.parentList.slotHeight + 2) return;
        FontRenderer renderer = Minecraft.getMinecraft().fontRenderer;
        GuiUtils.drawScaledOnelineString(renderer, I18n.format("gui.advancedinfo.list.coreheader", pos.getX(), pos.getY(), pos.getZ(), dimID, dimName), listWidth - 10, x + 5, y + slotHeight / 2 - renderer.FONT_HEIGHT / 2, 0xFFFFFF);
    }

    @Override
    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        this.parentList.setSelectedSlotIndex(slotIndex);
        if (Minecraft.getSystemTime() - this.lastClickTime < 250L)
        {
            this.parentList.getOwner().showDetailPage(this);
            return true;
        }
        else
        {
            this.lastClickTime = Minecraft.getSystemTime();
            return false;
        }
    }

    @Override
    public void updatePosition(int slotIndex, int x, int y, float partialTicks) { }

    @Override
    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) { }
}
