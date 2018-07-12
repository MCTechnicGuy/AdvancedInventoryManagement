package com.mctechnicguy.aim.client.render;

import com.mctechnicguy.aim.blocks.BlockNetworkCable;
import com.mctechnicguy.aim.tileentity.TileEntityNetworkCable;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

public class CableBoundingBoxRenderer {

	public static void renderCableBoundingBox(@Nonnull DrawBlockHighlightEvent event) {
		if (event.getSubID() == 0 && event.getTarget().typeOfHit == RayTraceResult.Type.BLOCK)
        {
			if (event.getPlayer().world.getBlockState(event.getTarget().getBlockPos()).getBlock() instanceof BlockNetworkCable) {
				GlStateManager.enableBlend();
				OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
                GlStateManager.glLineWidth(2.0F);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
            	
            	double minX = 1D/16D*5D;
            	double minY = 1D/16D*5D;
            	double minZ = 1D/16D*5D;
            	double maxX = 1D - 1D/16D*5D;
            	double maxY = 1D - 1D/16D*5D;
            	double maxZ = 1D - 1D/16D*5D;
            	double extension = 1D/16D*5D;

            	float f1 = 0.002F;
            	
                double d0 = event.getPlayer().lastTickPosX + (event.getPlayer().posX - event.getPlayer().lastTickPosX) * (double)event.getPartialTicks();
                double d1 = event.getPlayer().lastTickPosY + (event.getPlayer().posY - event.getPlayer().lastTickPosY) * (double)event.getPartialTicks();
                double d2 = event.getPlayer().lastTickPosZ + (event.getPlayer().posZ - event.getPlayer().lastTickPosZ) * (double)event.getPartialTicks();
            	AxisAlignedBB box = new AxisAlignedBB(event.getTarget().getBlockPos().getX() + minX, event.getTarget().getBlockPos().getY() + minY, event.getTarget().getBlockPos().getZ() + minZ, 
            			event.getTarget().getBlockPos().getX() + maxX, event.getTarget().getBlockPos().getY() + maxY, event.getTarget().getBlockPos().getZ() + maxZ).expand((double)f1, (double)f1, (double)f1).offset(-d0, -d1, -d2); 
            	
            	TileEntityNetworkCable cable = (TileEntityNetworkCable) event.getPlayer().world.getTileEntity(event.getTarget().getBlockPos());
            	if (cable != null) {
            		for (EnumFacing dir : EnumFacing.VALUES) {
            			if (cable.hasRealConnection(dir)) drawExtendedFaces(extension, box, dir);
            			else drawStandardFace(cable, box, dir);
            		}
            	}
                
            	GlStateManager.depthMask(true);
            	GlStateManager.enableTexture2D();
            	GlStateManager.disableBlend();
            	event.setCanceled(true);
			}
        }	
	}

