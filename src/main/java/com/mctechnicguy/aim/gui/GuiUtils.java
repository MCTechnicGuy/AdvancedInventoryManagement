package com.mctechnicguy.aim.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiUtils {

    /**
     * Not perfectly accurate but pretty accurate algorithm for fitting a longer text into a specified rectangle.
     * @param renderer The current font renderer object
     * @param text The text to render to the screen
     * @param wrapWidth The maximal width
     * @param maxHeight The maximal height
     * @return A double for GLScale which resizes the text to fit the given parameters
     */
    static double getTextScaleToFitHeightAndWidth(FontRenderer renderer, String text, int wrapWidth, int maxHeight, double minScale, double maxScale) {
        double expectedHeight = renderer.getWordWrappedHeight(text, wrapWidth);
        if (expectedHeight <= maxHeight) return maxScale;
        double scaledUp = Math.floor(Math.sqrt(maxHeight / expectedHeight) * 100D);
        if (scaledUp <= 0 || (scaledUp / 100D < minScale)) return minScale;
        return Math.min(scaledUp / 100D, maxScale);
    }

    static void drawScaledMultilineString(FontRenderer renderer, String text, int x, int y, int wrapWidth, int maxHeight, int textColor) {
        drawScaledMultilineString(renderer, text, x, y, wrapWidth, maxHeight, textColor, 0.1, 1.0);
    }

    static void drawScaledMultilineString(FontRenderer renderer, String text, int x, int y, int wrapWidth, int maxHeight, int textColor, double minScale, double maxScale) {
        double scale = getTextScaleToFitHeightAndWidth(renderer, text, wrapWidth, maxHeight, minScale, maxScale);
        double reScale = 1 / scale;
        GlStateManager.scale(scale, scale, scale);
        renderer.drawSplitString(text, (int)Math.round(x * reScale), (int)Math.round(y * reScale), (int)Math.round(wrapWidth * reScale), textColor);
        GlStateManager.scale(reScale, reScale, reScale);
    }

    static void drawScaledOnelineString(FontRenderer renderer, String text, int maxWidth, int x, int y, int color) {
        double expectedWidth = renderer.getStringWidth(text);
        if (expectedWidth > maxWidth) {
            double scale = Math.floor(maxWidth / expectedWidth * 100D);
            if (scale <= 0) scale = 0.1;
            else scale /= 100D;
            GlStateManager.scale(scale, scale, scale);
            renderer.drawStringWithShadow(text, (int)Math.round(x / scale), (int)Math.round(y / scale), color);
            GlStateManager.scale(1/scale, 1/scale, 1/scale);
        } else {
            renderer.drawStringWithShadow(text, x, y, color);
        }
    }

    public static void drawTexturedQuad(double x, double y, double u, double v, double width, double height, int textureSize, double zLevel){
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vb = tessellator.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.pos(x, y, zLevel).tex(u / textureSize, v / textureSize).endVertex();
        vb.pos(x, y + height, zLevel).tex(u / textureSize, (v + height) / textureSize).endVertex();
        vb.pos(x + width, y + height, zLevel).tex((u + width) / textureSize, (v + height) / textureSize).endVertex();
        vb.pos(x + width, y, zLevel).tex((u + width) / textureSize, v / textureSize).endVertex();
        tessellator.draw();
    }

    public static void drawScaledTexturedQuad(double x, double y, double u, double v, double maxU, double maxV, int textureSize, double width, double height, double zLevel){
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vb = tessellator.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.pos(x, y, zLevel).tex(u / textureSize, v / textureSize).endVertex();
        vb.pos(x, y + height, zLevel).tex(u / textureSize, maxV / textureSize).endVertex();
        vb.pos(x + width, y + height, zLevel).tex(maxU / textureSize, maxV / textureSize).endVertex();
        vb.pos(x + width, y, zLevel).tex(maxU / textureSize, v / textureSize).endVertex();
        tessellator.draw();
    }


}
