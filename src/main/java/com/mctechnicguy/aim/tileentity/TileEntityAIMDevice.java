package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.blocks.IHasModes;
import com.mctechnicguy.aim.client.render.NetworkInfoOverlayRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

import javax.annotation.Nullable;

public abstract class TileEntityAIMDevice extends TileEntityNetworkElement {
	
	
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

	public void renderStatusInformation(ScaledResolution res) {
		super.renderStatusInformation(res);
		if (this.blockType instanceof IHasModes) {
            NetworkInfoOverlayRenderer.renderModeString(res, ((IHasModes)blockType).getCurrentModeUnlocalizedName(world, pos));
        }
        if (this instanceof IHasOwnInventory) {
		    NetworkInfoOverlayRenderer.renderInventoryContent(res, ((IHasOwnInventory) this).getOwnInventoryContent(), 40);
        }

	}

}
