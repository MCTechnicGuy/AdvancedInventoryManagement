package com.mctechnicguy.aim.client.render;


import com.mctechnicguy.aim.tileentity.TileEntityPlayerMonitor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class TileEntityPlayerMonitorRenderer extends TileEntitySpecialRenderer<TileEntityPlayerMonitor> {

    @Override
    public void render(TileEntityPlayerMonitor te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        //TODO!!!
        /*if (!te.hasWorld() || !te.getCoreActive()) return;



        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);

        String value = te.getFormattedValue();
        String percentage = te.getPercentageValue();

        for (EnumFacing face : EnumFacing.HORIZONTALS) {

            if (!te.getWorld().getBlockState(te.getPos().offset(face)).isOpaqueCube()) {
                GlStateManager.translate(0, 0, 0.501);
                GlStateManager.scale(0.010416667F, -0.010416667F, 0.010416667F);

                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680.0F, 0.0F);

                getFontRenderer().drawString(te.getCore().playerConnectedName, -getFontRenderer().getStringWidth(te.getCore().playerConnectedName) / 2, -30, 0xFFFFFF);
                getFontRenderer().drawString(te.getModeFormatted(), -getFontRenderer().getStringWidth(te.getModeFormatted()) / 2, -18, 0xFFFFFF);

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
        GlStateManager.popMatrix();*/
    }
}
