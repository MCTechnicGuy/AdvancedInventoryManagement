package com.mctechnicguy.aim.event;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.ClientProxy;
import com.mctechnicguy.aim.capability.CapabilityPlayerAccess;
import com.mctechnicguy.aim.client.render.CableBoundingBoxRenderer;
import com.mctechnicguy.aim.client.render.NetworkInfoOverlayRenderer;
import com.mctechnicguy.aim.items.ItemAIMInfoProvider;
import com.mctechnicguy.aim.network.PacketHelper;
import com.mctechnicguy.aim.network.PacketKeyPressed;
import com.mctechnicguy.aim.tileentity.IProvidesNetworkInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class AIMEventHandler {

    private BlockPos lastSelectedBlockPos = null;

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onKeyInput(InputEvent.KeyInputEvent event) {
        if(ClientProxy.KeyChangeAccess.isPressed()) {
        	PacketHelper.sendPacketToServer(new PacketKeyPressed());
        }

    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        Profiler profiler = mc.mcProfiler;

        if (mc.player.getHeldItemMainhand().getItem() instanceof ItemAIMInfoProvider || mc.player.getHeldItemOffhand().getItem() instanceof ItemAIMInfoProvider) {
            if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
                profiler.startSection("aim-advanced-info-provider-overlay");

                RayTraceResult traceResult = mc.objectMouseOver;
                if (traceResult != null && traceResult.typeOfHit == RayTraceResult.Type.BLOCK) {


                    if (lastSelectedBlockPos != null && !lastSelectedBlockPos.equals(traceResult.getBlockPos()) && mc.world.getTileEntity(lastSelectedBlockPos) instanceof IProvidesNetworkInfo) {
                        ((IProvidesNetworkInfo) mc.world.getTileEntity(lastSelectedBlockPos)).invalidateServerInfo();
                    }
                    TileEntity tileEntity = mc.world.getTileEntity(traceResult.getBlockPos());
                    if (tileEntity instanceof IProvidesNetworkInfo) {
                        lastSelectedBlockPos = traceResult.getBlockPos();
                        NetworkInfoOverlayRenderer.renderNetworkInfoOverlay(event, (IProvidesNetworkInfo)tileEntity);
                    } else {
                        lastSelectedBlockPos = null;
                    }
                }
                profiler.endSection();
            }
        } else  if (lastSelectedBlockPos != null && mc.world.getTileEntity(lastSelectedBlockPos) instanceof IProvidesNetworkInfo) {
            ((IProvidesNetworkInfo) mc.world.getTileEntity(lastSelectedBlockPos)).invalidateServerInfo();
        }
    }
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onBlockHighlight(@Nonnull DrawBlockHighlightEvent event) {
		CableBoundingBoxRenderer.renderCableBoundingBox(event);
	}

	
	@SubscribeEvent
	public void onEntityConstructing(@Nonnull AttachCapabilitiesEvent event) {
		if (event.getObject() instanceof EntityPlayer) {
			event.addCapability(CapabilityPlayerAccess.CAP_IDENTIFIER, new CapabilityPlayerAccess((EntityPlayer)event.getObject()));
		}
	}
	
	@SubscribeEvent
	public void onPlayerCloned(@Nonnull PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			if (event.getOriginal().hasCapability(AdvancedInventoryManagement.PLAYER_ACCESS_CAP, null)) {
				CapabilityPlayerAccess oldCap = event.getOriginal().getCapability(AdvancedInventoryManagement.PLAYER_ACCESS_CAP, null);
				CapabilityPlayerAccess newCap = event.getEntityPlayer().getCapability(AdvancedInventoryManagement.PLAYER_ACCESS_CAP, null);
				newCap.setAccessible(oldCap.isAccessible());
			}
		}
	}
	
}
