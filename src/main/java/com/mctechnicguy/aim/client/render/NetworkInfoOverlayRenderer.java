package com.mctechnicguy.aim.client.render;

import com.mctechnicguy.aim.tileentity.IProvidesNetworkInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


@SideOnly(Side.CLIENT)
public class NetworkInfoOverlayRenderer {

    private static final int NAME_OFFSET = -15;
    private static final int TEXT_COLOR = 0x5656FF;

    private int currentOffset;
    private ScaledResolution resolution;
    private IProvidesNetworkInfo tile;

    public int getCurrentOffset() {
        return currentOffset;
    }

    public ScaledResolution getResolution() {
        return resolution;
    }

    public static void renderNetworkInfoOverlay(RenderGameOverlayEvent.Post event, IProvidesNetworkInfo tile) {
        NetworkInfoOverlayRenderer renderer = new NetworkInfoOverlayRenderer(event.getResolution(), tile);
        renderer.renderNetworkInfoOverlay();
    }

    public NetworkInfoOverlayRenderer(ScaledResolution res, IProvidesNetworkInfo tile) {
        this.resolution = res;
        this.currentOffset = 10;
        this.tile = tile;
    }

    private void renderNetworkInfoOverlay() {
        this.renderString(tile.getNameForOverlay(), TEXT_COLOR, NAME_OFFSET);
        tile.renderStatusInformation(this);
        this.renderString(I18n.format("aimoverlay.update") );
    }

    private void renderString(String text, int color, int yOffset) {
        renderString(text, color, yOffset, 0);
    }

    private void renderString(String text, int color) {
        renderString(text, color, currentOffset);
        currentOffset += 12;
    }

    private void renderString(String text) {
        renderString(text, TEXT_COLOR, currentOffset, 0);
        currentOffset += 12;
    }

    private void renderString(String text, int color, int yOffset, int xOffset) {
        Minecraft mc = Minecraft.getMinecraft();
        int x = resolution.getScaledWidth() / 2 - mc.fontRenderer.getStringWidth(text) / 2 + xOffset;
        int y = resolution.getScaledHeight() / 2 + yOffset;
        mc.fontRenderer.drawStringWithShadow(text, x, y, color);
    }

    public void renderStatusString(boolean isActive) {
        TextComponentTranslation activeMessage = new TextComponentTranslation(isActive ? "aimoverlay.status.active" : "aimoverlay.status.inactive");
        activeMessage.getStyle().setColor(isActive ? TextFormatting.GREEN : TextFormatting.RED);
        renderString(I18n.format("aimoverlay.status", activeMessage.getFormattedText()), TEXT_COLOR);
    }

    public void renderStatusString(String unlocalizedStatus, TextFormatting format) {
        TextComponentTranslation activeMessage = new TextComponentTranslation(unlocalizedStatus);
        activeMessage.getStyle().setColor(format);
        renderString(I18n.format("aimoverlay.status", activeMessage.getFormattedText()), TEXT_COLOR);
    }

    public void renderModeString(String unlocalizedModeName) {
        TextComponentTranslation message = new TextComponentTranslation(unlocalizedModeName);
        message.getStyle().setColor(TextFormatting.AQUA);
        renderString(I18n.format("aimoverlay.mode", message.getFormattedText()), TEXT_COLOR);
    }

    public void renderInventoryContent(@Nullable NonNullList<ItemStack> content) {
        Minecraft mc = Minecraft.getMinecraft();
        RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
        int x = resolution.getScaledWidth() / 2;
        int y = resolution.getScaledHeight() / 2 + currentOffset;
        currentOffset += 16;
        mc.fontRenderer.drawStringWithShadow(I18n.format("aimoverlay.content"), x - mc.fontRenderer.getStringWidth(I18n.format("aimoverlay.content")), y + 2, TEXT_COLOR );

        if (content == null) {
            mc.fontRenderer.drawStringWithShadow(I18n.format("aimoverlay.content.unknown"), x + 8, y + 2, TEXT_COLOR );
        } else if (content.isEmpty() || !listHasItems(content)) {
            mc.fontRenderer.drawStringWithShadow(I18n.format("aimoverlay.content.none"), x + 8, y + 2, TEXT_COLOR );
        } else {
            for (int i = 0; i < content.size(); i++) {
                RenderHelper.enableGUIStandardItemLighting();
                itemRender.renderItemIntoGUI(content.get(i), x + 17*i + 8, y - 4);
                itemRender.renderItemOverlays(Minecraft.getMinecraft().fontRenderer, content.get(i), x + 17*i + 8, y - 4);
                RenderHelper.disableStandardItemLighting();
            }
        }
    }

    private boolean listHasItems(@Nonnull NonNullList<ItemStack> content) {
        for (ItemStack stack : content) {
            if (!stack.isEmpty()) return true;
        }
        return false;
    }

    public void renderTileValues(String descKey, TextFormatting valueFormat, boolean valueUnknown, Object... value) {
        TextComponentTranslation message;
        if (!valueUnknown) {
            message = new TextComponentTranslation("aimoverlay." + descKey + ".value", value);
        } else {
            message = new TextComponentTranslation("aimoverlay.content.unknown");
        }
        message.getStyle().setColor(valueFormat);
        renderString(I18n.format("aimoverlay." + descKey, message.getFormattedText()));
    }

}
