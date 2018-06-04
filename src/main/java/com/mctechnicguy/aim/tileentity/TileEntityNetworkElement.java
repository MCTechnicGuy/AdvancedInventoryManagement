package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.client.render.NetworkInfoOverlayRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TileEntityNetworkElement extends TileEntity implements IProvidesNetworkInfo {
	
	@Nullable
    private TileEntityAIMCore coreTile;
	private BlockPos corePos;
	//Single-use-variable to store the core state when the block is rendered for the first time. After that, the core will send updates
	private boolean coreActive;

	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.readCoreData(nbt);
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
		return oldState.getBlock() != newState.getBlock();
	}
	
	@Nonnull
    @Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		this.writeCoreData(nbt);
		return nbt;
	}
	
	@Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbtTag = new NBTTagCompound();
		this.writeCoreData(nbtTag);
		return new SPacketUpdateTileEntity(this.getPos(), 0, nbtTag);
	}

	@Override
	public void onDataPacket(NetworkManager net, @Nonnull SPacketUpdateTileEntity packet) {
		this.readCoreData(packet.getNbtCompound());
	}

	void writeCoreData(@Nonnull NBTTagCompound nbt) {
		if (this.hasCore()) {
			nbt.setInteger("coreX", this.getCore().getPos().getX());
			nbt.setInteger("coreY", this.getCore().getPos().getY());
			nbt.setInteger("coreZ", this.getCore().getPos().getZ());
		}
		nbt.setBoolean("hasCore", this.hasCore());
	}

	void readCoreData(@Nonnull NBTTagCompound nbt) {
		if (nbt.getBoolean("hasCore")) {
			this.setCorePos(new BlockPos(nbt.getInteger("coreX"), nbt.getInteger("coreY"), nbt.getInteger("coreZ")));
		} else
			this.setCore(null);
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbtTag = super.getUpdateTag();
		this.writeCoreData(nbtTag);
		nbtTag.setBoolean("coreActive", isCoreActive());
		return nbtTag;
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		super.handleUpdateTag(tag);
		this.readCoreData(tag);
		coreActive = tag.getBoolean("coreActive");
	}

	public boolean isCoreActive() {
		if (!this.hasCore()) return false;
		if (this.hasWorld() && this.world.isRemote) return this.coreTile.isActive();
		else return this.coreTile.isActive() && this.coreTile.getConnectedPlayer() != null;
	}

	@Nullable
    public TileEntityAIMCore getCore() {
		if (coreTile != null && coreTile.isInvalid()) this.setCore(null);
		return coreTile;
	}

	public boolean hasCore() {
		if (corePos == null) return false;
		if (coreTile == null || coreTile.isInvalid()) {
			TileEntity te = world.getTileEntity(corePos);
			if (!(te instanceof TileEntityAIMCore)) {
				this.setCore(null);
				return false;
			}
			coreTile = (TileEntityAIMCore)te;
		}
		return true;
	}
	
	public boolean isPlayerAccessAllowed(@Nonnull EntityPlayer player) {
		return !this.hasCore() || this.getCore().isPlayerAccessAllowed(player);
	}
	
	public void setCore(@Nullable TileEntityAIMCore newCore) {
		if (coreTile != newCore) {
			this.coreTile = newCore;
			if (newCore != null) setCorePos(newCore.getPos());
			else setCorePos(null);
		}
	}
	
	private void setCorePos(BlockPos newpos) {
		if (newpos != this.corePos) {
			this.corePos = newpos;
			this.updateBlock();
		}
	}
	
	public void updateBlock() {
		coreActive = false;
		if (this.hasWorld()) {
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
			this.markDirty();
		}
	}

	public boolean getCoreActive() {
		if (coreActive) {
			coreActive = false;
			return true;
		}
		return false;
	}

    @Nonnull
    @SideOnly(Side.CLIENT)
    public final ItemStack getDisplayStack() {
        return this.hasWorld() ? new ItemStack(this.getBlockType()) : ItemStack.EMPTY;
    }

    @SideOnly(Side.CLIENT)
	public String getNameForOverlay() {
		return I18n.format(getUnlocalizedBlockName());
	}

    @SideOnly(Side.CLIENT)
	public void renderStatusInformation(ScaledResolution res) {
		NetworkInfoOverlayRenderer.renderStatusString(res, this.isCoreActive());
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    public final String getUnlocalizedBlockName() {
        return this.hasWorld() ? this.getBlockType().getUnlocalizedName() : "";
    }

    @Nullable
    @Override
    public NBTTagCompound getTagForOverlayUpdate() {
        return null;
    }

    @Override
    public void handleTagForOverlayUpdate(NBTTagCompound nbt) {

    }
}
