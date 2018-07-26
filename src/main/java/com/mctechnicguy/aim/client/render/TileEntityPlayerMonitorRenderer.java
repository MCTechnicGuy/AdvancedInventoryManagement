package com.mctechnicguy.aim.client.render;


import com.mctechnicguy.aim.tileentity.TileEntityPlayerMonitor;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;

public class TileEntityPlayerMonitorRenderer extends TileEntitySpecialRenderer<TileEntityPlayerMonitor> {

    @Override
    public void render(TileEntityPlayerMonitor te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!te.hasWorld() || !te.getCoreActive()) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);

        String value = te.getFormattedValue();
        String percentage = te.getPercentageFormatted();

        for (EnumFacing face : EnumFacing.HORIZONTALS) {

            if (!te.getWorld().getBlockState(te.getPos().offset(face)).isOpaqueCube()) {
                GlStateManager.translate(0, 0, 0.501);
                GlStateManager.scale(0.010416667F, -0.010416667F, 0.010416667F);

                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680.0F, 0.0F);

                getFontRenderer().drawString(te.getPlayerConnectedName(), -getFontRenderer().getStringWidth(te.getPlayerConnectedName()) / 2, -30, 0xFFFFFF);
                String modeFormatted = I18n.format("mode.monitordisplay." + te.mode.getName());
                getFontRenderer().drawString(modeFormatted, -getFontRenderer().getStringWidth(modeFormatted) / 2, -18, 0xFFFFFF);

                if (value != null) {
                    getFontRenderer().drawString(value, -getFontRenderer().getStringWidth(value) / 2, -6, 0xFFFFFF);
                }

                if (percentage != null) {
                    getFontRenderer().drawString(percentage, -getFontRenderer().getStringWidth(percentage) / 2, 6, 0xFFFFFF);
                }

                GlStateManager.scale(1 / (0.010416667F), 1 / (-0.010416667F), 1 / (0.010416667F));
                GlStateManager.translate(0, 0, -0.501);
            }
            GlStateManager.rotate(-90, 0, 1, 0);
        }

        GlStateManager.translate(-x, -y, -z);
        GlStateManager.popMatrix();
    }
}
