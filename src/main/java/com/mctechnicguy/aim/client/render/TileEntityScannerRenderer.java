package com.mctechnicguy.aim.client.render;

import com.mctechnicguy.aim.ModInfo;
import com.mctechnicguy.aim.tileentity.TileEntityScanner;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.nio.FloatBuffer;

public class TileEntityScannerRenderer extends TileEntitySpecialRenderer<TileEntityScanner>  {

	private static final ResourceLocation texture = new ResourceLocation(
			ModInfo.ID + ":textures/blocks/scanner_effects.png");
	private static final double Pixel = 1D / 16D;
	private static final double TexPixel = 1D / 32D;

	@Override
	public void render(TileEntityScanner te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (te.getStatus() == TileEntityScanner.EnumScanStatus.OFFLINE)
			return;

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder VB = tess.getBuffer();
		this.bindTexture(texture);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();

		GlStateManager.translate(x, y, z);

		switch (te.getStatus()) {
		case OFFLINE:
			return;
		case DOORS_OPEN:
			this.moveDoors(false, te.getStatus().duration - te.getActionTicksLeft(), VB);
			break;
		case LASER_OUT:
			this.moveLasers(false, te.getStatus().duration - te.getActionTicksLeft(), VB);
			break;
		case LASER_CIRCLING:
			this.circleLaser(te.getStatus().duration - te.getActionTicksLeft(), VB, te.getScannedPlayer() == null ? 3 : 1 + te.getScannedPlayer().height);
			break;
		case LASER_IN:
			this.moveLasers(true, te.getStatus().duration - te.getActionTicksLeft(), VB);
			break;
		case DOORS_CLOSE:
			this.moveDoors(true, te.getStatus().duration - te.getActionTicksLeft(), VB);
			break;
		}

		RenderHelper.enableStandardItemLighting();
		GlStateManager.translate(-x, -y, -z);
	}

