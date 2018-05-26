package com.mctechnicguy.aim.tileentity;

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

}
