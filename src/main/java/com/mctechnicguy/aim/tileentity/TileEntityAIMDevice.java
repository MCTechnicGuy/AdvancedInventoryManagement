package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.blocks.BlockAIMModulatedDevice;
import com.mctechnicguy.aim.client.render.NetworkInfoOverlayRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public abstract class TileEntityAIMDevice extends TileEntityNetworkElement {
	
	boolean hasAccurateServerInfo = false;

	public TileEntityAIMDevice() {
	}

	public int getDeviceMode() {
		return world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
	}

	InventoryPlayer getPlayerInventory() {
		return this.getCore().getConnectedPlayer().inventory;
	}
	
	@Nullable
    public EntityPlayer getPlayer() {
		return this.getCore().getConnectedPlayer();
	}

	@Override
    @SideOnly(Side.CLIENT)
	public void renderStatusInformation(NetworkInfoOverlayRenderer renderer) {
		super.renderStatusInformation(renderer);
		if (this.blockType instanceof BlockAIMModulatedDevice) {
            renderer.renderModeString(((BlockAIMModulatedDevice)blockType).getCurrentModeUnlocalizedName(world, pos));
        }
        if (this instanceof IHasOwnInventory) {
            renderer.renderInventoryContent(((IHasOwnInventory) this).getOwnInventoryContent());
        }

	}

    @Override
    @SideOnly(Side.CLIENT)
    public void invalidateServerInfo() {
        super.invalidateServerInfo();
        hasAccurateServerInfo = false;
    }
}