	private static final void drawStandardFace(@Nonnull TileEntityNetworkCable cable, @Nonnull AxisAlignedBB box, @Nonnull EnumFacing dir) {
		BufferBuilder VB = Tessellator.getInstance().getBuffer();
		
		
        VB.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        if (dir.equals(EnumFacing.NORTH)) {
        	if (!cable.hasRealConnection(EnumFacing.WEST)) {
        		VB.pos(box.minX, box.minY, box.minZ).endVertex();
            	VB.pos(box.minX, box.maxY, box.minZ).endVertex();
        	}
        	if (!cable.hasRealConnection(EnumFacing.EAST)) {
        		VB.pos(box.maxX, box.minY, box.minZ).endVertex();
    	        VB.pos(box.maxX, box.maxY, box.minZ).endVertex();
        	}
        	if (!cable.hasRealConnection(EnumFacing.UP)) {
        		VB.pos(box.minX, box.maxY, box.minZ).endVertex();
    	        VB.pos(box.maxX, box.maxY, box.minZ).endVertex();
        	}
        	if (!cable.hasRealConnection(EnumFacing.DOWN)) {
        		VB.pos(box.minX, box.minY, box.minZ).endVertex();
    	        VB.pos(box.maxX, box.minY, box.minZ).endVertex();
        	}
        }
        
       if (dir.equals(EnumFacing.EAST)) {
        	if (!cable.hasRealConnection(EnumFacing.NORTH)) {
        		VB.pos(box.maxX, box.minY, box.minZ).endVertex();
            	VB.pos(box.maxX, box.maxY, box.minZ).endVertex();
        	}
        	if (!cable.hasRealConnection(EnumFacing.SOUTH)) {
        		VB.pos(box.maxX, box.minY, box.maxZ).endVertex();
    	        VB.pos(box.maxX, box.maxY, box.maxZ).endVertex();
        	}
        	if (!cable.hasRealConnection(EnumFacing.UP)) {
        		VB.pos(box.maxX, box.maxY, box.minZ).endVertex();
    	        VB.pos(box.maxX, box.maxY, box.maxZ).endVertex();
        	}
        	if (!cable.hasRealConnection(EnumFacing.DOWN)) {
        		VB.pos(box.maxX, box.minY, box.minZ).endVertex();
    	        VB.pos(box.maxX, box.minY, box.maxZ).endVertex();
        	}
        }
        
       if (dir.equals(EnumFacing.WEST)) {
    	   	if (!cable.hasRealConnection(EnumFacing.NORTH)) {
    	   		VB.pos(box.minX, box.minY, box.minZ).endVertex();
    	   		VB.pos(box.minX, box.maxY, box.minZ).endVertex();
    	   	}
       		if (!cable.hasRealConnection(EnumFacing.SOUTH)) {
       			VB.pos(box.minX, box.minY, box.maxZ).endVertex();
       			VB.pos(box.minX, box.maxY, box.maxZ).endVertex();
       		}
       		if (!cable.hasRealConnection(EnumFacing.UP)) {
       			VB.pos(box.minX, box.maxY, box.minZ).endVertex();
       			VB.pos(box.minX, box.maxY, box.maxZ).endVertex();
       		}
       		if (!cable.hasRealConnection(EnumFacing.DOWN)) {
       			VB.pos(box.minX, box.minY, box.minZ).endVertex();
       			VB.pos(box.minX, box.minY, box.maxZ).endVertex();
       		}
        }
        
        if (dir.equals(EnumFacing.SOUTH)) {
        	if (!cable.hasRealConnection(EnumFacing.WEST)) {
        		VB.pos(box.minX, box.minY, box.maxZ).endVertex();
            	VB.pos(box.minX, box.maxY, box.maxZ).endVertex();
        	}
        	if (!cable.hasRealConnection(EnumFacing.EAST)) {
        		VB.pos(box.maxX, box.minY, box.maxZ).endVertex();
    	        VB.pos(box.maxX, box.maxY, box.maxZ).endVertex();
        	}
        	if (!cable.hasRealConnection(EnumFacing.UP)) {
        		VB.pos(box.minX, box.maxY, box.maxZ).endVertex();
    	        VB.pos(box.maxX, box.maxY, box.maxZ).endVertex();
        	}
        	if (!cable.hasRealConnection(EnumFacing.DOWN)) {
        		VB.pos(box.minX, box.minY, box.maxZ).endVertex();
    	        VB.pos(box.maxX, box.minY, box.maxZ).endVertex();
        	}
        }
        Tessellator.getInstance().draw();
	}

