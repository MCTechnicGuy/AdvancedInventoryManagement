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

import javax.annotation.Nullable;

public class NetworkInfoOverlayRenderer {

    private static final int NAME_OFFSET = -15;
    private static final int STATUS_OFFSET = 10;
    private static final int MODE_OFFSET = 20;
    private static final int UPDATE_INFO_STRING_OFFSET = 70;

    public static void renderNetworkInfoOverlay(RenderGameOverlayEvent.Post event, IProvidesNetworkInfo tile) {
        renderStringAt(event.getResolution(), tile.getNameForOverlay(), 0x5656FF, NAME_OFFSET);
        tile.renderStatusInformation(event.getResolution());
        renderStringAt(event.getResolution(), I18n.format("aimoverlay.update"),0x5656FF , UPDATE_INFO_STRING_OFFSET);
    }

    private static void renderStringAt(ScaledResolution resolution, String text, int color, int yOffset) {
        renderStringAt(resolution, text, color, yOffset, 0);
    }

    private static void renderStringAt(ScaledResolution resolution, String text, int color, int yOffset, int xOffset) {
        Minecraft mc = Minecraft.getMinecraft();
        int x = resolution.getScaledWidth() / 2 - mc.fontRenderer.getStringWidth(text) / 2 + xOffset;
        int y = resolution.getScaledHeight() / 2 + yOffset;
        mc.fontRenderer.drawStringWithShadow(text, x, y, color);
    }

    public static void renderStatusString(ScaledResolution resolution, boolean isActive) {
        TextComponentTranslation activeMessage = new TextComponentTranslation(isActive ? "aimoverlay.status.active" : "aimoverlay.status.inactive");
        activeMessage.getStyle().setColor(isActive ? TextFormatting.GREEN : TextFormatting.RED);
        renderStringAt(resolution, I18n.format("aimoverlay.status", activeMessage.getFormattedText()), 0x5656FF, STATUS_OFFSET);
    }

    public static void renderModeString(ScaledResolution resolution, String unlocalizedModeName) {
        TextComponentTranslation message = new TextComponentTranslation(unlocalizedModeName);
        message.getStyle().setColor(TextFormatting.AQUA);
        renderStringAt(resolution, I18n.format("aimoverlay.mode", message.getFormattedText()), 0x5656FF, MODE_OFFSET);
    }

    public static void renderInventoryContent(ScaledResolution resolution, @Nullable NonNullList<ItemStack> content, int yOffset) {
        Minecraft mc = Minecraft.getMinecraft();
        RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
        int x = resolution.getScaledWidth() / 2;
        int y = resolution.getScaledHeight() / 2 + yOffset;
        mc.fontRenderer.drawStringWithShadow(I18n.format("aimoverlay.content"), x - mc.fontRenderer.getStringWidth(I18n.format("aimoverlay.content")), y,0x5656FF );

        if (content == null) {
            mc.fontRenderer.drawStringWithShadow(I18n.format("aimoverlay.content.unknown"), x + 8, y,0x5656FF );
        } else if (content.isEmpty()) {
            mc.fontRenderer.drawStringWithShadow(I18n.format("aimoverlay.content.none"), x + 8, y,0x5656FF );
        } else {
            for (int i = 0; i < content.size(); i++) {
                RenderHelper.enableGUIStandardItemLighting();
                itemRender.renderItemIntoGUI(content.get(i), x + 17*i + 8, y - 4);
                itemRender.renderItemOverlays(Minecraft.getMinecraft().fontRenderer, content.get(i), x + 17*i + 8, y - 4);
                RenderHelper.disableStandardItemLighting();
            }
        }
    }

}
