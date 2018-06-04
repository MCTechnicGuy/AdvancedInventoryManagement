package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.util.AIMUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

public class TileEntityNetworkCable extends TileEntityNetworkElement {

	@Nonnull
    private boolean[] couldConnectAtSide = new boolean[] { false, false, false, false, false, false };
	@Nonnull
    private boolean[] connectionBlockedAtSide = new boolean[] { false, false, false, false, false, false };

	public TileEntityNetworkCable() {}

	private void writeSyncNBT(@Nonnull NBTTagCompound nbt) {
		for (int i = 0; i < this.connectionBlockedAtSide.length; i++) {
			nbt.setBoolean("connectionblocked_" + i, this.connectionBlockedAtSide[i]);
		}
	}

	private void readSyncNBT(@Nonnull NBTTagCompound nbt) {
		for (int i = 0; i < this.connectionBlockedAtSide.length; i++) {
			this.connectionBlockedAtSide[i] = nbt.getBoolean("connectionblocked_" + i);
		}
		if (this.hasWorld()) world.markBlockRangeForRenderUpdate(pos, pos);
	}

	public void updateConnections() {
		this.resetConnections();
		for (EnumFacing dir : EnumFacing.VALUES) {
			if (AIMUtils.checkForAIMBlockAtSide(dir, world, this.pos))
				couldConnectAtSide[dir.ordinal()] = true;
		}
	}

	public boolean canTransferSignal(EnumFacing dir, boolean comingFromCore) {
		return !isConnectionBlocked(dir);
	}

	public boolean hasRealConnection(@Nonnull EnumFacing dir) {
		return this.connectionPossible(dir) && !this.isConnectionBlocked(dir);
	}

	private boolean connectionPossible(@Nonnull EnumFacing dir) {
		return this.couldConnectAtSide[dir.ordinal()];
	}

	public boolean isConnectionBlocked(@Nonnull EnumFacing dir) {
		return this.connectionBlockedAtSide[dir.ordinal()];
	}

	private void resetConnections() {
		for (int i = 0; i < couldConnectAtSide.length; i++) {
			couldConnectAtSide[i] = false;
		}
	}

	public void setConnectionBlocked(int i, boolean b) {
		this.connectionBlockedAtSide[i] = b;
		this.updateBlock();
	}

	public SPacketUpdateTileEntity getUpdatePacket() {
		if (!world.isRemote) {
			NBTTagCompound nbtTag = new NBTTagCompound();
			this.writeSyncNBT(nbtTag);
			this.writeCoreData(nbtTag);
			return new SPacketUpdateTileEntity(this.pos, 0, nbtTag);
		} else
			return null;
	}

	public void onDataPacket(NetworkManager net, @Nonnull SPacketUpdateTileEntity packet) {
		readSyncNBT(packet.getNbtCompound());
		this.readCoreData(packet.getNbtCompound());
	}

	@Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		this.updateConnections();
		nbt = super.writeToNBT(nbt);
		this.writeSyncNBT(nbt);
		return nbt;
	}

	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.readSyncNBT(nbt);
		this.updateConnections();
	}

}