	private static final void drawExtendedFaces(double extension, @Nonnull AxisAlignedBB box, @Nonnull EnumFacing dir) {
		BufferBuilder VB = Tessellator.getInstance().getBuffer();
		
		if (dir.equals(EnumFacing.UP)) {
			VB.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
			VB.pos(box.minX, box.maxY + extension, box.minZ).endVertex();
			VB.pos(box.minX, box.maxY + extension, box.maxZ).endVertex();
			VB.pos(box.maxX, box.maxY + extension, box.maxZ).endVertex();
			VB.pos(box.maxX, box.maxY + extension, box.minZ).endVertex();
			VB.pos(box.minX, box.maxY + extension, box.minZ).endVertex();
			Tessellator.getInstance().draw();
			
			VB.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
			VB.pos(box.minX, box.maxY + extension, box.minZ).endVertex();
			VB.pos(box.minX, box.maxY, box.minZ).endVertex();
			VB.pos(box.maxX, box.maxY + extension, box.minZ).endVertex();
			VB.pos(box.maxX, box.maxY, box.minZ).endVertex();
			VB.pos(box.minX, box.maxY + extension, box.maxZ).endVertex();
			VB.pos(box.minX, box.maxY, box.maxZ).endVertex();
			VB.pos(box.maxX, box.maxY + extension, box.maxZ).endVertex();
			VB.pos(box.maxX, box.maxY, box.maxZ).endVertex();
			Tessellator.getInstance().draw();
		}
		
		if (dir.equals(EnumFacing.DOWN)) {
			VB.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
			VB.pos(box.minX, box.minY - extension, box.minZ).endVertex();
			VB.pos(box.minX, box.minY - extension, box.maxZ).endVertex();
			VB.pos(box.maxX, box.minY - extension, box.maxZ).endVertex();
			VB.pos(box.maxX, box.minY - extension, box.minZ).endVertex();
			VB.pos(box.minX, box.minY - extension, box.minZ).endVertex();
			Tessellator.getInstance().draw();
			
			VB.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
			VB.pos(box.minX, box.minY - extension, box.minZ).endVertex();
			VB.pos(box.minX, box.minY, box.minZ).endVertex();
			VB.pos(box.maxX, box.minY - extension, box.minZ).endVertex();
			VB.pos(box.maxX, box.minY, box.minZ).endVertex();
			VB.pos(box.minX, box.minY - extension, box.maxZ).endVertex();
			VB.pos(box.minX, box.minY, box.maxZ).endVertex();
			VB.pos(box.maxX, box.minY - extension, box.maxZ).endVertex();
			VB.pos(box.maxX, box.minY, box.maxZ).endVertex();
			Tessellator.getInstance().draw();
		}
		
		if (dir.equals(EnumFacing.EAST)) {
			VB.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
			VB.pos(box.maxX + extension, box.minY, box.minZ).endVertex();
			VB.pos(box.maxX + extension, box.minY, box.maxZ).endVertex();
			VB.pos(box.maxX + extension, box.maxY, box.maxZ).endVertex();
			VB.pos(box.maxX + extension, box.maxY, box.minZ).endVertex();
			VB.pos(box.maxX + extension, box.minY, box.minZ).endVertex();
			Tessellator.getInstance().draw();
			
			VB.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
			VB.pos(box.maxX, box.minY, box.minZ).endVertex();
			VB.pos(box.maxX + extension, box.minY, box.minZ).endVertex();
			VB.pos(box.maxX, box.maxY, box.minZ).endVertex();
			VB.pos(box.maxX + extension, box.maxY, box.minZ).endVertex();
			VB.pos(box.maxX, box.minY, box.maxZ).endVertex();
			VB.pos(box.maxX + extension, box.minY, box.maxZ).endVertex();
			VB.pos(box.maxX, box.maxY, box.maxZ).endVertex();
			VB.pos(box.maxX + extension, box.maxY, box.maxZ).endVertex();
			Tessellator.getInstance().draw();
		}
		
		if (dir.equals(EnumFacing.WEST)) {
			VB.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
			VB.pos(box.minX - extension, box.minY, box.minZ).endVertex();
			VB.pos(box.minX - extension, box.minY, box.maxZ).endVertex();
			VB.pos(box.minX - extension, box.maxY, box.maxZ).endVertex();
			VB.pos(box.minX - extension, box.maxY, box.minZ).endVertex();
			VB.pos(box.minX - extension, box.minY, box.minZ).endVertex();
			Tessellator.getInstance().draw();
			
			VB.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
			VB.pos(box.minX, box.minY, box.minZ).endVertex();
			VB.pos(box.minX - extension, box.minY, box.minZ).endVertex();
			VB.pos(box.minX, box.maxY, box.minZ).endVertex();
			VB.pos(box.minX - extension, box.maxY, box.minZ).endVertex();
			VB.pos(box.minX, box.minY, box.maxZ).endVertex();
			VB.pos(box.minX - extension, box.minY, box.maxZ).endVertex();
			VB.pos(box.minX, box.maxY, box.maxZ).endVertex();
			VB.pos(box.minX - extension, box.maxY, box.maxZ).endVertex();
			Tessellator.getInstance().draw();
		}
		
		if (dir.equals(EnumFacing.NORTH)) {
			VB.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
			VB.pos(box.minX, box.minY, box.minZ - extension).endVertex();
			VB.pos(box.minX, box.maxY, box.minZ - extension).endVertex();
			VB.pos(box.maxX, box.maxY, box.minZ - extension).endVertex();
			VB.pos(box.maxX, box.minY, box.minZ - extension).endVertex();
			VB.pos(box.minX, box.minY, box.minZ - extension).endVertex();
			Tessellator.getInstance().draw();
			
			VB.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
			VB.pos(box.minX, box.minY, box.minZ).endVertex();
			VB.pos(box.minX, box.minY, box.minZ - extension).endVertex();
			VB.pos(box.maxX, box.minY, box.minZ).endVertex();
			VB.pos(box.maxX, box.minY, box.minZ - extension).endVertex();
			VB.pos(box.minX, box.maxY, box.minZ).endVertex();
			VB.pos(box.minX, box.maxY, box.minZ - extension).endVertex();
			VB.pos(box.maxX, box.maxY, box.minZ).endVertex();
			VB.pos(box.maxX, box.maxY, box.minZ - extension).endVertex();
			Tessellator.getInstance().draw();
		}
		
		if (dir.equals(EnumFacing.SOUTH)) {
			VB.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
			VB.pos(box.minX, box.minY, box.maxZ + extension).endVertex();
			VB.pos(box.minX, box.maxY, box.maxZ + extension).endVertex();
			VB.pos(box.maxX, box.maxY, box.maxZ + extension).endVertex();
			VB.pos(box.maxX, box.minY, box.maxZ + extension).endVertex();
			VB.pos(box.minX, box.minY, box.maxZ + extension).endVertex();
			Tessellator.getInstance().draw();
			
			VB.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
			VB.pos(box.minX, box.minY, box.maxZ).endVertex();
			VB.pos(box.minX, box.minY, box.maxZ + extension).endVertex();
			VB.pos(box.maxX, box.minY, box.maxZ).endVertex();
			VB.pos(box.maxX, box.minY, box.maxZ + extension).endVertex();
			VB.pos(box.minX, box.maxY, box.maxZ).endVertex();
			VB.pos(box.minX, box.maxY, box.maxZ + extension).endVertex();
			VB.pos(box.maxX, box.maxY, box.maxZ).endVertex();
			VB.pos(box.maxX, box.maxY, box.maxZ + extension).endVertex();
			Tessellator.getInstance().draw();
		}
		
		
	}
}
