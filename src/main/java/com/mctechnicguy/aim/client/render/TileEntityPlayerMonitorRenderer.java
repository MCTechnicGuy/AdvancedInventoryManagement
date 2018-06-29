package com.mctechnicguy.aim.client.render;


import com.mctechnicguy.aim.tileentity.TileEntityPlayerMonitor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class TileEntityPlayerMonitorRenderer extends TileEntitySpecialRenderer<TileEntityPlayerMonitor> {

    @Override
    public void render(TileEntityPlayerMonitor te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!te.isCoreActive()) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);

        for (int i = 0; i < 4; i++) {
            GlStateManager.translate(0, 0, 0.501);
            GlStateManager.scale(0.010416667F, -0.010416667F, 0.010416667F);

            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680.0F, 0.0F);

            getFontRenderer().drawString(te.getCore().playerConnectedName, -getFontRenderer().getStringWidth(te.getCore().playerConnectedName) / 2, -30, 1000000);
            getFontRenderer().drawString(te.getModeFormatted(), -getFontRenderer().getStringWidth(te.getModeFormatted()) / 2, -18, 1000000);
            String value = te.getFormattedValue();
            if (value != null) {
                getFontRenderer().drawString(value, -getFontRenderer().getStringWidth(value) / 2, -6, 1000000);
            }
            String percentage = te.getPercentageValue();
            if (percentage != null) {
                getFontRenderer().drawString(percentage, -getFontRenderer().getStringWidth(percentage) / 2, 6, 1000000);
            }


            GlStateManager.scale(1 / (0.010416667F), 1 / (-0.010416667F), 1 / (0.010416667F));
            GlStateManager.translate(0, 0, -0.501);
            GlStateManager.rotate(90, 0, 1, 0);
        }

        GlStateManager.translate(-x, -y, -z);
        GlStateManager.popMatrix();
    }
}