	private final void circleLaser(int duration, @Nonnull BufferBuilder VB, float playerHeight) {
		GlStateManager.translate(0.5, 0, 0.5);
		GlStateManager.rotate(duration * 1.8F, 0, 1, 0);
		GlStateManager.translate(-0.5, 0, -0.5);
		
		VB.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		renderLaserAt(0.5, 0.125, -0.75, VB);
		renderLaserAt(0.5, 0.125, 1.75, VB);
		Tessellator.getInstance().draw();

        GlStateManager.translate(0.5, 0, 0.5);
        GlStateManager.rotate(-duration * 1.8F, 0, 1, 0);
        GlStateManager.translate(-0.5, 0, -0.5);


        Vector3f startPoint1 = new Vector3f(0, 0.125F + (float)Pixel * 4F, -1.25F);

        double angle = Math.toRadians(duration * 1.8D);
        float cosA = (float)Math.cos(-angle);
        float sinA = (float)Math.sin(-angle);

        Matrix3f rotation = new Matrix3f();
        rotation.load(FloatBuffer.wrap(new float[] {cosA, 0, sinA, 0, 1, 0, -sinA, 0, cosA}));
        startPoint1 = Matrix3f.transform(rotation, startPoint1, null);


        float angleRatio = (float) (angle / (2 * Math.PI));
        float x;
        float z;

        //-x, +z, +x, -z

        if (angleRatio <= 0.125) {
            x = -angleRatio / 0.25F;
            z = -0.495F;
        } else if (angleRatio <= 0.375) {
            x = -0.495F;
            z = -0.495F + (angleRatio - 0.125F) / 0.25F;
        } else if (angleRatio <= 0.625) {
            x = -0.495F + (angleRatio - 0.375F) / 0.25F;
            z = 0.495F;
        } else if (angleRatio <= 0.875) {
            x = 0.495F;
            z = 0.495F - ((angleRatio - 0.625F) / 0.25F);
        } else {
            x = 0.495F - ((angleRatio - 0.875F)) / 0.25F;
            z = -0.495F;
        }

		GlStateManager.disableTexture2D();
		VB.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
		GlStateManager.glLineWidth(5.0F);
		GlStateManager.color(0x4c / 256F, 0xb7 / 256F, 0x51 / 256F, 0.7F);
		VB.pos(startPoint1.x + 0.5, startPoint1.y, startPoint1.z + 0.5).endVertex();
		VB.pos(x + 0.5, playerHeight - duration * 0.01, z + 0.5).endVertex();
		VB.pos(-startPoint1.x + 0.5, 0.125 + Pixel * 4, -startPoint1.z + 0.5).endVertex();
		VB.pos(-x + 0.5, playerHeight - duration * 0.01, -z + 0.5).endVertex();

		Tessellator.getInstance().draw();
		
		GlStateManager.enableTexture2D();
		GlStateManager.color(1F, 1F, 1F, 0.7F);
		GlStateManager.glLineWidth(1F);


		VB.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX); //Scanning-Quad
		VB.pos(0, playerHeight - duration * 0.01, 0).tex(0, 16 * TexPixel).endVertex();
		VB.pos(0, playerHeight - duration * 0.01, 1).tex(0, 32 * TexPixel).endVertex();
		VB.pos(1, playerHeight - duration * 0.01, 1).tex(16 * TexPixel, 32 * TexPixel).endVertex();
		VB.pos(1, playerHeight - duration * 0.01, 0).tex(16 * TexPixel, 16 * TexPixel).endVertex();
		Tessellator.getInstance().draw();	
		GlStateManager.color(1F, 1F, 1F, 1F);
	}

	private final void moveLasers(boolean afterScan, int duration, @Nonnull BufferBuilder VB) {
		VB.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		if (!afterScan) {
			renderLaserAt(0.5, 0.125, 0.25 - 0.02 * duration, VB);
			renderLaserAt(0.5, 0.125, 0.75 + 0.02 * duration, VB);
		} else {
			renderLaserAt(0.5, 0.125, -0.75 + 0.02 * duration, VB);
			renderLaserAt(0.5, 0.125, 1.75 - 0.02 * duration, VB);
		}
		Tessellator.getInstance().draw();
	}

	private final void moveDoors(boolean afterScan, int duration, @Nonnull BufferBuilder VB) {

		VB.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		if (!afterScan) {
			if (duration <= 5) {
				renderDoorsAt(duration * 0.01, 0.125, VB);
			} else {
				renderDoorsAt(0.05, 0.125 + (duration - 5) * 0.0109375, VB);
			}
		} else {
			if (duration >= 40) {
				renderDoorsAt(0.05 - (duration - 40) * 0.01, 0.125, VB);
			} else {
				renderDoorsAt(0.05, 0.5625 - duration * 0.0109375, VB);
			}
		}
		

		renderLaserAt(0.5, 0.125, 0.25, VB);
		renderLaserAt(0.5, 0.125, 0.75, VB);
		Tessellator.getInstance().draw();

	}

	private final void renderDoorsAt(double zPos, double yPos, @Nonnull BufferBuilder VB) {
		VB.pos(0.3125, yPos, zPos).tex(0, 0).endVertex();
		VB.pos(0.3125, 0.5625, zPos).tex(0, 7 * TexPixel - ((yPos -0.125) / 2)).endVertex();
		VB.pos(0.6875, 0.5625, zPos).tex(6 * TexPixel, 7 * TexPixel - ((yPos -0.125) / 2) ).endVertex();
		VB.pos(0.6875, yPos, zPos).tex(6 * TexPixel, 0).endVertex();

		VB.pos(0.3125, yPos, 1 - zPos).tex(0, 0).endVertex();
		VB.pos(0.3125, 0.5625, 1 - zPos).tex(0, 7 * TexPixel - ((yPos -0.125) / 2)).endVertex();
		VB.pos(0.6875, 0.5625, 1 - zPos).tex(6 * TexPixel, 7 * TexPixel - ((yPos -0.125) / 2)).endVertex();
		VB.pos(0.6875, yPos, 1 - zPos).tex(6 * TexPixel, 0).endVertex();
	}

	private final void renderLaserAt(double xPos, double yPos, double zPos, @Nonnull BufferBuilder VB) {
		VB.setTranslation(xPos, yPos, zPos);

		VB.pos(-Pixel, 0, -Pixel).tex(16 * TexPixel, 15 * TexPixel).endVertex();
		VB.pos(-Pixel, Pixel, -Pixel).tex(16 * TexPixel, 16 * TexPixel).endVertex();
		VB.pos(Pixel, Pixel, -Pixel).tex(18 * TexPixel, 16 * TexPixel).endVertex();
		VB.pos(Pixel, 0, -Pixel).tex(18 * TexPixel, 15 * TexPixel).endVertex();

		VB.pos(-Pixel * 2, Pixel, -Pixel * 2).tex(17 * TexPixel, 3 * TexPixel).endVertex();
		VB.pos(-Pixel * 2, Pixel * 2, -Pixel * 2).tex(17 * TexPixel, 4 * TexPixel).endVertex();
		VB.pos(Pixel * 2, Pixel * 2, -Pixel * 2).tex(21 * TexPixel, 4 * TexPixel).endVertex();
		VB.pos(Pixel * 2, Pixel, -Pixel * 2).tex(21 * TexPixel, 3 * TexPixel).endVertex();

		VB.pos(-Pixel * 2, Pixel * 2, -Pixel * 2).tex(17 * TexPixel, 4 * TexPixel).endVertex();
		VB.pos(-Pixel, Pixel * 3, -Pixel).tex(17 * TexPixel, 5 * TexPixel).endVertex();
		VB.pos(Pixel, Pixel * 3, -Pixel).tex(21 * TexPixel, 5 * TexPixel).endVertex();
		VB.pos(Pixel * 2, Pixel * 2, -Pixel * 2).tex(21 * TexPixel, 4 * TexPixel).endVertex();

		VB.pos(-Pixel, Pixel * 3, -Pixel).tex(17 * TexPixel, 3 * TexPixel).endVertex();
		VB.pos(-Pixel, Pixel * 4, -Pixel).tex(17 * TexPixel, 4 * TexPixel).endVertex();
		VB.pos(Pixel, Pixel * 4, -Pixel).tex(19 * TexPixel, 4 * TexPixel).endVertex();
		VB.pos(Pixel, Pixel * 3, -Pixel).tex(19 * TexPixel, 4 * TexPixel).endVertex();

		//Second side
		VB.pos(-Pixel, 0, Pixel).tex(16 * TexPixel, 15 * TexPixel).endVertex();
		VB.pos(-Pixel, Pixel, Pixel).tex(16 * TexPixel, 16 * TexPixel).endVertex();
		VB.pos(Pixel, Pixel, Pixel).tex(18 * TexPixel, 16 * TexPixel).endVertex();
		VB.pos(Pixel, 0, Pixel).tex(18 * TexPixel, 15 * TexPixel).endVertex();

		VB.pos(-Pixel * 2, Pixel, Pixel * 2).tex(17 * TexPixel, 3 * TexPixel).endVertex();
		VB.pos(-Pixel * 2, Pixel * 2, Pixel * 2).tex(17 * TexPixel, 4 * TexPixel).endVertex();
		VB.pos(Pixel * 2, Pixel * 2, Pixel * 2).tex(21 * TexPixel, 4 * TexPixel).endVertex();
		VB.pos(Pixel * 2, Pixel, Pixel * 2).tex(21 * TexPixel, 3 * TexPixel).endVertex();

		VB.pos(-Pixel * 2, Pixel * 2, Pixel * 2).tex(17 * TexPixel, 4 * TexPixel).endVertex();
		VB.pos(-Pixel, Pixel * 3, Pixel).tex(17 * TexPixel, 5 * TexPixel).endVertex();
		VB.pos(Pixel, Pixel * 3, Pixel).tex(21 * TexPixel, 5 * TexPixel).endVertex();
		VB.pos(Pixel * 2, Pixel * 2, Pixel * 2).tex(21 * TexPixel, 4 * TexPixel).endVertex();

		VB.pos(-Pixel, Pixel * 3, Pixel).tex(17 * TexPixel, 3 * TexPixel).endVertex();
		VB.pos(-Pixel, Pixel * 4, Pixel).tex(17 * TexPixel, 4 * TexPixel).endVertex();
		VB.pos(Pixel, Pixel * 4, Pixel).tex(19 * TexPixel, 4 * TexPixel).endVertex();
		VB.pos(Pixel, Pixel * 3, Pixel).tex(19 * TexPixel, 4 * TexPixel).endVertex();

		VB.pos(-Pixel, 0, -Pixel).tex(16 * TexPixel, 15 * TexPixel).endVertex();
		VB.pos(-Pixel, Pixel, -Pixel).tex(16 * TexPixel, 16 * TexPixel).endVertex();
		VB.pos(-Pixel, Pixel, Pixel).tex(18 * TexPixel, 16 * TexPixel).endVertex();
		VB.pos(-Pixel, 0, Pixel).tex(18 * TexPixel, 15 * TexPixel).endVertex();

		VB.pos(-Pixel * 2, Pixel, -Pixel * 2).tex(17 * TexPixel, 3 * TexPixel).endVertex();
		VB.pos(-Pixel * 2, Pixel * 2, -Pixel * 2).tex(17 * TexPixel, 4 * TexPixel).endVertex();
		VB.pos(-Pixel * 2, Pixel * 2, Pixel * 2).tex(21 * TexPixel, 4 * TexPixel).endVertex();
		VB.pos(-Pixel * 2, Pixel, Pixel * 2).tex(21 * TexPixel, 3 * TexPixel).endVertex();

		VB.pos(-Pixel * 2, Pixel * 2, -Pixel * 2).tex(17 * TexPixel, 4 * TexPixel).endVertex();
		VB.pos(-Pixel, Pixel * 3, -Pixel).tex(17 * TexPixel, 5 * TexPixel).endVertex();
		VB.pos(-Pixel, Pixel * 3, Pixel).tex(21 * TexPixel, 5 * TexPixel).endVertex();
		VB.pos(-Pixel * 2, Pixel * 2, Pixel * 2).tex(21 * TexPixel, 4 * TexPixel).endVertex();

		VB.pos(-Pixel, Pixel * 3, -Pixel).tex(17 * TexPixel, 3 * TexPixel).endVertex();
		VB.pos(-Pixel, Pixel * 4, -Pixel).tex(17 * TexPixel, 4 * TexPixel).endVertex();
		VB.pos(-Pixel, Pixel * 4, Pixel).tex(19 * TexPixel, 4 * TexPixel).endVertex();
		VB.pos(-Pixel, Pixel * 3, Pixel).tex(19 * TexPixel, 4 * TexPixel).endVertex();

		VB.pos(Pixel, 0, -Pixel).tex(16 * TexPixel, 15 * TexPixel).endVertex();
		VB.pos(Pixel, Pixel, -Pixel).tex(16 * TexPixel, 16 * TexPixel).endVertex();
		VB.pos(Pixel, Pixel, Pixel).tex(18 * TexPixel, 16 * TexPixel).endVertex();
		VB.pos(Pixel, 0, Pixel).tex(18 * TexPixel, 15 * TexPixel).endVertex();

		VB.pos(Pixel * 2, Pixel, -Pixel * 2).tex(17 * TexPixel, 3 * TexPixel).endVertex();
		VB.pos(Pixel * 2, Pixel * 2, -Pixel * 2).tex(17 * TexPixel, 4 * TexPixel).endVertex();
		VB.pos(Pixel * 2, Pixel * 2, Pixel * 2).tex(21 * TexPixel, 4 * TexPixel).endVertex();
		VB.pos(Pixel * 2, Pixel, Pixel * 2).tex(21 * TexPixel, 3 * TexPixel).endVertex();

		VB.pos(Pixel * 2, Pixel * 2, -Pixel * 2).tex(17 * TexPixel, 4 * TexPixel).endVertex();
		VB.pos(Pixel, Pixel * 3, -Pixel).tex(17 * TexPixel, 5 * TexPixel).endVertex();
		VB.pos(Pixel, Pixel * 3, Pixel).tex(21 * TexPixel, 5 * TexPixel).endVertex();
		VB.pos(Pixel * 2, Pixel * 2, Pixel * 2).tex(21 * TexPixel, 4 * TexPixel).endVertex();

		VB.pos(Pixel, Pixel * 3, -Pixel).tex(17 * TexPixel, 3 * TexPixel).endVertex();
		VB.pos(Pixel, Pixel * 4, -Pixel).tex(17 * TexPixel, 4 * TexPixel).endVertex();
		VB.pos(Pixel, Pixel * 4, Pixel).tex(19 * TexPixel, 4 * TexPixel).endVertex();
		VB.pos(Pixel, Pixel * 3, Pixel).tex(19 * TexPixel, 4 * TexPixel).endVertex();
		
		VB.pos(-Pixel, Pixel * 4, -Pixel).tex(17 * TexPixel, 1 * TexPixel).endVertex();
		VB.pos(-Pixel, Pixel * 4, Pixel).tex(17 * TexPixel, 3 * TexPixel).endVertex();
		VB.pos(Pixel, Pixel * 4, Pixel).tex(19 * TexPixel, 3 * TexPixel).endVertex();
		VB.pos(Pixel, Pixel * 4, -Pixel).tex(19 * TexPixel, 1 * TexPixel).endVertex();

		VB.pos(-Pixel * 2, Pixel, -Pixel * 2).tex(16 * TexPixel, 16 * TexPixel).endVertex();
		VB.pos(-Pixel * 2, Pixel, Pixel * 2).tex(16 * TexPixel, 20 * TexPixel).endVertex();
		VB.pos(Pixel * 2, Pixel, Pixel * 2).tex(20 * TexPixel, 20 * TexPixel).endVertex();
		VB.pos(Pixel * 2, Pixel, -Pixel * 2).tex(20 * TexPixel, 16 * TexPixel).endVertex();

		VB.setTranslation(0, 0, 0);
	}

}
